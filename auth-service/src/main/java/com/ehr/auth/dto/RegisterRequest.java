package com.ehr.auth.dto;

import com.ehr.auth.model.enums.UserRole;

public record RegisterRequest(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        UserRole role) {  
}
