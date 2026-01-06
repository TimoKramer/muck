# ADR 001: Central In-Memory Cache for Pipeline and Run Data

## Status

Accepted

## Context

Muck is a web UI for monitoring Bob CI/CD pipelines. Previously, every HTTP request to view pipelines or runs triggered synchronous API calls to the Bob server:

- `GET /pipelines` called `bobClient.listPipelines()`, then for each pipeline called `bobClient.listRuns()` to fetch the latest status
- `GET /runs?group=X&name=Y` called `bobClient.listRuns()`

This approach had several problems:

1. **Latency**: Users experienced slow page loads, especially on the pipelines page which made N+1 API calls (1 for pipelines, N for each pipeline's runs)
2. **Bob server load**: Every page refresh hit the Bob API, multiplied by concurrent users
3. **Fragility**: If Bob was slow or temporarily unavailable, the UI became unresponsive

## Decision

Introduce a central in-memory cache with scheduled background refresh:

- **PipelineCache**: Thread-safe storage using `volatile` references for the pipeline list and `ConcurrentHashMap` for runs
- **CacheRefresher**: A `ScheduledExecutorService` that polls Bob every 5 seconds, updating the cache
- **Handlers**: Read from cache instead of calling Bob directly

The cache stores immutable `List.copyOf()` snapshots to prevent concurrent modification issues.

## Consequences

### Advantages

1. **Fast response times**: HTTP requests return immediately from memory, no network I/O blocking
2. **Reduced Bob server load**: Only one client (the refresher) polls Bob, regardless of how many users are viewing the UI
3. **Resilience**: If Bob is temporarily unavailable, users still see the last known state instead of an error page
4. **Predictable performance**: Response time is constant and independent of Bob's latency
5. **Simplicity**: Single-threaded refresher avoids complex synchronization; handlers just read immutable snapshots

### Disadvantages

1. **Stale data**: Users may see data up to 5 seconds old. A pipeline could complete but the UI won't reflect it until the next refresh cycle
2. **Memory usage**: All pipeline and run data is held in memory. For large Bob installations with thousands of runs, this could be significant
3. **Startup delay**: With `block-on-startup: true`, the server won't accept requests until the first successful Bob API call. If Bob is down at startup, Muck won't start
4. **No cache invalidation**: The cache only refreshes on a timer. There's no mechanism for Bob to push updates or for specific entries to be invalidated
5. **Single point of failure**: If the refresher thread dies unexpectedly, the cache becomes permanently stale (mitigated by catching all exceptions in the refresh loop)
6. **Polling inefficiency**: We poll every 5 seconds even when nothing has changed, wasting resources. A webhook or event-driven approach would be more efficient

### Trade-offs Accepted

| Concern | Trade-off |
|---------|-----------|
| Data freshness vs. performance | Accepted 5-second staleness for instant page loads |
| Memory vs. latency | Accepted memory overhead to eliminate per-request API calls |
| Complexity vs. resilience | Kept implementation simple; no sophisticated cache eviction, TTLs, or distributed caching |

## Alternatives Considered

### 1. Per-request caching with TTL
Cache API responses with a short TTL (e.g., 5 seconds). First request in the window hits Bob, subsequent requests use cache.

- **Pro**: Simpler, no background thread
- **Con**: First user after TTL expires still experiences slow load; cache thundering herd problem under load

### 2. HTTP caching headers
Have Bob return `Cache-Control` headers and use an HTTP caching proxy.

- **Pro**: Standard mechanism, offloads caching logic
- **Con**: Requires Bob to support it; adds infrastructure complexity

### 3. WebSocket/SSE for real-time updates
Bob pushes updates to Muck when pipeline state changes.

- **Pro**: Always fresh data, no polling waste
- **Con**: Requires Bob to support push notifications; significantly more complex implementation

### 4. No caching
Keep the current synchronous approach.

- **Pro**: Always fresh data, simple
- **Con**: Unacceptable latency and Bob server load as identified in the problem statement

## References

- `src/main/java/muck/cache/PipelineCache.java` - Cache implementation
- `src/main/java/muck/cache/CacheRefresher.java` - Scheduled refresh task
- `src/main/resources/application.yaml` - Configuration (`cache.refresh-interval-seconds`)
