package com.isyraf.kuantanfunmap.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserAlreadyExist.class})
    public ResponseEntity<CustomErrorResponse> handleUserAlreadyExist(
            UserAlreadyExist ex,
            HttpServletRequest request
    ) {

        CustomErrorResponse response = new CustomErrorResponse();

        response.setStatus(HttpStatus.CONFLICT.value());
        response.setTitle("User Already Exists");
        response.setDescription(ex.getMessage());
        response.setPath(request.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({UserNotExist.class})
    public ResponseEntity<CustomErrorResponse> handleUserNotExist(
            UserNotExist ex,
            HttpServletRequest request
    ) {
        CustomErrorResponse response = new CustomErrorResponse();

        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setTitle("User Not Exist");
        response.setDescription(ex.getMessage());
        response.setPath(request.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({InvalidCredential.class})
    public ResponseEntity<CustomErrorResponse> handleInvalidCredentail(
            InvalidCredential ex,
            HttpServletRequest request
    ) {
        CustomErrorResponse response = new CustomErrorResponse();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setTitle("Incorrect password");
        response.setDescription(ex.getMessage());
        response.setPath(request.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler({InvalidToken.class})
    public ResponseEntity<CustomErrorResponse> handleInvalidToken(
            InvalidToken ex,
            HttpServletRequest request
    ) {
        CustomErrorResponse response = new CustomErrorResponse();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setTitle("Invalid Token");
        response.setDescription(ex.getMessage());
        response.setPath(request.getRequestURI());
        response.setTimestamp(ZonedDateTime.now());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
