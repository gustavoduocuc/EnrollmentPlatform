package com.duoc.enrollmentplatform.courses.application;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

public class ActiveTeacherLookup {
    private final UserRepository userRepository;

    public ActiveTeacherLookup(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User require(Id instructorId) {
        User user = userRepository.findById(instructorId)
                .orElseThrow(() -> DomainError.notFound("User " + instructorId.getValue() + " not found"));
        user.requireActiveTeacher();
        return user;
    }
}
