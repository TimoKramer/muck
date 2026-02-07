package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.common.uri.UriEncoding;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;

public class FetchArtifactHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(FetchArtifactHandler.class.getName());

    private final BobClient bobClient;

    public FetchArtifactHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var group = UriEncoding.encodeUri(req.query().get("group"));
            var name = UriEncoding.encodeUri(req.query().get("name"));
            var runId = ValidationHelper.validateRun(req.query().get("run"));
            var store = UriEncoding.encodeUri(req.query().get("store"));
            var artifact = UriEncoding.encodeUri(req.query().get("artifact"));

            if (group == null || name == null || store == null || artifact == null) {
                res.status(Status.BAD_REQUEST_400);
                res.send("Missing required parameters");
                return;
            }

            LOGGER.log(Level.INFO, "Fetching artifact: {0}/{1}/{2}/{3}/{4}",
                    new Object[] { group, name, runId, store, artifact });

            try (var response = bobClient.fetchArtifact(group, name, runId, store, artifact)) {
                if (response.status() != Status.OK_200) {
                    res.status(response.status());
                    res.send("Failed to fetch artifact");
                    return;
                }

                res.status(Status.OK_200);
                res.header("Content-Type", "application/tar");
                res.header("Content-Disposition", "attachment; filename=\"" + artifact + ".tar\"");

                response.entity().inputStream().transferTo(res.outputStream());
            }
        } catch (ValidationException e) {
            res.status(Status.BAD_REQUEST_400);
            res.send("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching artifact", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error fetching artifact: " + e.getMessage());
        }
    }
}
