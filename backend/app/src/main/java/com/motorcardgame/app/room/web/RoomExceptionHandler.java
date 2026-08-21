package com.motorcardgame.app.room.web;

import com.motorcardgame.app.room.application.RoomAccessDeniedException;
import com.motorcardgame.app.room.application.RoomEmptyException;
import com.motorcardgame.app.room.application.RoomNotFoundException;
import com.motorcardgame.app.room.application.RoomNotOpenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RoomController.class)
class RoomExceptionHandler {

    @ExceptionHandler(RoomNotFoundException.class)
    ResponseEntity<String> handleNotFound(RoomNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RoomNotOpenException.class)
    ResponseEntity<String> handleNotOpen(RoomNotOpenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(RoomEmptyException.class)
    ResponseEntity<String> handleEmpty(RoomEmptyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(RoomAccessDeniedException.class)
    ResponseEntity<String> handleAccessDenied(RoomAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
