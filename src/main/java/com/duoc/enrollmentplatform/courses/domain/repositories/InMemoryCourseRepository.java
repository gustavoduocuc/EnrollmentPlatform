package com.duoc.enrollmentplatform.courses.domain.repositories;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryCourseRepository implements CourseRepository {
    private final Map<String, Course> courses = new HashMap<>();

    public InMemoryCourseRepository() {}

    public InMemoryCourseRepository(List<Course> initial) {
        initial.forEach(course -> courses.put(course.getId().getValue(), course));
    }

    @Override
    public void save(Course course) {
        courses.put(course.getId().getValue(), course);
    }

    @Override
    public Optional<Course> findById(Id id) {
        return Optional.ofNullable(courses.get(id.getValue()));
    }

    @Override
    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }

    @Override
    public Optional<Course> findByNameAndSection(String name, Section section) {
        return courses.values().stream()
                .filter(course -> course.getName().equals(name) && course.getSection().equals(section))
                .findFirst();
    }

    @Override
    public void delete(Id id) {
        courses.remove(id.getValue());
    }
}
