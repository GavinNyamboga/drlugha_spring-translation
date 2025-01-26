package drlugha.translator.shared.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String message = "Bad Request.";

    @Getter
    private List<String> errorList = new ArrayList<>();

    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
        this.message = message;
    }

    public BadRequestException(String message, List<String> errorList) {
        super(message);
        this.message = message;
        this.errorList = errorList;
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public BadRequestException(String message, Throwable cause) {
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
