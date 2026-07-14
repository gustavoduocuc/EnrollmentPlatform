package com.duoc.enrollmentplatform.courses.infrastructure.adapters;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseStore extends JpaRepository<CourseRecord, String> {
    Optional<CourseRecord> findByNameAndSection(String name, String section);
}
