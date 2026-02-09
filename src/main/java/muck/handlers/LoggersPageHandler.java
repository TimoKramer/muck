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
import muck.client.BobClient;

public class LoggersPageHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(LoggersPageHandler.class.getName());

    private final Configuration freemarkerConfig;
    private final BobClient bobClient;

    public LoggersPageHandler(Configuration freemarkerConfig, BobClient bobClient) {
        this.freemarkerConfig = freemarkerConfig;
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var loggers = bobClient.listLoggers();

            var template = freemarkerConfig.getTemplate("loggers.ftl");

            var model = new HashMap<String, Object>();
            model.put("bobUrl", bobClient.getBaseUrl());
            model.put("loggers", loggers);
            model.put("connected", bobClient.checkHealth());

            var writer = new StringWriter();
            template.process(model, writer);

            res.status(Status.OK_200);
            res.header("Content-Type", "text/html; charset=utf-8");
            res.send(writer.toString());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error rendering loggers page", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error loading loggers: " + e.getMessage());
        }
    }
}
