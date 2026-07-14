package com.duoc.enrollmentplatform.users.application;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;

public class PreRegisterUserUseCase {
    private final UserRepository userRepository;

    public PreRegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO execute(String email, String roleName) {
        Email userEmail = Email.create(email);
        if (userRepository.findByEmail(userEmail).isPresent()) {
            throw DomainError.validation("Email " + email + " is already registered");
        }
        Role role = parseRole(roleName);
        User user = User.preRegister(userEmail, role);
        userRepository.save(user);
        return UserDTO.from(user);
    }

    private Role parseRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return Role.STUDENT;
        }
        try {
            return Role.valueOf(roleName.trim().toUpperCase());
        } catch (IllegalArgumentException error) {
            throw DomainError.validation("Invalid role: " + roleName);
        }
    }
}
