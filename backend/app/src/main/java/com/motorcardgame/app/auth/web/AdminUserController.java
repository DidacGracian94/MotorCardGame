package com.motorcardgame.app.auth.web;

import com.motorcardgame.app.auth.application.TokenClaims;
import com.motorcardgame.app.auth.application.UserAdminService;
import com.motorcardgame.app.auth.web.dto.ChangeUserRoleRequest;
import com.motorcardgame.app.auth.web.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solo accesible con {@code ROLE_ADMIN} (regla declarativa en {@code SecurityConfig} sobre
 * {@code /api/admin/**}). Es la única forma de promover a alguien a admin dentro de la app — el
 * primer admin se crea a mano en base de datos, no hay ningún camino de auto-promoción.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public List<UserResponse> listAll() {
        return userAdminService.listUsers().stream().map(UserResponse::from).toList();
    }

    @PutMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable UUID id, @Valid @RequestBody ChangeUserRoleRequest request) {
        UUID actingAdminId = currentPrincipal().userId();
        return UserResponse.from(userAdminService.changeRole(actingAdminId, id, request.role()));
    }

    private static TokenClaims currentPrincipal() {
        return (TokenClaims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
