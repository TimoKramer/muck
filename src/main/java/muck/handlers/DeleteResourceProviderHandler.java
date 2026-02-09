package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.common.uri.UriEncoding;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class DeleteResourceProviderHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(DeleteResourceProviderHandler.class.getName());

    private final BobClient bobClient;

    public DeleteResourceProviderHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        var name = UriEncoding.encodeUri(req.query().get("name"));

        if (name == null || name.isBlank()) {
            res.status(Status.BAD_REQUEST_400);
            res.send("Name parameter is required");
            return;
        }

        LOGGER.log(Level.INFO, "Deleting resource provider: {0}", name);

        var success = bobClient.deleteResourceProvider(name);

        if (success) {
            res.status(Status.NO_CONTENT_204);
            res.send();
        } else {
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Failed to delete resource provider");
        }
    }
}
