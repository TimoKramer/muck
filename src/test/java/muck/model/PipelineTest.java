package muck.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    @Test
    void fullConstructor() {
        var p = new Pipeline("dev", "build", "running");
        assertEquals("dev", p.group());
        assertEquals("build", p.name());
        assertEquals("running", p.status());
    }

    @Test
    void twoArgConstructor() {
        var p = new Pipeline("dev", "build");
        assertEquals("dev", p.group());
        assertEquals("build", p.name());
        assertEquals("unknown", p.status());
    }

    @Test
    void nullGroupDefaultsToDefault() {
        var p = new Pipeline(null, "build", "running");
        assertEquals("default", p.group());
    }

    @Test
    void nullStatusDefaultsToUnknown() {
        var p = new Pipeline("dev", "build", null);
        assertEquals("unknown", p.status());
    }

    @Test
    void fullName() {
        var p = new Pipeline("dev", "build");
        assertEquals("dev/build", p.fullName());
    }

    @Test
    void withStatus() {
        var p = new Pipeline("dev", "build", "unknown");
        var updated = p.withStatus("passed");
        assertEquals("passed", updated.status());
        assertEquals("dev", updated.group());
        assertEquals("build", updated.name());
        // Original is unchanged (immutable)
        assertEquals("unknown", p.status());
    }
}
