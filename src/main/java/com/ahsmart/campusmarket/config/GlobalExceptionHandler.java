package com.ahsmart.campusmarket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle multipart size exceptions specifically and redirect back to the verification page
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public RedirectView handleMaxUpload(MaxUploadSizeExceededException ex) {
        logger.warn("Upload exceeded maximum size", ex);
        // Redirect with explicit query parameter so the template can display the error (safer than relying on model exposure)
        String message = "File upload exceeded maximum size (5MB). Please resize and try again.";
        return new RedirectView("/auth/requestVerification?error=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
    }

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
