package muck.client;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.HeaderNames;
import io.helidon.http.Method;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import muck.model.Pipeline;
import muck.model.Run;

public class BobClient {
    private static final Logger LOGGER = Logger.getLogger(BobClient.class.getName());

    private final String baseUrl;
    private final Http1Client client;
    private final OpenAPI openApiSpec;
    private final Map<String, ApiOperation> operations;

    public BobClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = Http1Client.builder()
                .baseUri(baseUrl)
                .build();
        this.openApiSpec = loadOpenApiSpec(this.client);
        this.operations = parseOperations(this.openApiSpec);
    }

    private OpenAPI loadOpenApiSpec(Http1Client client) {
        try {
            var response = client.get("/api.yaml").request();
            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch OpenAPI spec from server: {0}", response.status());
                return null;
            }

            var result = new OpenAPIParser()
                    .readContents(response.as(String.class), null, null);

            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                LOGGER.log(Level.WARNING, "OpenAPI spec parsing warnings: {0}", result.getMessages());
            }

            LOGGER.fine("Successfully loaded OpenAPI specification");
            return result.getOpenAPI();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load OpenAPI spec", e);
            throw e;
        }
    }

    private Map<String, ApiOperation> parseOperations(OpenAPI openApiSpec) {
        var ops = new HashMap<String, ApiOperation>();

        if (openApiSpec == null || openApiSpec.getPaths() == null) {
            LOGGER.warning("No OpenAPI spec or paths available");
            return ops;
        }

        openApiSpec.getPaths().forEach((path, pathItem) -> {
            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                if (operation.getOperationId() != null) {
                    var apiOp = new ApiOperation(operation.getOperationId(), path, Method.create(httpMethod.name()));
                    ops.put(operation.getOperationId(), apiOp);
                }
            });
        });

        LOGGER.log(Level.INFO, "Parsed {0} API operations from OpenAPI spec", ops.size());
        return ops;
    }

    private ApiOperation getOperation(String operationId) {
        var op = operations.get(operationId);
        if (op == null) {
            throw new IllegalStateException("Operation '%s' not found in OpenAPI spec".formatted(operationId));
        }
        return op;
    }

    private Http1ClientResponse executeRequest(Method method, String path) {
        return executeRequest(method, path, Map.of());
    }

    private Http1ClientResponse executeRequest(Method method, String path, Map<String, String> queryParams) {
        var req = client.method(method).path(path);
        queryParams.forEach(req::queryParam);
        return req.request();
    }

    public List<Pipeline> listPipelines() {
        return listPipelines(null, null);
    }

    public List<Pipeline> listPipelines(String group, String name) {
        try {
            var op = getOperation("PipelineList");
            var queryParams = group != null && name != null ? Map.of("group", group, "name", name) : Map.<String, String>of();
            var response = executeRequest(op.method(), op.path(), queryParams);

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch pipelines: {0}", response.status());
                return List.of();
            }

            Map<String, Object> body = response.as(Map.class);
            List<Map<String, Object>> pipelinesData = (List<Map<String, Object>>) body.get("message");

            var pipelines = pipelinesData.stream()
                    .map(data -> new Pipeline(
                            (String) data.get("group"),
                            (String) data.get("name"),
                            "unknown",
                            data))
                    .toList();

            LOGGER.log(Level.FINE, "Fetched {0} pipelines", pipelines.size());
            return pipelines;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching pipelines", e);
            return List.of();
        }
    }

    public List<Pipeline> listPipelinesWithStatus() {
        var pipelines = listPipelines();
        return pipelines.stream()
                .map(p -> {
                    var runs = listRuns(p.group(), p.name());
                    var latestStatus = runs.stream()
                            .max(java.util.Comparator.comparing(Run::scheduledAt))
                            .map(Run::status)
                            .orElse("unknown");
                    return p.withStatus(latestStatus);
                })
                .toList();
    }

    public List<Run> listRuns(String group, String name) {
        try {
            var op = getOperation("PipelineRuns");
            var path = op.path()
                    .replace("{group}", group)
                    .replace("{name}", name);
            var response = executeRequest(op.method(), path);

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch pipeline runs: {0}", response.status());
                return List.of();
            }

            Map<String, Object> body = response.as(Map.class);
            var runsData = (List<Map<String, Object>>) body.get("message");

            var runs = runsData.stream()
                    .map(data -> new Run(
                            (String) data.get("status"),
                            (String) data.get("name"),
                            (String) data.get("group"),
                            (String) data.get("run-id"),
                            (String) data.get("completed-at"),
                            (String) data.get("scheduled-at"),
                            (String) data.get("logger")))
                    .toList();

            LOGGER.log(Level.FINE, "Fetched {0} runs for pipeline {1}/{2}", new Object[] { runs.size(), group, name });
            return runs;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching pipeline runs", e);
            return List.of();
        }
    }

    public Http1ClientResponse fetchLogs(String run) {
        try {
            var op = getOperation("PipelineLogs");
            var path = op.path().replace("{id}", run);
            LOGGER.log(Level.INFO, "Fetching logs from Bob: method={0}, url={1}",
                    new Object[] { op.method(), baseUrl + path });

            return client
                    .method(op.method())
                    .readTimeout(Duration.ZERO)
                    .path(path)
                    .queryParam("follow", "true")
                    .request();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception in fetchLogs for run: " + run, e);
            throw e;
        }
    }

    public String getPipelineStatus(String group, String name) {
        try {
            var op = getOperation("PipelineStatus");
            var path = op.path()
                    .replace("{group}", group)
                    .replace("{name}", name);
            var response = executeRequest(op.method(), path);

            if (response.status() != Status.OK_200) {
                return "unknown";
            }

            Map<String, Object> statusData = response.as(Map.class);
            return (String) statusData.getOrDefault("status", "unknown");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error fetching pipeline status", e);
            return "error";
        }
    }

    public boolean startPipeline(String group, String name, String logger) {
        try {
            var op = getOperation("PipelineStart");
            var path = op.path()
                    .replace("{group}", group)
                    .replace("{name}", name)
                    .replace("{logger}", logger);
            var response = executeRequest(op.method(), path);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error starting pipeline", e);
            return false;
        }
    }

    public boolean deletePipeline(String group, String name) {
        try {
            var op = getOperation("PipelineDelete");
            var path = op.path()
                    .replace("{group}", group)
                    .replace("{name}", name);
            var response = executeRequest(op.method(), path);
            LOGGER.warning("DELETE: " + response.status().toString());

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error deleting pipeline", e);
            return false;
        }
    }

    public boolean stopPipeline(String runId) {
        try {
            var op = getOperation("PipelineStop");
            var path = op.path().replace("{run-id}", runId);
            var response = executeRequest(op.method(), path);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error stopping pipeline", e);
            return false;
        }
    }

    public boolean createPipeline(String body) {
        try {
            var op = getOperation("PipelineCreate");
            var response = client.method(op.method())
                    .path(op.path())
                    .header(HeaderNames.CONTENT_TYPE, "application/json")
                    .submit(body);
            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating pipeline", e);
            return false;
        }
    }

    public boolean pausePipeline(String group, String name) {
        try {
            var op = getOperation("PipelinePause");
            var path = op.path()
                    .replace("{group}", group)
                    .replace("{name}", name);
            var response = executeRequest(op.method(), path);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error pausing pipeline", e);
            return false;
        }
    }

    public boolean unpausePipeline(String group, String name) {
        try {
            var op = getOperation("PipelineUnpause");
            var path = op.path()
                    .replace("{group}", group)
                    .replace("{name}", name);
            var response = executeRequest(op.method(), path);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error unpausing pipeline", e);
            return false;
        }
    }

    public Http1ClientResponse fetchArtifact(String group, String name, String runId, String store, String artifact) {
        var op = getOperation("PipelineArtifactFetch");
        var path = op.path()
                .replace("{group}", group)
                .replace("{name}", name)
                .replace("{id}", runId)
                .replace("{store-name}", store)
                .replace("{artifact-name}", artifact);

        LOGGER.log(Level.INFO, "Fetching artifact from Bob: method={0}, url={1}",
                new Object[] { op.method(), baseUrl + path });

        return client.method(op.method())
                .readTimeout(Duration.ZERO)
                .path(path)
                .request();
    }

    public List<Map<String, Object>> listResourceProviders() {
        try {
            var op = getOperation("ResourceProviderList");
            var response = executeRequest(op.method(), op.path());

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch resource providers: {0}", response.status());
                return List.of();
            }

            Map<String, Object> body = response.as(Map.class);
            return (List<Map<String, Object>>) body.get("message");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching resource providers", e);
            return List.of();
        }
    }

    public boolean createResourceProvider(String body) {
        try {
            var op = getOperation("ResourceProviderCreate");
            var response = client.method(op.method())
                    .path(op.path())
                    .header(HeaderNames.CONTENT_TYPE, "application/json")
                    .submit(body);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating resource provider", e);
            return false;
        }
    }

    public boolean deleteResourceProvider(String name) {
        try {
            var op = getOperation("ResourceProviderDelete");
            var path = op.path().replace("{name}", name);
            var response = executeRequest(op.method(), path);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error deleting resource provider", e);
            return false;
        }
    }

    public List<Map<String, Object>> listArtifactStores() {
        try {
            var op = getOperation("ArtifactStoreList");
            var response = executeRequest(op.method(), op.path());

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch artifact stores: {0}", response.status());
                return List.of();
            }

            Map<String, Object> body = response.as(Map.class);
            return (List<Map<String, Object>>) body.get("message");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching artifact stores", e);
            return List.of();
        }
    }

    public List<Map<String, Object>> listLoggers() {
        try {
            var op = getOperation("LoggerList");
            var response = executeRequest(op.method(), op.path());

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch loggers: {0}", response.status());
                return List.of();
            }

            Map<String, Object> body = response.as(Map.class);
            return (List<Map<String, Object>>) body.get("message");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching loggers", e);
            return List.of();
        }
    }

    public boolean createArtifactStore(String body) {
        try {
            var op = getOperation("ArtifactStoreCreate");
            var response = client.method(op.method())
                    .path(op.path())
                    .header(HeaderNames.CONTENT_TYPE, "application/json")
                    .submit(body);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating artifact store", e);
            return false;
        }
    }

    public boolean deleteArtifactStore(String name) {
        try {
            var op = getOperation("ArtifactStoreDelete");
            var path = op.path().replace("{name}", name);
            var response = executeRequest(op.method(), path);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error deleting artifact store", e);
            return false;
        }
    }

    public boolean createLogger(String body) {
        try {
            var op = getOperation("LoggerCreate");
            var response = client.method(op.method())
                    .path(op.path())
                    .header(HeaderNames.CONTENT_TYPE, "application/json")
                    .submit(body);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating logger", e);
            return false;
        }
    }

    public boolean deleteLogger(String name) {
        try {
            var op = getOperation("LoggerDelete");
            var path = op.path().replace("{name}", name);
            var response = executeRequest(op.method(), path);

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error deleting logger", e);
            return false;
        }
    }

    public Map<String, Object> getClusterInfo() {
        try {
            var op = getOperation("ClusterInfo");
            var response = executeRequest(op.method(), op.path());

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch cluster info: {0}", response.status());
                return Map.of();
            }

            return response.as(Map.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching cluster info", e);
            return Map.of();
        }
    }

    public boolean checkHealth() {
        try {
            var op = getOperation("HealthCheck");
            var response = executeRequest(op.method(), op.path());
            return response.status() == Status.OK_200;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Health check failed", e);
            return false;
        }
    }

    public OpenAPI getOpenApiSpec() {
        return openApiSpec;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
