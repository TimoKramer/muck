package muck.handlers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationHelperTest {

    @Test
    void validateRunAcceptsValidRunId() throws ValidationException {
        var result = ValidationHelper.validateRun("r-550e8400-e29b-41d4-a716-446655440000");
        assertEquals("r-550e8400-e29b-41d4-a716-446655440000", result);
    }

    @Test
    void validateRunRejectsNull() {
        assertThrows(ValidationException.class, () -> ValidationHelper.validateRun(null));
    }

    @Test
    void validateRunRejectsMissingPrefix() {
        assertThrows(ValidationException.class,
                () -> ValidationHelper.validateRun("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void validateRunRejectsInvalidUuid() {
        assertThrows(ValidationException.class,
                () -> ValidationHelper.validateRun("r-not-a-uuid"));
    }

    @Test
    void validatePipelineIdAcceptsValid() throws ValidationException {
        assertEquals("my-pipeline", ValidationHelper.validatePipelineId("my-pipeline", "name"));
        assertEquals("build_v2", ValidationHelper.validatePipelineId("build_v2", "name"));
        assertEquals("Prod01", ValidationHelper.validatePipelineId("Prod01", "name"));
    }

    @Test
    void validatePipelineIdRejectsNull() {
        assertThrows(ValidationException.class,
                () -> ValidationHelper.validatePipelineId(null, "name"));
    }

    @Test
    void validatePipelineIdRejectsBlank() {
        assertThrows(ValidationException.class,
                () -> ValidationHelper.validatePipelineId("", "name"));
        assertThrows(ValidationException.class,
                () -> ValidationHelper.validatePipelineId("   ", "name"));
    }

    @Test
    void validatePipelineIdRejectsSpecialChars() {
        assertThrows(ValidationException.class,
                () -> ValidationHelper.validatePipelineId("my pipeline", "name"));
        assertThrows(ValidationException.class,
                () -> ValidationHelper.validatePipelineId("build/deploy", "name"));
        assertThrows(ValidationException.class,
                () -> ValidationHelper.validatePipelineId("name@host", "name"));
    }

    @Test
    void validatePipelineIdErrorIncludesFieldName() {
        var ex = assertThrows(ValidationException.class,
                () -> ValidationHelper.validatePipelineId("", "group"));
        assertTrue(ex.getMessage().contains("group"));
    }
}
