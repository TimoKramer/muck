package muck.model;

/**
 * Represents a Bob CI/CD pipeline
 */
public class Pipeline {
    private final String group;
    private final String name;
    private String status;

    public Pipeline(String group, String name) {
        this.group = group != null ? group : "default";
        this.name = name;
        this.status = "unknown";
    }

    public Pipeline(String group, String name, String status) {
        this.group = group != null ? group : "default";
        this.name = name;
        this.status = status != null ? status : "unknown";
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullName() {
        return group + "/" + name;
    }

    @Override
    public String toString() {
        return "Pipeline{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
