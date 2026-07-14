package com.duoc.enrollmentplatform.users.application;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;

public class LoginUserUseCase {
    private final UserRepository userRepository;

    public LoginUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResultDTO execute(String email) {
        User user = userRepository.findByEmail(Email.create(email))
                .orElseThrow(() -> DomainError.notFound("User " + email + " not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw DomainError.validation("User " + email + " has not completed registration");
        }
        return new LoginResultDTO(true, user.getEmail().getValue(), user.getRole().name());
    }
}
