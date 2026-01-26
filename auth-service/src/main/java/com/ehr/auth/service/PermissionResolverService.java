package com.ehr.auth.service;

import com.ehr.auth.model.User;
import com.ehr.auth.model.enums.UserRole;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class PermissionResolverService {

    // Hardcoded role-permission mappings for Phase 1
    private static final Map<UserRole, Set<String>> ROLE_PERMISSIONS = Map.of(
        UserRole.ADMIN, Set.of("USER:DELETE")
    );

    public Set<String> resolvePermissions(User user) {
        return ROLE_PERMISSIONS.getOrDefault(user.getRole(), Collections.emptySet());
    }

    public boolean hasPermission(User user, String permission) {
        Set<String> permissions = resolvePermissions(user);
        return permissions.contains(permission);
    }
}
