package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import muck.client.BobClient;

public class CreateResourceProviderHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(CreateResourceProviderHandler.class.getName());

    private final BobClient bobClient;

    public CreateResourceProviderHandler(BobClient bobClient) {
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

            LOGGER.log(Level.INFO, "Creating resource provider: {0}", name);

            var success = bobClient.createResourceProvider(body);

            if (success) {
                res.status(Status.OK_200);
                res.send("Resource provider created");
            } else {
                res.status(Status.INTERNAL_SERVER_ERROR_500);
                res.send("Failed to create resource provider");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating resource provider", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error: " + e.getMessage());
        }
    }
}
