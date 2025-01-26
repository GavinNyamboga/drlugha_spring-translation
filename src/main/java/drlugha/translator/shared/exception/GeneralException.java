package drlugha.translator.shared.exception;

public class GeneralException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public GeneralException() {
        super();
    }

    private String message = "Internal Server Error";


    public GeneralException(final String message, final Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public GeneralException(String message) {
        super(message);
        this.message = message;
    }


    public GeneralException(final Throwable cause) {
        super(cause);
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
