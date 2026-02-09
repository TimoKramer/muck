package muck;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.staticcontent.StaticContentService;
import muck.client.BobClient;
import muck.handlers.ArtifactStoresPageHandler;
import muck.handlers.CreateArtifactStoreHandler;
import muck.handlers.CreateLoggerHandler;
import muck.handlers.CreatePipelineHandler;
import muck.handlers.CreateResourceProviderHandler;
import muck.handlers.DeleteArtifactStoreHandler;
import muck.handlers.DeleteLoggerHandler;
import muck.handlers.DeletePipelineHandler;
import muck.handlers.DeleteResourceProviderHandler;
import muck.handlers.FetchArtifactHandler;
import muck.handlers.HomeHandler;
import muck.handlers.LoggersPageHandler;
import muck.handlers.LogsHandler;
import muck.handlers.LogsStreamHandler;
import muck.handlers.PausePipelineHandler;
import muck.handlers.PipelinesHandler;
import muck.handlers.ResourceProvidersPageHandler;
import muck.handlers.RunsHandler;
import muck.handlers.StartPipelineHandler;
import muck.handlers.StopPipelineHandler;
import muck.handlers.UnpausePipelineHandler;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static Configuration freemarkerConfig;
    private static BobClient bobClient;
    private static String bobLogger;

    public static void main(String[] args) throws IOException {
        try {
            LogConfig.configureRuntime();

            var config = Config.create();
            var devMode = config.get("dev-mode").asBoolean().orElse(false);
            var serverConfig = config.get("server");
            var locale = Locale.forLanguageTag(config.get("locale").asString().orElse("en_us"));

            freemarkerConfig = createFreemarkerConfig(devMode);
            freemarkerConfig.setLocale(locale);
            freemarkerConfig.setOutputFormat(HTMLOutputFormat.INSTANCE);

            var bobUrl = config.get("bob.url")
                    .asString()
                    .orElseThrow();
            bobClient = new BobClient(bobUrl);

            bobLogger = config.get("bob.logger")
                    .asString()
                    .orElseThrow();

            var server = WebServer.builder()
                    .config(serverConfig)
                    .routing(Main::routing)
                    .build()
                    .start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutdown initiated...");
                server.stop();
            }));

            LOGGER.info("Muck - Bob CI/CD Monitor");
            LOGGER.info("WEB server is up! http://localhost:" + server.port());

        } catch (Exception e) {
            LOGGER.severe("Unable to start Muck: " + e.getMessage());
        }
    }

    static void routing(HttpRouting.Builder routing) {
        routing
                .register("/static", StaticContentService.builder("static")
                        .build())

                // Pipelines
                .get("/", new HomeHandler(freemarkerConfig, bobClient))
                .get("/pipelines", new PipelinesHandler(freemarkerConfig, bobClient))
                .get("/runs", new RunsHandler(freemarkerConfig, bobClient))
                .post("/start", new StartPipelineHandler(bobLogger, bobClient))
                .post("/create", new CreatePipelineHandler(bobClient))
                .delete("/delete", new DeletePipelineHandler(bobClient))

                // Pipeline run controls
                .post("/run/stop", new StopPipelineHandler(bobClient))
                .post("/run/pause", new PausePipelineHandler(bobClient))
                .post("/run/unpause", new UnpausePipelineHandler(bobClient))

                // Logs
                .get("/logs", new LogsHandler(freemarkerConfig, bobClient))
                .get("/logs/stream", new LogsStreamHandler(bobClient))

                // Artifacts
                .get("/artifact", new FetchArtifactHandler(bobClient))

                // Loggers
                .get("/loggers", new LoggersPageHandler(freemarkerConfig, bobClient))
                .post("/loggers", new CreateLoggerHandler(bobClient))
                .delete("/loggers", new DeleteLoggerHandler(bobClient))

                // Resource Providers
                .get("/resource-providers", new ResourceProvidersPageHandler(freemarkerConfig, bobClient))
                .post("/resource-providers", new CreateResourceProviderHandler(bobClient))
                .delete("/resource-providers", new DeleteResourceProviderHandler(bobClient))

                // Artifact Stores
                .get("/artifact-stores", new ArtifactStoresPageHandler(freemarkerConfig, bobClient))
                .post("/artifact-stores", new CreateArtifactStoreHandler(bobClient))
                .delete("/artifact-stores", new DeleteArtifactStoreHandler(bobClient))

                // Health
                .get("/health", (req, res) -> res.send("OK"));
    }

    private static Configuration createFreemarkerConfig(boolean devMode) throws IOException {
        var cfg = new Configuration(Configuration.VERSION_2_3_33);
        if (devMode) {
            cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
            cfg.setTemplateUpdateDelayMilliseconds(0);
        } else {
            cfg.setClassLoaderForTemplateLoading(Main.class.getClassLoader(), "templates");
        }
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        return cfg;
    }
}
