package muck.handlers;

import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import muck.model.Pipeline;

import freemarker.template.Configuration;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class PipelinesHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(PipelinesHandler.class.getName());

    private final Configuration freemarkerConfig;
    private final BobClient bobClient;

    public PipelinesHandler(Configuration freemarkerConfig, BobClient bobClient) {
        this.freemarkerConfig = freemarkerConfig;
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var sortField = req.query().first("sort").orElse("name");
            var sortDir = req.query().first("dir").orElse("asc");

            Comparator<Pipeline> comparator = switch (sortField) {
                case "group" -> Comparator.comparing(Pipeline::group);
                case "status" -> Comparator.comparing(Pipeline::status);
                default -> Comparator.comparing(Pipeline::name);
            };
            if ("desc".equals(sortDir)) {
                comparator = comparator.reversed();
            }

            var pipelines = bobClient.listPipelinesWithStatus().stream().sorted(comparator).toList();
            var loggers = bobClient.listLoggers();

            var template = freemarkerConfig.getTemplate("pipelines.ftl");

            var model = new HashMap<String, Object>();
            model.put("bobUrl", bobClient.getBaseUrl());
            model.put("pipelines", pipelines);
            model.put("sortField", sortField);
            model.put("sortDir", sortDir);
            model.put("connected", bobClient.checkHealth());
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
