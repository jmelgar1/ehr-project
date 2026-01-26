package com.ehr.auth.constants;

public final class ApiPaths {
    public static final String AUTH_API_PATH = "/api/auth";
    public static final String AUTH_LOGIN_API_PATH = AUTH_API_PATH + "/login";
    public static final String AUTH_REGISTER_API_PATH = AUTH_API_PATH + "/register";

    public static final String USERS_API_PATH = "/api/users";
    public static final String USERS_BY_ID_API_PATH = USERS_API_PATH + "/{id}";

    private ApiPaths() {}
}
