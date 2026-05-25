package com.gargotrust.gestion_achats_enligne.response;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {

        ApiResponse<Object> response = new ApiResponse<>(
                List.of(ex.getMessage()),
                new ApiResponse.Payload<>("false", null)
        );

        return ResponseEntity.badRequest().body(response);
    }
}
