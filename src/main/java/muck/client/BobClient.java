package muck.client;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import muck.model.Pipeline;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for interacting with the Bob CI/CD REST API.
 * Validates requests against the OpenAPI specification.
 */
public class BobClient {
    private static final Logger LOGGER = Logger.getLogger(BobClient.class.getName());

    private final String baseUrl;
    private final Http1Client client;
    private final OpenAPI openApiSpec;

    public BobClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = Http1Client.builder()
                .baseUri(baseUrl)
                .build();
        this.openApiSpec = loadOpenApiSpec();
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
     * Fetch all pipelines from Bob API
     * GET /api/pipelines
     */
    public List<Pipeline> listPipelines() {
        try {
            var response = client.get("/api/pipelines").request();

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch pipelines: {0}", response.status());
                return List.of();
            }

            // Parse response as list of maps
            List<Map<String, Object>> pipelinesData = response.as(List.class);
            var pipelines = pipelinesData.stream()
                    .filter(data -> data.get("name") != null)
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
     * GET /api/pipelines/{group}/{name}/status
     */
    public String getPipelineStatus(String group, String name) {
        try {
            var path = "/api/pipelines/%s/%s/status".formatted(group, name);
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
     * POST /api/pipelines/{group}/{name}/start
     */
    public boolean startPipeline(String group, String name) {
        try {
            var path = "/api/pipelines/%s/%s/start".formatted(group, name);
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
