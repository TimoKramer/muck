package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import muck.client.BobClient;

public class CreateArtifactStoreHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(CreateArtifactStoreHandler.class.getName());

    private final BobClient bobClient;

    public CreateArtifactStoreHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var params = req.content().as(io.helidon.common.parameters.Parameters.class);

            var name = params.first("name").orElse("");
            var url = params.first("url").orElse("");

            if (name.isBlank() || url.isBlank()) {
                res.status(Status.BAD_REQUEST_400);
                res.send("Name and URL are required");
                return;
            }

            var body = Json.createObjectBuilder()
                    .add("name", name)
                    .add("url", url)
                    .build()
                    .toString();

            LOGGER.log(Level.INFO, "Creating artifact store: {0}", name);

            var success = bobClient.createArtifactStore(body);

            if (success) {
                res.status(Status.OK_200);
                res.send("Artifact store created");
            } else {
                res.status(Status.INTERNAL_SERVER_ERROR_500);
                res.send("Failed to create artifact store");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating artifact store", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error: " + e.getMessage());
        }
    }
}
