package com.duoc.enrollmentplatform.enrollment.domain.repositories;

import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;

import java.util.*;

public class InMemoryEnrollmentRepository implements EnrollmentRepository {
    private final Map<String, Enrollment> enrollments = new HashMap<>();

    @Override public void save(Enrollment enrollment) { enrollments.put(enrollment.getId().getValue(), enrollment); }
    @Override public Optional<Enrollment> findById(Id id) { return Optional.ofNullable(enrollments.get(id.getValue())); }
    @Override public List<Enrollment> findAll() { return List.copyOf(enrollments.values()); }
    @Override public void deleteById(Id id) { enrollments.remove(id.getValue()); }

    @Override
    public boolean existsByCourseId(Id courseId) {
        return enrollments.values().stream()
                .flatMap(enrollment -> enrollment.getLines().stream())
                .anyMatch(line -> line.getCourseId().equals(courseId));
    }
}
