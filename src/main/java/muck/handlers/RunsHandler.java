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

public class RunsHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(RunsHandler.class.getName());

    private final Configuration freemarkerConfig;
    private final BobClient bobClient;
    private final PipelineCache cache;

    public RunsHandler(Configuration freemarkerConfig, BobClient bobClient, PipelineCache cache) {
        this.freemarkerConfig = freemarkerConfig;
        this.bobClient = bobClient;
        this.cache = cache;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var group = ValidationHelper.validatePipelineId(req.query().get("group"), "group");
            var name = ValidationHelper.validatePipelineId(req.query().get("name"), "name");

            var runs = cache.getRuns(group, name);

            var template = freemarkerConfig.getTemplate("runs.ftl");

            var model = Map.of(
                    "bobUrl", bobClient.getBaseUrl(),
                    "runs", runs,
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
        } catch (NoSuchElementException e) {
            LOGGER.log(Level.INFO, "Requested run not found");
            res.status(Status.NOT_FOUND_404);
            res.send("Requested run not found");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching/rendering runs", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("<div class='error'>Error loading runs: " + e.getMessage() + "</div>");
        }
    }
}
