package com.motorcardgame.app;

import com.motorcardgame.engine.Engine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
class StatusController {

    @GetMapping("/api/status")
    Map<String, String> status() {
        return Map.of("status", "UP", "engineVersion", Engine.VERSION);
    }
}
