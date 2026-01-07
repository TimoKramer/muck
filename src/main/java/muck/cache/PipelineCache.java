package muck.cache;

import muck.model.Pipeline;
import muck.model.Run;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Thread-safe in-memory cache for pipeline and run data.
 */
public class PipelineCache {
    private static final Logger LOGGER = Logger.getLogger(PipelineCache.class.getName());

    private volatile List<Pipeline> pipelines = List.of();
    private final ConcurrentHashMap<String, List<Run>> runsByPipeline = new ConcurrentHashMap<>();
    private volatile Instant lastUpdated = null;
    private volatile boolean healthy = false;

    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    public List<Run> getRuns(String group, String name) {
        String key = makeKey(group, name);
        return runsByPipeline.getOrDefault(key, List.of());
    }

    public void updatePipelines(List<Pipeline> newPipelines) {
        this.pipelines = List.copyOf(newPipelines);
        this.lastUpdated = Instant.now();
        LOGGER.fine("Updated pipelines cache with " + newPipelines.size() + " entries");
    }

    public void updateRuns(String group, String name, List<Run> runs) {
        String key = makeKey(group, name);
        runsByPipeline.put(key, List.copyOf(runs));
    }

    public void pruneStaleRuns(List<Pipeline> currentPipelines) {
        var validKeys = currentPipelines.stream()
                .map(p -> makeKey(p.group(), p.name()))
                .toList();
        runsByPipeline.keySet().removeIf(key -> !validKeys.contains(key));
    }

    public Optional<Instant> getLastUpdated() {
        return Optional.ofNullable(lastUpdated);
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    private String makeKey(String group, String name) {
        return group + "/" + name;
    }
}
