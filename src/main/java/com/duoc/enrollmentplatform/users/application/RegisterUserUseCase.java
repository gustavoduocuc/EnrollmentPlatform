package com.duoc.enrollmentplatform.users.application;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.users.application.ports.IdentityTenantRegister;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;

public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final IdentityTenantRegister identityTenantRegister;

    public RegisterUserUseCase(UserRepository userRepository, IdentityTenantRegister identityTenantRegister) {
        this.userRepository = userRepository;
        this.identityTenantRegister = identityTenantRegister;
    }

    public UserDTO execute(String email, String fullName) {
        User user = userRepository.findByEmail(Email.create(email))
                .orElseThrow(() -> DomainError.notFound("User " + email + " not found"));
        if (user.getStatus() != UserStatus.PENDING) {
            throw DomainError.validation("User " + email + " is already registered");
        }
        if (fullName == null || fullName.isBlank()) {
            throw DomainError.validation("Full name is required");
        }
        identityTenantRegister.provision(email, fullName);
        user.completeRegistration(fullName);
        userRepository.save(user);
        return UserDTO.from(user);
    }
}
