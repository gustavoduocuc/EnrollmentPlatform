package com.duoc.enrollmentplatform.users.application;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

public class UpdateUserUseCase {
    private final UserRepository userRepository;

    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO execute(String userId, String fullName) {
        User user = userRepository.findById(Id.create(userId))
                .orElseThrow(() -> DomainError.notFound("User " + userId + " not found"));
        user.updateFullName(fullName);
        userRepository.save(user);
        return UserDTO.from(user);
    }
}
