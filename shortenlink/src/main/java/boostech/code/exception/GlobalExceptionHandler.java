package boostech.code.exception;

import boostech.code.payload.response.ErrorDetail;
import boostech.code.payload.response.UrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<UrlResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
        List<ErrorDetail> error = List.of(new ErrorDetail(
                "shortUrl",
                "NOT_FOUND",
                exception.getMessage()
        ));
        // 404 (NOT_FOUND)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new UrlResponse(
                "Error",
                exception.getMessage(),
                Collections.singletonList("/api/v1/**"),
                error
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<UrlResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        List<ErrorDetail> error = List.of(new ErrorDetail(
                "shortUrl",
                "BAD_REQUEST",
                exception.getMessage()
        ));
        // 400
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UrlResponse(
                "Error",
                "Bad request",
                "/api/v1/**"
        ));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<UrlResponse> handleGeneralException(Exception exception, HttpServletRequest request) {
        List<ErrorDetail> error = List.of(new ErrorDetail(
                "GENERAL EXCEPTION",
                "INTERNAL_SERVER_ERROR",
                exception.getMessage()
        ));
        exception.printStackTrace();

        String requestUri = request.getRequestURI();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UrlResponse(
                "Error",
                "Internal server error",
                Collections.singletonList(requestUri),
                error
        ));
    }





}
