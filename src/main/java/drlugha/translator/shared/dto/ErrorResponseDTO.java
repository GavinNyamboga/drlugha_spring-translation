package drlugha.translator.shared.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class ErrorResponseDTO {

    @Setter
    private String message;
    @Setter
    private int code;
    @Setter
    private int status;
    private String developerMessage;
    private String path;
    private String timestamp;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");
    private List<String> errorList;


    public ErrorResponseDTO(final String message) {
        super();
        this.message = message;
        timestamp = formatter.format(LocalDateTime.now());
    }

    public ErrorResponseDTO(final String message, final String developerMessage, int code, int status, String path) {
        super();
        this.message = message;
        this.developerMessage = developerMessage;
        this.code = code;
        this.status = status;
        this.path = path;
        timestamp = formatter.format(LocalDateTime.now());
    }
}
