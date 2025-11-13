package muck.client;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import muck.model.Pipeline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for interacting with the Bob CI/CD REST API.
 * Operations are dynamically loaded from the OpenAPI specification.
 */
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
        this.openApiSpec = loadOpenApiSpec();
        this.operations = parseOperations();
    }

    private OpenAPI loadOpenApiSpec() {
        try {
            var specStream = getClass().getClassLoader()
                    .getResourceAsStream("api.yaml");
            if (specStream == null) {
                LOGGER.warning("Could not load api.yaml from resources");
                return null;
            }

            // Parse OpenAPI spec from classpath
            var result = new OpenAPIParser()
                    .readContents(new String(specStream.readAllBytes()), null, null);

            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                LOGGER.log(Level.WARNING, "OpenAPI spec parsing warnings: {0}", result.getMessages());
            }

            LOGGER.info("Successfully loaded OpenAPI specification");
            return result.getOpenAPI();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load OpenAPI spec", e);
            return null;
        }
    }

    /**
     * Parse all operations from the OpenAPI spec and map them by operationId
     */
    private Map<String, ApiOperation> parseOperations() {
        var ops = new HashMap<String, ApiOperation>();

        if (openApiSpec == null || openApiSpec.getPaths() == null) {
            LOGGER.warning("No OpenAPI spec or paths available");
            return ops;
        }

        openApiSpec.getPaths().forEach((path, pathItem) -> {
            addOperation(ops, path, pathItem.getGet(), "GET");
            addOperation(ops, path, pathItem.getPost(), "POST");
            addOperation(ops, path, pathItem.getPut(), "PUT");
            addOperation(ops, path, pathItem.getDelete(), "DELETE");
            addOperation(ops, path, pathItem.getPatch(), "PATCH");
        });

        LOGGER.log(Level.INFO, "Parsed {0} API operations from OpenAPI spec", ops.size());
        return ops;
    }

    private void addOperation(Map<String, ApiOperation> ops, String path, Operation operation, String method) {
        if (operation != null && operation.getOperationId() != null) {
            var apiOp = new ApiOperation(operation.getOperationId(), path, method);
            ops.put(operation.getOperationId(), apiOp);
        }
    }

    /**
     * Get an API operation by its operationId
     */
    private ApiOperation getOperation(String operationId) {
        var op = operations.get(operationId);
        if (op == null) {
            throw new IllegalStateException("Operation '%s' not found in OpenAPI spec".formatted(operationId));
        }
        return op;
    }

    /**
     * Fetch all pipelines from Bob API
     * Uses operationId: PipelineList
     * Returns: {"message": [{"name": "...", "group": "..."}, ...]}
     */
    public List<Pipeline> listPipelines() {
        try {
            var op = getOperation("PipelineList");
            var response = client.get(op.path()).request();

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch pipelines: {0}", response.status());
                return List.of();
            }

            // Bob API returns: {"message": [...]} wrapper
            Map<String, Object> body = response.as(Map.class);
            List<Map<String, Object>> pipelinesData = (List<Map<String, Object>>) body.get("message");

            var pipelines = pipelinesData.stream()
                    .map(data -> new Pipeline(
                            (String) data.get("group"),
                            (String) data.get("name")))
                    .toList();

            LOGGER.log(Level.INFO, "Fetched {0} pipelines", pipelines.size());
            return pipelines;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching pipelines", e);
            return List.of();
        }
    }

    /**
     * Get the status of a specific pipeline
     * Uses operationId: PipelineStatus
     */
    public String getPipelineStatus(String group, String name) {
        try {
            var op = getOperation("PipelineStatus");
            var path = op.path()
                    .replace("{group}", group)
                    .replace("{name}", name);
            var response = client.get(path).request();

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

    /**
     * Start a pipeline
     * Uses operationId: PipelineStart
     */
    public boolean startPipeline(String group, String name) {
        try {
            var op = getOperation("PipelineStart");
            var path = op.path()
                    .replace("{group}", group)
                    .replace("{name}", name);
            var response = client.post(path).request();

            return response.status() == Status.ACCEPTED_202;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error starting pipeline", e);
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
