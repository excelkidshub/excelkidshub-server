package in.excelkidshub.platform.common.exception;

/**
 * Exception thrown when a resource conflict occurs (e.g., duplicate entry).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
