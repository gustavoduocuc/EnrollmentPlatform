package com.duoc.enrollmentplatform.users.application;

import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

import java.util.List;

public class ListUsersUseCase {
    private final UserRepository userRepository;

    public ListUsersUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> execute() {
        return userRepository.findAll().stream().map(UserDTO::from).toList();
    }
}
