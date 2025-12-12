package muck.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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

                // Does abort the stream at some point
                // logResponse.entity().inputStream().transferTo(res.outputStream());

                try (var reader = new BufferedReader(
                        new InputStreamReader(logResponse.entity().inputStream(), StandardCharsets.UTF_8));
                        var writer = new BufferedWriter(
                                new OutputStreamWriter(res.outputStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error streaming logs", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error loading logs: " + e.getMessage());
        }
    }
}
