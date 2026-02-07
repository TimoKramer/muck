package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
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
            var body = req.content().as(String.class);

            if (body == null || body.isBlank()) {
                res.status(Status.BAD_REQUEST_400);
                res.send("Request body is required");
                return;
            }

            LOGGER.log(Level.INFO, "Creating pipeline");

            var success = bobClient.createPipeline(body);

            if (success) {
                res.status(Status.ACCEPTED_202);
                res.send("{\"message\": \"Pipeline created\"}");
            } else {
                res.status(Status.INTERNAL_SERVER_ERROR_500);
                res.send("{\"message\": \"Failed to create pipeline\"}");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating pipeline", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }
}
