package com.ihorvoloshyn.autotests.core.config;

public record Credentials(String username, String password) {
    public Credentials {
        username = username == null ? "" : username;
        password = password == null ? "" : password;
    }

    public boolean isConfigured() {
        return !username.isBlank();
    }

    public static Credentials empty() {
        return new Credentials("", "");
    }
}
