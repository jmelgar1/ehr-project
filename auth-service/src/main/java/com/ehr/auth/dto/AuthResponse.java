package com.ehr.auth.dto;

import com.ehr.auth.model.enums.UserRole;

public record AuthResponse(
    String accessToken, 
    String username, 
    UserRole role) {
}
