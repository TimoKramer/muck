package muck;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
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
import muck.cache.CacheRefresher;
import muck.cache.PipelineCache;
import muck.client.BobClient;
import muck.handlers.HomeHandler;
import muck.handlers.LogsHandler;
import muck.handlers.LogsStreamHandler;
import muck.handlers.PipelinesHandler;
import muck.handlers.RunsHandler;
import muck.handlers.StartPipelineHandler;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static Configuration freemarkerConfig;
    private static BobClient bobClient;
    private static String bobLogger;
    private static PipelineCache pipelineCache;
    private static CacheRefresher cacheRefresher;

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

            pipelineCache = new PipelineCache();

            var refreshIntervalSeconds = config.get("cache.refresh-interval-seconds")
                    .asInt()
                    .orElse(5);
            var blockOnStartup = config.get("cache.block-on-startup")
                    .asBoolean()
                    .orElse(true);

            cacheRefresher = new CacheRefresher(
                    bobClient,
                    pipelineCache,
                    Duration.ofSeconds(refreshIntervalSeconds));
            cacheRefresher.start(blockOnStartup);

            var server = WebServer.builder()
                    .config(serverConfig)
                    .routing(Main::routing)
                    .build()
                    .start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutdown initiated...");
                cacheRefresher.close();
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

                .get("/", new HomeHandler(freemarkerConfig, bobClient, pipelineCache))

                .get("/pipelines", new PipelinesHandler(freemarkerConfig, bobClient, pipelineCache))

                .get("/runs", new RunsHandler(freemarkerConfig, bobClient, pipelineCache))

                .get("/logs", new LogsHandler(freemarkerConfig, bobClient, pipelineCache))

                .get("/logs/stream", new LogsStreamHandler(bobClient))

                .post("/start", new StartPipelineHandler(bobLogger, bobClient, cacheRefresher))

                .delete("/delete", new DeletePipelineHandler(bobClient, cacheRefresher))

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
