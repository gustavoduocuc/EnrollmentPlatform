package com.duoc.enrollmentplatform.courses.domain.repositories;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    void save(Course course);
    Optional<Course> findById(Id id);
    List<Course> findAll();
    Optional<Course> findByNameAndSection(String name, Section section);
    void delete(Id id);
}
