package com.sarthak.library.author_book_management.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    //404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(),404,System.currentTimeMillis()), HttpStatus.NOT_FOUND);
    }

    //409
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex){
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(),409,System.currentTimeMillis()), HttpStatus.CONFLICT);
    }

    //400 validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidationException(
            MethodArgumentNotValidException ex
    ){
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(),error.getDefaultMessage()));

        Map<String,Object> response = new HashMap<>();
        response.put("message","Validation failed");
        response.put("errors", fieldErrors);
        response.put("status", 400);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //500 fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex){
        return new ResponseEntity<>(new ErrorResponse("Something went wrong" + ex,500,System.currentTimeMillis()), HttpStatus.INTERNAL_SERVER_ERROR );
    }
}
