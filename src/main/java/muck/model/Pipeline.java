package muck.model;

import java.util.Map;

/**
 * Represents a Bob CI/CD pipeline
 *
 * @param group      Pipeline group (defaults to "default" if null)
 * @param name       Pipeline name
 * @param status     Pipeline status (defaults to "unknown" if null)
 * @param definition Raw pipeline definition from the API (image, steps, vars, resources, etc.)
 */
public record Pipeline(String group, String name, String status, Map<String, Object> definition) {

    public Pipeline {
        group = group != null ? group : "default";
        status = status != null ? status : "unknown";
        definition = definition != null ? definition : Map.of();
    }

    public Pipeline(String group, String name) {
        this(group, name, "unknown", Map.of());
    }

    public Pipeline(String group, String name, String status) {
        this(group, name, status, Map.of());
    }

    public String fullName() {
        return group + "/" + name;
    }

    public Pipeline withStatus(String newStatus) {
        return new Pipeline(this.group, this.name, newStatus, this.definition);
    }
}
