package com.duoc.enrollmentplatform.users.application;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

public class DeleteUserUseCase {
    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(String userId) {
        Id id = Id.create(userId);
        if (userRepository.findById(id).isEmpty()) {
            throw DomainError.notFound("User " + userId + " not found");
        }
        userRepository.delete(id);
    }
}
