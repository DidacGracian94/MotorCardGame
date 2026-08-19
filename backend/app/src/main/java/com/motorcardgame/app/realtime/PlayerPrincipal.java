package com.motorcardgame.app.realtime;

import java.security.Principal;

public record PlayerPrincipal(String name) implements Principal {

    @Override
    public String getName() {
        return name;
    }
}
