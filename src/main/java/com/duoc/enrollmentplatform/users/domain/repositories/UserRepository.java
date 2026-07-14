package com.duoc.enrollmentplatform.users.domain.repositories;

import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(Id id);
    Optional<User> findByEmail(Email email);
    Optional<User> findByStudentId(Id studentId);
    List<User> findAll();
    void delete(Id id);
}
