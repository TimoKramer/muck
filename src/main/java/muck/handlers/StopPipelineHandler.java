package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class StopPipelineHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(StopPipelineHandler.class.getName());

    private final BobClient bobClient;

    public StopPipelineHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var runId = ValidationHelper.validateRun(req.query().get("run"));

            LOGGER.log(Level.INFO, "Stopping pipeline run: {0}", runId);

            var success = bobClient.stopPipeline(runId);

            if (success) {
                res.status(Status.NO_CONTENT_204);
                res.send();
            } else {
                res.status(Status.INTERNAL_SERVER_ERROR_500);
                res.send("Failed to stop pipeline run");
            }
        } catch (ValidationException e) {
            res.status(Status.BAD_REQUEST_400);
            res.send("Invalid request: " + e.getMessage());
        }
    }
}
