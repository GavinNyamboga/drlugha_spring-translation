package drlugha.translator.shared.controller;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {


    public ResponseEntity<SuccessResponse> success() {
        return ResponseEntity.status(HttpStatus.OK.value()).body(new SuccessResponse(true, "Successful"));
    }

    public ResponseEntity<SuccessResponse> success(String message) {
        return ResponseEntity.status(HttpStatus.OK.value()).body(new SuccessResponse(true, message));
    }

    public <T> ResponseEntity<T> entity(T entity) {
        return ResponseEntity.status(HttpStatus.OK.value())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(entity);
    }
}
