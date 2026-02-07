package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.common.uri.UriEncoding;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class UnpausePipelineHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(UnpausePipelineHandler.class.getName());

    private final BobClient bobClient;

    public UnpausePipelineHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        var group = UriEncoding.encodeUri(req.query().get("group"));
        var name = UriEncoding.encodeUri(req.query().get("name"));

        if (group == null || name == null) {
            res.status(Status.BAD_REQUEST_400);
            res.send("Missing group or name parameter");
            return;
        }

        LOGGER.log(Level.INFO, "Unpausing pipeline: {0}/{1}", new Object[] { group, name });

        var success = bobClient.unpausePipeline(group, name);

        if (success) {
            res.status(Status.NO_CONTENT_204);
            res.send();
        } else {
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Failed to unpause pipeline");
        }
    }
}
