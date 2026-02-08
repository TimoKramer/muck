package muck.handlers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import muck.client.BobClient;

public class CreatePipelineHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(CreatePipelineHandler.class.getName());

    private final BobClient bobClient;

    public CreatePipelineHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var params = req.content().as(io.helidon.common.parameters.Parameters.class);

            var group = params.first("group").orElse("");
            var name = params.first("name").orElse("");
            var image = params.first("image").orElse("");
            var stepsText = params.first("steps").orElse("");
            var varsText = params.first("vars").orElse("");

            if (group.isBlank() || name.isBlank() || image.isBlank() || stepsText.isBlank()) {
                res.status(Status.BAD_REQUEST_400);
                res.send("Missing required fields");
                return;
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

            // Parse environment variables (KEY=VALUE per line)
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

            String body = pipeline.build().toString();
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
}
