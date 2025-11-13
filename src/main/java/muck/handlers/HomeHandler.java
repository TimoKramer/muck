package muck.handlers;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for the home page (/)
 * Renders the main layout with HTMX setup
 */
public class HomeHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(HomeHandler.class.getName());

    private final Configuration freemarkerConfig;
    private final BobClient bobClient;

    public HomeHandler(Configuration freemarkerConfig, BobClient bobClient) {
        this.freemarkerConfig = freemarkerConfig;
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var template = freemarkerConfig.getTemplate("index.ftl");

            var model = Map.of(
                    "title", "Muck - Bob CI/CD Monitor",
                    "bobUrl", bobClient.getBaseUrl()
            );

            var writer = new StringWriter();
            template.process(model, writer);

            res.status(Status.OK_200);
            res.header("Content-Type", "text/html; charset=utf-8");
            res.send(writer.toString());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error rendering home page", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error rendering page: " + e.getMessage());
        }
    }
}
