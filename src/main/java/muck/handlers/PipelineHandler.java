package muck.handlers;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;
import muck.model.Pipeline;

import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for the /pipelines endpoint
 * Returns HTML fragment with pipeline list for HTMX
 */
public class PipelineHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(PipelineHandler.class.getName());

    private final Configuration freemarkerConfig;
    private final BobClient bobClient;

    public PipelineHandler(Configuration freemarkerConfig, BobClient bobClient) {
        this.freemarkerConfig = freemarkerConfig;
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            // Fetch pipelines from Bob API
            var pipelines = bobClient.listPipelines();

            // Fetch status for each pipeline (optional - can be slow)
            // Uncomment if you want to show status with real-time data:
            // var pipelinesWithStatus = pipelines.stream()
            //     .map(p -> p.withStatus(
            //         bobClient.getPipelineStatus(p.group(), p.name())))
            //     .toList();

            var template = freemarkerConfig.getTemplate("pipelines.ftl");

            var model = Map.of("pipelines", pipelines);

            var writer = new StringWriter();
            template.process(model, writer);

            res.status(Status.OK_200);
            res.header("Content-Type", "text/html; charset=utf-8");
            res.send(writer.toString());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching/rendering pipelines", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("<div class='error'>Error loading pipelines: " + e.getMessage() + "</div>");
        }
    }
}
