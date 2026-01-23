package com.ehr.auth.dto;

import com.ehr.auth.model.enums.UserRole;

public record AuthResponse(
    String token, 
    String username, 
    UserRole role) {
}
