package com.duoc.enrollmentplatform.users.domain.repositories;

import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> usersById = new HashMap<>();

    public InMemoryUserRepository() {}

    public InMemoryUserRepository(List<User> initial) {
        initial.forEach(this::save);
    }

    @Override
    public void save(User user) {
        usersById.put(user.getId().getValue(), user);
    }

    @Override
    public Optional<User> findById(Id id) {
        return Optional.ofNullable(usersById.get(id.getValue()));
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return usersById.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findByStudentId(Id studentId) {
        return usersById.values().stream()
                .filter(user -> user.getStudentId() != null && user.getStudentId().equals(studentId))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }

    @Override
    public void delete(Id id) {
        usersById.remove(id.getValue());
    }
}
