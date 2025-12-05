package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class LogsStreamHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(LogsStreamHandler.class.getName());

    private final BobClient bobClient;

    public LogsStreamHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var run = req.query().get("run");
            try (var logResponse = bobClient.fetchLogs(run)) {

                if (logResponse.status() != Status.OK_200) {
                    res.status(logResponse.status());
                    res.send("Failed to fetch logs: " + logResponse.status());
                    return;
                }

                res.status(Status.OK_200);
                res.header("Content-Type", "text/plain; charset=utf-8");
                res.header("Cache-Control", "no-cache");
                res.header("Connection", "keep-alive");
                res.header("Transfer-Encoding", "chunked");
                res.header("X-Content-Type-Options", "nosniff");

                logResponse.entity().inputStream().transferTo(res.outputStream());

            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error streaming logs", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error loading logs: " + e.getMessage());
        }
    }
}
