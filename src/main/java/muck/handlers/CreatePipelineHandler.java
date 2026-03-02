package muck.handlers;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import muck.client.BobClient;
import org.yaml.snakeyaml.Yaml;

public class CreatePipelineHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(CreatePipelineHandler.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final BobClient bobClient;

    public CreatePipelineHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var contentType = req.headers().contentType().orElse(null);

            String body;
            if (contentType != null && contentType.text().contains("yaml")) {
                body = handleYamlUpload(req);
            } else if (contentType != null && contentType.text().contains("json")) {
                body = req.content().as(String.class);
            } else {
                body = handleFormSubmission(req);
            }

            if (body == null) {
                res.status(Status.BAD_REQUEST_400);
                res.send("Missing required fields");
                return;
            }

            LOGGER.log(Level.INFO, "Creating pipeline: {0}", body);

            var success = bobClient.createPipeline(body);

            if (success) {
                res.status(Status.OK_200);
                res.send("Pipeline created");
            } else {
                res.status(Status.INTERNAL_SERVER_ERROR_500);
                res.send("Failed to create pipeline");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating pipeline", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error: " + e.getMessage());
        }
    }

    private String handleFormSubmission(ServerRequest req) {
        var params = req.content().as(io.helidon.common.parameters.Parameters.class);

        var group = params.first("group").orElse("");
        var name = params.first("name").orElse("");
        var image = params.first("image").orElse("");
        var stepsText = params.first("steps").orElse("");
        var varsText = params.first("vars").orElse("");

        if (group.isBlank() || name.isBlank() || image.isBlank() || stepsText.isBlank()) {
            return null;
        }

        JsonObjectBuilder pipeline = Json.createObjectBuilder()
                .add("group", group)
                .add("name", name)
                .add("image", image);

        JsonArrayBuilder stepsArray = Json.createArrayBuilder();
        for (String line : stepsText.split("\n")) {
            String cmd = line.trim();
            if (!cmd.isEmpty()) {
                stepsArray.add(Json.createObjectBuilder().add("cmd", cmd));
            }
        }
        pipeline.add("steps", stepsArray);

        if (!varsText.isBlank()) {
            JsonObjectBuilder varsObj = Json.createObjectBuilder();
            for (String line : varsText.split("\n")) {
                String trimmed = line.trim();
                int eqIdx = trimmed.indexOf('=');
                if (eqIdx > 0) {
                    String key = trimmed.substring(0, eqIdx).trim();
                    String value = trimmed.substring(eqIdx + 1).trim();
                    varsObj.add(key, value);
                }
            }
            pipeline.add("vars", varsObj);
        }

        return pipeline.build().toString();
    }

    private String handleYamlUpload(ServerRequest req) throws Exception {
        var content = req.content().as(String.class);
        if (content == null || content.isBlank()) {
            return null;
        }
        var yaml = new Yaml();
        Map<String, Object> parsed = yaml.load(content);
        var pipeline = parsed.containsKey("spec") ? (Map<String, Object>) parsed.get("spec") : parsed;
        return MAPPER.writeValueAsString(pipeline);
    }
}
