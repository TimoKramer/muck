package muck.model;

/**
 * Represents a pipeline run from Bob CI/CD
 *
 * @param status       Run status (e.g., "running", "passed", "failed")
 * @param runId        Unique identifier for the run
 * @param completedAt  Timestamp when the run completed (ISO 8601 format)
 * @param initiatedAt  Timestamp when the run was initiated (ISO 8601 format)
 * @param scheduledAt  Timestamp when the run was scheduled (ISO 8601 format)
 * @param logger       Logger associated with the run
 */
public record Run(
        String status,
        String runId,
        String completedAt,
        String initiatedAt,
        String scheduledAt,
        String logger) {

    /**
     * Compact constructor with null safety
     */
    public Run {
        status = status != null ? status : "unknown";
        runId = runId != null ? runId : "";
        completedAt = completedAt != null ? completedAt : "";
        initiatedAt = initiatedAt != null ? initiatedAt : "";
        scheduledAt = scheduledAt != null ? scheduledAt : "";
        logger = logger != null ? logger : "";
    }

    /**
     * Check if the run is complete
     */
    public boolean isComplete() {
        return !completedAt.isEmpty();
    }

    /**
     * Check if the run was successful
     */
    public boolean isSuccessful() {
        return "passed".equalsIgnoreCase(status);
    }
}
