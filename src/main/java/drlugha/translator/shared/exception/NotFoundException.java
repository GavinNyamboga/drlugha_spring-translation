package drlugha.translator.shared.exception;

public final class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public NotFoundException() {
        super();
    }

    private String message = "Not Found Error";


    public NotFoundException(final String message, final Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public NotFoundException(String message) {
        super(message);
        this.message = message;
    }


    public NotFoundException(final Throwable cause) {
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
