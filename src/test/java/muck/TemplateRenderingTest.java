package muck;

import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import freemarker.template.Configuration;
import muck.model.Pipeline;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the FreeMarker config + template + record contract.
 *
 * <p>{@link muck.model.Pipeline} is a Java record, so templates rely on FreeMarker exposing its
 * accessors ({@code group()}, {@code name()}, ...) as properties ({@code pipeline.group}). That
 * support was silently lost once and produced a "method+sequence" render error; this test fails
 * loudly if the FreeMarker config ever stops resolving record accessors as properties.
 */
class TemplateRenderingTest {

    private static Configuration cfg;

    @BeforeAll
    static void setUp() throws Exception {
        cfg = Main.createFreemarkerConfig(false, Locale.ENGLISH);
    }

    @Test
    void resolvesRecordAccessorsAsProperties() throws Exception {
        var template = new freemarker.template.Template(
                "inline", "[${pipeline.group}/${pipeline.name}:${pipeline.status}]", cfg);

        var writer = new StringWriter();
        template.process(Map.of("pipeline", new Pipeline("dev", "build", "passed", Map.of())), writer);

        assertTrue(writer.toString().equals("[dev/build:passed]"),
                "Record accessors should resolve as properties, got: " + writer);
    }

    @Test
    void rendersPipelinesTemplateWithRecordList() throws Exception {
        var template = cfg.getTemplate("pipelines.ftl");

        var pipeline = new Pipeline("dev", "build", "passed",
                Map.of("group", "dev", "name", "build", "image", "alpine"));

        var model = Map.<String, Object>of(
                "bobUrl", "http://localhost:7777",
                "connected", true,
                "loggers", List.of(),
                "pipelines", List.of(pipeline));

        var writer = new StringWriter();
        template.process(model, writer);

        var html = writer.toString();
        assertTrue(html.contains("build"), "Pipeline name should appear in rendered output");
        assertTrue(html.contains("passed"), "Pipeline status should appear in rendered output");
    }
}
