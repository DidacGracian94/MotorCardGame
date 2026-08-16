package com.motorcardgame.app.gamedefinition.web;

import com.motorcardgame.app.gamedefinition.application.GameDefinitionNotFoundException;
import com.motorcardgame.app.gamedefinition.application.SlugAlreadyExistsException;
import com.motorcardgame.app.gamedefinition.instance.application.GameInstanceNotFoundException;
import com.motorcardgame.app.gamedefinition.instance.web.GameInstanceController;
import com.motorcardgame.app.gamedefinition.instance.web.InstanceController;
import com.motorcardgame.app.gamedefinition.version.application.GameDefinitionVersionNotFoundException;
import com.motorcardgame.app.gamedefinition.version.application.VersionPublishConflictException;
import com.motorcardgame.app.gamedefinition.version.web.GameDefinitionVersionController;
import com.motorcardgame.engine.exception.InvalidGameDefinitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
        GameDefinitionController.class,
        GameDefinitionVersionController.class,
        GameInstanceController.class,
        InstanceController.class
})
class GameDefinitionExceptionHandler {

    @ExceptionHandler(GameDefinitionNotFoundException.class)
    ResponseEntity<String> handleNotFound(GameDefinitionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SlugAlreadyExistsException.class)
    ResponseEntity<String> handleSlugConflict(SlugAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(GameDefinitionVersionNotFoundException.class)
    ResponseEntity<String> handleVersionNotFound(GameDefinitionVersionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(VersionPublishConflictException.class)
    ResponseEntity<String> handleVersionPublishConflict(VersionPublishConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(GameInstanceNotFoundException.class)
    ResponseEntity<String> handleInstanceNotFound(GameInstanceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidGameDefinitionException.class)
    ResponseEntity<String> handleInvalidGameDefinition(InvalidGameDefinitionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
