package com.duoc.enrollmentplatform.users.application.ports;

public interface IdentityTenantRegister {
    void provision(String email, String fullName);
}
