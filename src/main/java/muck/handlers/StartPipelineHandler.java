package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.common.uri.UriEncoding;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.cache.CacheRefresher;
import muck.client.BobClient;

public class StartPipelineHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(StartPipelineHandler.class.getName());

    private final BobClient bobClient;
    private final String bobLogger;
    private final CacheRefresher cacheRefresher;

    public StartPipelineHandler(String bobLogger, BobClient bobClient, CacheRefresher cacheRefresher) {
        this.bobClient = bobClient;
        this.bobLogger = bobLogger;
        this.cacheRefresher = cacheRefresher;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        var group = UriEncoding.encodeUri(req.query().get("group"));
        var name = UriEncoding.encodeUri(req.query().get("name"));

        if (group == null || name == null || bobLogger == null) {
            res.status(Status.BAD_REQUEST_400);
            res.send("Missing parameter");
            return;
        }

        LOGGER.log(Level.INFO, "Starting pipeline group: {0} name: {1} logger: {2}",
                new Object[] { group, name, bobLogger });

        var success = bobClient.startPipeline(group, name, bobLogger);

        if (success) {
            cacheRefresher.triggerRefresh();
            res.status(Status.OK_200);
            res.header("HX-Redirect", "/runs?group=" + group + "&name=" + name);
            res.send("");
        } else {
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Failed to start pipeline");
        }
    }
}
