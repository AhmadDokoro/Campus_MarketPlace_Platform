package com.ahsmart.campusmarket.exceptions;

import com.ahsmart.campusmarket.payloadDTOs.APIExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // gets called whenever a controller throws and exception
public class myGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e){
        Map<String, String> response = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError)error).getField();   // name of the field which does not meet constrain defined
            String message  =  error.getDefaultMessage();        // the error
            response.put(fieldName, message);
        });

        return new ResponseEntity<Map<String, String>>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIExceptionResponse> myResourceNotFoundException(ResourceNotFoundException e)
    {
        String message = e.getMessage();
        APIExceptionResponse apqResponse = new APIExceptionResponse(message, false);
        return new ResponseEntity<>(apqResponse, HttpStatus.NOT_FOUND);
    }



    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIExceptionResponse> myAPIException(APIException e)
    {
        String message = e.getMessage();
        APIExceptionResponse apiResponse = new APIExceptionResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

}
