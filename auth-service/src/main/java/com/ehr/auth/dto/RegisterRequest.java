package com.ehr.auth.dto;

import com.ehr.auth.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 20) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12) String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        UserRole role) {
}
