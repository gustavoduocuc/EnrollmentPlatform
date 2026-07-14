package com.duoc.enrollmentplatform.users.infrastructure.adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStore extends JpaRepository<UserRecord, String> {
    Optional<UserRecord> findByEmail(String email);
    Optional<UserRecord> findByStudentId(String studentId);
}
