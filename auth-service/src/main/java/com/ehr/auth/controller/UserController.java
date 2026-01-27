package com.ehr.auth.controller;

import static com.ehr.auth.constants.ApiPaths.USERS_API_PATH;

import com.ehr.auth.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(USERS_API_PATH)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_USER:DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, Authentication authentication) {
        UUID currentUserId = (UUID) authentication.getPrincipal();
        userService.deleteUser(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
