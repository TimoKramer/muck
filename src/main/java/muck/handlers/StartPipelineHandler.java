package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class StartPipelineHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(StartPipelineHandler.class.getName());

    private final BobClient bobClient;

    public StartPipelineHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        var group = req.query().get("group");
        var name = req.query().get("name");

        if (group == null || name == null) {
            res.status(Status.BAD_REQUEST_400);
            res.send("Missing group or name parameter");
            return;
        }

        LOGGER.log(Level.INFO, "Starting pipeline {0}/{1}", new Object[]{group, name});

        var success = bobClient.startPipeline(group, name);

        if (success) {
            res.status(Status.OK_200);
            res.header("HX-Redirect", "/runs?group=" + group + "&name=" + name);
            res.send("");
        } else {
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Failed to start pipeline");
        }
    }
}
