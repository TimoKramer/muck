package muck.handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import muck.client.BobClient;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class DownloadPipelineDefinitionHandler implements Handler {
    private static final Logger LOGGER = Logger.getLogger(DownloadPipelineDefinitionHandler.class.getName());

    private final BobClient bobClient;

    public DownloadPipelineDefinitionHandler(BobClient bobClient) {
        this.bobClient = bobClient;
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) {
        try {
            var group = req.query().get("group");
            var name = req.query().get("name");

            if (group == null || name == null) {
                res.status(Status.BAD_REQUEST_400);
                res.send("Missing required parameters: group, name");
                return;
            }

            var pipelines = bobClient.listPipelines(group, name);

            if (pipelines.isEmpty()) {
                res.status(Status.NOT_FOUND_404);
                res.send("Pipeline not found");
                return;
            }

            var pipeline = pipelines.getFirst();
            var definition = pipeline.definition();

            if (definition.isEmpty()) {
                res.status(Status.NOT_FOUND_404);
                res.send("Pipeline definition not available");
                return;
            }

            var options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            var yaml = new Yaml(options);
            var yamlContent = yaml.dump(definition);

            res.status(Status.OK_200);
            res.header("Content-Type", "application/x-yaml");
            res.header("Content-Disposition", "attachment; filename=\"" + group + "-" + name + ".yaml\"");
            res.send(yamlContent);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error downloading pipeline definition", e);
            res.status(Status.INTERNAL_SERVER_ERROR_500);
            res.send("Error: " + e.getMessage());
        }
    }
}
