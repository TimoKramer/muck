package muck.handlers;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import freemarker.template.Configuration;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.cache.PipelineCache;
import muck.client.BobClient;

public class PipelinesHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(PipelinesHandler.class.getName());

    private final Configuration freemarkerConfig;
    private final BobClient bobClient;
    private final PipelineCache cache;

    public PipelinesHandler(Configuration freemarkerConfig, BobClient bobClient, PipelineCache cache) {
        this.freemarkerConfig = freemarkerConfig;
        this.bobClient = bobClient;
        this.cache = cache;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var pipelines = cache.getPipelines();
            var loggers = bobClient.listLoggers();

            var template = freemarkerConfig.getTemplate("pipelines.ftl");

            var model = new HashMap<String, Object>();
            model.put("bobUrl", bobClient.getBaseUrl());
            model.put("pipelines", pipelines);
            model.put("connected", cache.isHealthy());
            model.put("loggers", loggers);

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
