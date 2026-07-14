package com.duoc.enrollmentplatform.users.infrastructure.adapters;

import com.duoc.enrollmentplatform.users.application.ports.IdentityTenantRegister;

public class NoOpIdentityTenantRegister implements IdentityTenantRegister {
    @Override
    public void provision(String email, String fullName) {
    }
}
