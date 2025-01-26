package drlugha.translator.shared.exception;


import drlugha.translator.shared.dto.ErrorResponseDTO;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messages;

    private final HttpServletRequest servletRequest;

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add("Content-Type", "application/json");
//        httpHeaders.add("Access-Control-Allow-Origin", "*");
//        httpHeaders.add("Accept", "*");
        return httpHeaders;
    }


    /**
     * Handles jakarta.validation.ConstraintViolationException. Thrown when @Validated fails.
     *
     * @param ex the ConstraintViolationException
     * @return the ApiError object
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        logger.error("Validatio eerro");
        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO("Validation error", ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.value(),
                getRequestPath());
        return handleExceptionInternal(ex, bodyOfResponse, getHttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles EntityNotFoundException. Created to encapsulate errors with more detail than jakarta.persistence.EntityNotFoundException.
     *
     * @param ex the EntityNotFoundException
     * @return the ApiError object
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(
            EntityNotFoundException ex) {
        logger.error("Entity  not found");
        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO(ex.getMessage(), ex.getMessage(), HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.value(), getRequestPath());
        return new ResponseEntity<>(bodyOfResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle DataIntegrityViolationException, inspects the cause for different DB causes.
     *
     * @param ex the DataIntegrityViolationException
     * @return the ApiError object
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                  WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String messages = ex.getMessage();
        if (ex.getCause() instanceof ConstraintViolationException) {
            status = HttpStatus.CONFLICT;
            messages = "Database error";
        }

        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO(messages, ex.getMessage(), status.value(), status.value(),
                getRequestPath());
        return handleExceptionInternal(ex, bodyOfResponse, getHttpHeaders(), status, request);
    }

    /**
     * Handle Exception, handle generic Exception.class
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                      WebRequest request) {
        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO(String.format("The parameter '%s' of value '%s' could not be converted to type '%s'", ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName()),
                ex.getMessage(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.value(),
                getRequestPath());
        return handleExceptionInternal(ex, bodyOfResponse, getHttpHeaders(), HttpStatus.BAD_REQUEST, request);

    }

    // 400
    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<Object> handleInvalidData(final RuntimeException ex, final WebRequest request) {
        logger.error("Invalid data");
        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO(ex.getMessage(), ex.getMessage(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.value(),
                getRequestPath());
        if (ex instanceof BadRequestException) {
            BadRequestException badRequestException = (BadRequestException) ex;
            List<String> errors = badRequestException.getErrorList();
            if (errors != null && !errors.isEmpty()) {
                bodyOfResponse.setErrorList(errors);
            }
        }
        return handleExceptionInternal(ex, bodyOfResponse, getHttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }


    // 401
    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<Object> handleUnauthorized(final RuntimeException ex, final WebRequest request) {
        logger.error("Unauthrized error");
        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO(ex.getMessage(), ex.getMessage(), 401, 401,
                getRequestPath());
        return handleExceptionInternal(ex, bodyOfResponse, getHttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    // 403
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDenied(final RuntimeException ex, final WebRequest request) {
        logger.error("Access denied");
        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO(ex.getMessage(), ex.getMessage(), 403, 403,
                getRequestPath());
        return handleExceptionInternal(ex, bodyOfResponse, getHttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    // 404
    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Object> handleNotFound(final RuntimeException ex, final WebRequest request) {
        logger.error("Not found");
        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO(ex.getMessage(), ex.getMessage(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.value(),
                getRequestPath());
        return handleExceptionInternal(ex, bodyOfResponse, getHttpHeaders(), HttpStatus.NOT_FOUND, request);
    }


    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleInternal(final RuntimeException ex, final WebRequest request) {
        logger.info("Local Message" + ex.getLocalizedMessage());
        logger.info("Message " + ex.getMessage());
        logger.info("Context PAth 4" + servletRequest.getRequestURI() + "?" + servletRequest.getQueryString());
        final ErrorResponseDTO bodyOfResponse = new ErrorResponseDTO("Internal Error Occurred", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                getRequestPath());
        return new ResponseEntity<>(bodyOfResponse, getHttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private String getRequestPath() {
        logger.error("Custom Web error path==" + servletRequest.getServletPath());
        String path = servletRequest.getRequestURI();
        if (servletRequest.getQueryString() == null)
            return path;
        return path + "?" + servletRequest.getQueryString();
    }

}
