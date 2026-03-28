package org.laith.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
//        return ResponseEntity.badRequest().body(ex.getBindingResult().toString());
//    }

    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        // If you want the first field message (e.g. "username must not be blank")
        String msg = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Invalid request"
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        // make it consistent with your UI expectation
        // you can customize text:
        if (msg != null && msg.toLowerCase().contains("must not be blank")) {
            msg = "Username is required";
        }

        return ResponseEntity.badRequest().body(msg);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception ex) {
        return ResponseEntity.badRequest().body(ex.getClass().getSimpleName() + ": " + ex.getMessage());
    }
}
