package muck.handlers;

import java.util.UUID;

class ValidationException extends Exception {
    public ValidationException(String errorMessage) {
        super(errorMessage);
    }
}

public class ValidationHelper {

    public static String validateRun(String run) throws ValidationException {
        if (run == null || !run.startsWith("r-")) {
            throw new ValidationException("Validation of run id failed");
        }
        try {
            UUID.fromString(run.substring(2));
            return run;
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Validation of run id failed");
        }
    }

}
