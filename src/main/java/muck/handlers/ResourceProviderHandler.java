package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.helidon.common.uri.UriEncoding;
import io.helidon.http.Method;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class ResourceProviderHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(ResourceProviderHandler.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final BobClient bobClient;

    public ResourceProviderHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var method = req.prologue().method();

            if (method == Method.GET) {
                handleList(res);
            } else if (method == Method.POST) {
                handleCreate(req, res);
            } else if (method == Method.DELETE) {
                handleDelete(req, res);
            } else {
                res.status(Status.METHOD_NOT_ALLOWED_405);
                res.send("Method not allowed");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling resource provider request", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleList(ServerResponse res) throws Exception {
        var providers = bobClient.listResourceProviders();

        res.status(Status.OK_200);
        res.header("Content-Type", "application/json");
        res.send(MAPPER.writeValueAsString(providers));
    }

    private void handleCreate(ServerRequest req, ServerResponse res) {
        var body = req.content().as(String.class);

        if (body == null || body.isBlank()) {
            res.status(Status.BAD_REQUEST_400);
            res.send("{\"message\": \"Request body is required\"}");
            return;
        }

        LOGGER.log(Level.INFO, "Creating resource provider");

        var success = bobClient.createResourceProvider(body);

        if (success) {
            res.status(Status.ACCEPTED_202);
            res.send("{\"message\": \"Resource provider created\"}");
        } else {
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("{\"message\": \"Failed to create resource provider\"}");
        }
    }

    private void handleDelete(ServerRequest req, ServerResponse res) {
        var name = UriEncoding.encodeUri(req.query().get("name"));

        if (name == null) {
            res.status(Status.BAD_REQUEST_400);
            res.send("{\"message\": \"Name parameter is required\"}");
            return;
        }

        LOGGER.log(Level.INFO, "Deleting resource provider: {0}", name);

        var success = bobClient.deleteResourceProvider(name);

        if (success) {
            res.status(Status.NO_CONTENT_204);
            res.send();
        } else {
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("{\"message\": \"Failed to delete resource provider\"}");
        }
    }
}
