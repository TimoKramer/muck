package muck.client;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import muck.model.Pipeline;

import java.io.InputStream;
import java.util.ArrayList;
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
            InputStream specStream = getClass().getClassLoader()
                    .getResourceAsStream("api.yaml");
            if (specStream == null) {
                LOGGER.warning("Could not load api.yaml from resources");
                return null;
            }

            // Parse OpenAPI spec from classpath
            SwaggerParseResult result = new OpenAPIParser()
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
            Http1ClientResponse response = client.get("/api/pipelines")
                    .request();

            if (response.status() != Status.OK_200) {
                LOGGER.log(Level.WARNING, "Failed to fetch pipelines: {0}", response.status());
                return List.of();
            }

            // Parse response as list of maps
            List<Map<String, Object>> pipelinesData = response.as(List.class);
            List<Pipeline> pipelines = new ArrayList<>();

            for (Map<String, Object> data : pipelinesData) {
                String name = (String) data.get("name");
                String group = (String) data.get("group");
                if (name != null) {
                    pipelines.add(new Pipeline(group, name));
                }
            }

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
            String path = String.format("/api/pipelines/%s/%s/status", group, name);
            Http1ClientResponse response = client.get(path).request();

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
            String path = String.format("/api/pipelines/%s/%s/start", group, name);
            Http1ClientResponse response = client.post(path).request();

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
