package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.common.uri.UriEncoding;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class DeletePipelineHandler implements Handler {

    private static final Logger LOGGER = Logger.getLogger(DeletePipelineHandler.class.getName());

    private final BobClient bobClient;

    public DeletePipelineHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        var group = UriEncoding.encodeUri(req.query().get("group"));
        var name = UriEncoding.encodeUri(req.query().get("name"));

        if (group == null || name == null) {
            res.status(Status.BAD_REQUEST_400);
            res.send("Missing parameter");
            return;
        }

        LOGGER.log(Level.INFO, "Deleting pipeline group: {0} name: {1}",
                new Object[] { group, name });

        var success = bobClient.deletePipeline(group, name);

        if (success) {
            res.status(Status.NO_CONTENT_204);
            res.send();
        } else {
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Failed to delete pipeline");
        }
    }
}
