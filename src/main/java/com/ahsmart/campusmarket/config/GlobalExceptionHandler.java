package com.ahsmart.campusmarket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleAll(Throwable ex) {
        // Log the full exception
        logger.error("Unhandled exception caught by GlobalExceptionHandler", ex);

        // Build a readable stack trace for debugging in development
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String body = "Unhandled exception: " + ex.getMessage() + "\n\n" + sw; // use sw directly to avoid unnecessary toString()

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
