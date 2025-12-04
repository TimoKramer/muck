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
import muck.handlers.LogsHandler;
import muck.handlers.LogsStreamHandler;
import muck.handlers.PipelineHandler;
import muck.handlers.RunsHandler;

import java.io.IOException;

public class Main {

    private static Configuration freemarkerConfig;
    private static BobClient bobClient;

    public static void main(String[] args) throws IOException {
        LogConfig.configureRuntime();

        var config = Config.create();
        var serverConfig = config.get("server");

        freemarkerConfig = createFreemarkerConfig();

        var bobUrl = config.get("bob.url")
                .asString()
                .orElse("http://localhost:7777");
        bobClient = new BobClient(bobUrl);

        var server = WebServer.builder()
                .config(serverConfig)
                .routing(Main::routing)
                .build()
                .start();

        System.out.println("Muck - Bob CI/CD Monitor");
        System.out.println("WEB server is up! http://localhost:" + server.port());
    }

    static void routing(HttpRouting.Builder routing) {
        routing
                .register("/static", StaticContentService.builder("static")
                        .build())

                .get("/", new HomeHandler(freemarkerConfig, bobClient))

                .get("/pipelines", new PipelineHandler(freemarkerConfig, bobClient))

                .get("/runs/group/{group}/name/{name}", new RunsHandler(freemarkerConfig, bobClient))

                .get("/logs/stream/run/{run}", new LogsStreamHandler(bobClient))

                .get("/health", (req, res) -> res.send("OK"));
    }

    private static Configuration createFreemarkerConfig() throws IOException {
        var cfg = new Configuration(Configuration.VERSION_2_3_33);
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
