package com.fortune.project.exception;

import com.fortune.project.dto.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException ex){
        ApiErrorResponse err = new ApiErrorResponse(
                ex.getStatusCode().value(),
                ex.getReason(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(err, ex.getStatusCode());
    }

}
