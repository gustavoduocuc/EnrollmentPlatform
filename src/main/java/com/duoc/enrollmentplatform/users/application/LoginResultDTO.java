package com.duoc.enrollmentplatform.users.application;

public class LoginResultDTO {
    public final boolean ready;
    public final String email;
    public final String role;

    public LoginResultDTO(boolean ready, String email, String role) {
        this.ready = ready;
        this.email = email;
        this.role = role;
    }
}
