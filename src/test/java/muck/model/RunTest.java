package muck.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunTest {

    @Test
    void fullConstructor() {
        var r = new Run("passed", "build", "dev", "r-123",
                "2024-01-01T12:00:00Z", "2024-01-01T11:00:00Z", "local");
        assertEquals("passed", r.status());
        assertEquals("build", r.name());
        assertEquals("dev", r.group());
        assertEquals("r-123", r.runId());
        assertEquals("2024-01-01T12:00:00Z", r.completedAt());
        assertEquals("2024-01-01T11:00:00Z", r.scheduledAt());
        assertEquals("local", r.logger());
    }

    @Test
    void nullDefaults() {
        var r = new Run(null, null, null, null, null, null, null);
        assertEquals("unknown", r.status());
        assertEquals("", r.runId());
        assertEquals("", r.completedAt());
        assertEquals("", r.scheduledAt());
        assertEquals("", r.logger());
    }

    @Test
    void isComplete() {
        var complete = new Run("passed", "b", "d", "r-1",
                "2024-01-01T12:00:00Z", "2024-01-01T11:00:00Z", "l");
        assertTrue(complete.isComplete());

        var incomplete = new Run("running", "b", "d", "r-2",
                "", "2024-01-01T11:00:00Z", "l");
        assertFalse(incomplete.isComplete());
    }

    @Test
    void isSuccessful() {
        var passed = new Run("passed", "b", "d", "r-1", "", "", "l");
        assertTrue(passed.isSuccessful());

        var failed = new Run("failed", "b", "d", "r-2", "", "", "l");
        assertFalse(failed.isSuccessful());

        var running = new Run("running", "b", "d", "r-3", "", "", "l");
        assertFalse(running.isSuccessful());
    }

    @Test
    void isSuccessfulCaseInsensitive() {
        var passed = new Run("PASSED", "b", "d", "r-1", "", "", "l");
        assertTrue(passed.isSuccessful());
    }
}
