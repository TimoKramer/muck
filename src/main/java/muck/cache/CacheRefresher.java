package muck.cache;

import muck.client.BobClient;
import muck.model.Pipeline;
import muck.model.Run;

import java.time.Duration;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduled task that periodically refreshes the pipeline cache.
 */
public class CacheRefresher implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(CacheRefresher.class.getName());

    private final BobClient bobClient;
    private final PipelineCache cache;
    private final Duration refreshInterval;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CacheRefresher(BobClient bobClient, PipelineCache cache, Duration refreshInterval) {
        this.bobClient = bobClient;
        this.cache = cache;
        this.refreshInterval = refreshInterval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cache-refresher");
            t.setDaemon(true);
            return t;
        });
    }

    public void start(boolean blockOnInitialLoad) {
        if (running.compareAndSet(false, true)) {
            LOGGER.info("Starting cache refresher with interval: " + refreshInterval);

            if (blockOnInitialLoad) {
                LOGGER.info("Performing blocking initial cache load...");
                refresh();
            }

            scheduler.scheduleAtFixedRate(
                    this::refresh,
                    blockOnInitialLoad ? refreshInterval.toSeconds() : 0,
                    refreshInterval.toSeconds(),
                    TimeUnit.SECONDS
            );

            LOGGER.info("Cache refresher started successfully");
        }
    }

    private void refresh() {
        LOGGER.fine("Starting cache refresh cycle");
        try {
            cache.setHealthy(bobClient.checkHealth());

            var pipelines = bobClient.listPipelines();

            if (pipelines.isEmpty()) {
                LOGGER.warning("No pipelines returned from Bob API - keeping existing cache");
                return;
            }

            var enrichedPipelines = pipelines.stream()
                    .map(this::fetchAndCacheRuns)
                    .toList();

            cache.updatePipelines(enrichedPipelines);
            cache.pruneStaleRuns(enrichedPipelines);

            LOGGER.fine("Cache refresh completed: " + enrichedPipelines.size() + " pipelines");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Cache refresh failed, will retry next cycle", e);
        }
    }

    private Pipeline fetchAndCacheRuns(Pipeline pipeline) {
        try {
            var runs = bobClient.listRuns(pipeline.group(), pipeline.name());
            cache.updateRuns(pipeline.group(), pipeline.name(), runs);

            var latestStatus = runs.stream()
                    .max(Comparator.comparing(Run::scheduledAt))
                    .map(Run::status)
                    .orElse("unknown");

            return pipeline.withStatus(latestStatus);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Failed to fetch runs for " + pipeline.fullName() + ", using cached data", e);
            return pipeline.withStatus("unknown");
        }
    }

    public void triggerRefresh() {
        if (running.get()) {
            scheduler.execute(this::refresh);
        }
    }

    @Override
    public void close() {
        LOGGER.info("Shutting down cache refresher...");
        running.set(false);
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Cache refresher shutdown complete");
    }
}
