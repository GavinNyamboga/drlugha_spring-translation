package drlugha.translator.shared.exception;

/**
 * The com.setup.generic.exception InvalidUserException.java
 *
 * @author Daniel Mboya
 * @version 1.0.0
 * @since Nov 11, 2018 3:29:26 PM
 */
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message = "Unauthorized Error";

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
        this.message = message;
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
