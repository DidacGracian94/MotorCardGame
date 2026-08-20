package com.motorcardgame.app.auth.web;

import com.motorcardgame.app.auth.application.EmailAlreadyExistsException;
import com.motorcardgame.app.auth.application.InvalidCredentialsException;
import com.motorcardgame.app.auth.application.InvalidGoogleTokenException;
import com.motorcardgame.app.auth.application.InvalidRefreshTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
class AuthExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ResponseEntity<String> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidGoogleTokenException.class)
    ResponseEntity<String> handleInvalidGoogleToken(InvalidGoogleTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
