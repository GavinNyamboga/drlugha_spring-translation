package drlugha.translator.shared.controller;

/**
 * @author Gavin
 * <br>
 * Created At: 25/01/2025
 */
public class SuccessResponse {

    private boolean success;

    private String message;

    public SuccessResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public SuccessResponse() {
    }
}
