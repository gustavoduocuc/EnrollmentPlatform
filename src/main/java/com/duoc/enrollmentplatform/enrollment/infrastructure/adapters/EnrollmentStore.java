package com.duoc.enrollmentplatform.enrollment.infrastructure.adapters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentStore extends JpaRepository<EnrollmentRecord, String> {
    @Query(value = "SELECT COUNT(*) > 0 FROM enrollment_lines WHERE course_id = :courseId", nativeQuery = true)
    boolean existsByCourseId(@Param("courseId") String courseId);
}
