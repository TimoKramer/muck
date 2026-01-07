package muck.handlers;

import java.io.StringWriter;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import freemarker.template.Configuration;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.cache.PipelineCache;
import muck.client.BobClient;

public class LogsHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(LogsHandler.class.getName());

    private final Configuration freemarkerConfig;
    private final BobClient bobClient;
    private final PipelineCache cache;

    public LogsHandler(Configuration freemarkerConfig, BobClient bobClient, PipelineCache cache) {
        this.freemarkerConfig = freemarkerConfig;
        this.bobClient = bobClient;
        this.cache = cache;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var run = ValidationHelper.validateRun(req.query().get("run"));
            var group = ValidationHelper.validatePipelineId(req.query().get("group"), "group");
            var name = ValidationHelper.validatePipelineId(req.query().get("name"), "name");

            var template = freemarkerConfig.getTemplate("logs.ftl");

            var model = Map.of(
                    "bobUrl", bobClient.getBaseUrl(),
                    "run", run,
                    "group", group,
                    "name", name,
                    "connected", cache.isHealthy());

            var writer = new StringWriter();
            template.process(model, writer);

            res.status(Status.OK_200);
            res.header("Content-Type", "text/html; charset=utf-8");
            res.send(writer.toString());

        } catch (ValidationException e) {
            res.status(Status.BAD_REQUEST_400);
            res.send("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching/rendering logs", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("<div class='error'>Error loading logs: " + e.getMessage() + "</div>");
        }
    }
}
