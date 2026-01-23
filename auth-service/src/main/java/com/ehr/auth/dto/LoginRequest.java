package com.ehr.auth.dto;

public record LoginRequest(
    String username, 
    String password) {
}
