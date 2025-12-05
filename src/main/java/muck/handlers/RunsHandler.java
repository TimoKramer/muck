package muck.handlers;

import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import freemarker.template.Configuration;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class RunsHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(RunsHandler.class.getName());

    private final Configuration freemarkerConfig;
    private final BobClient bobClient;

    public RunsHandler(Configuration freemarkerConfig, BobClient bobClient) {
        this.freemarkerConfig = freemarkerConfig;
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var group = req.path().pathParameters().get("group");
            var name = req.path().pathParameters().get("name");

            var runs = bobClient.listRuns(group, name);

            var template = freemarkerConfig.getTemplate("runs.ftl");

            var model = Map.of(
                    "bobUrl", bobClient.getBaseUrl(),
                    "runs", runs,
                    "group", group,
                    "name", name);

            var writer = new StringWriter();
            template.process(model, writer);

            res.status(Status.OK_200);
            res.header("Content-Type", "text/html; charset=utf-8");
            res.send(writer.toString());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching/rendering runs", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("<div class='error'>Error loading runs: " + e.getMessage() + "</div>");
        }
    }
}
