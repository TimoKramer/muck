package muck;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.staticcontent.StaticContentService;
import muck.client.BobClient;
import muck.handlers.HomeHandler;
import muck.handlers.PipelineHandler;

import java.io.IOException;

public class Main {

    private static Configuration freemarkerConfig;
    private static BobClient bobClient;

    public static void main(String[] args) throws IOException {
        LogConfig.configureRuntime();

        // Load configuration
        Config config = Config.create();
        Config serverConfig = config.get("server");

        // Initialize Freemarker
        freemarkerConfig = createFreemarkerConfig();

        // Initialize Bob API client
        String bobUrl = config.get("bob.url")
                .asString()
                .orElse("http://localhost:7777");
        bobClient = new BobClient(bobUrl);

        // Build server
        WebServer server = WebServer.builder()
                .config(serverConfig)
                .routing(Main::routing)
                .build()
                .start();

        System.out.println("Muck - Bob CI/CD Monitor");
        System.out.println("WEB server is up! http://localhost:" + server.port());
    }

    static void routing(HttpRouting.Builder routing) {
        routing
                // Static content (CSS, JS, etc.)
                .register("/static", StaticContentService.builder("static")
                        .build())

                // Home page
                .get("/", new HomeHandler(freemarkerConfig, bobClient))

                // HTMX endpoint for pipeline list
                .get("/pipelines", new PipelineHandler(freemarkerConfig, bobClient))

                // Health check
                .get("/health", (req, res) -> res.send("OK"));
    }

    private static Configuration createFreemarkerConfig() throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
        cfg.setClassLoaderForTemplateLoading(Main.class.getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        return cfg;
    }

    public static Configuration getFreemarkerConfig() {
        return freemarkerConfig;
    }

    public static BobClient getBobClient() {
        return bobClient;
    }
}
