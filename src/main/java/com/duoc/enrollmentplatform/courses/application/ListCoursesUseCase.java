package com.duoc.enrollmentplatform.courses.application;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

import java.util.List;

public class ListCoursesUseCase {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public ListCoursesUseCase(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public List<CourseDTO> execute() {
        return courseRepository.findAll().stream().map(this::toDTO).toList();
    }

    private CourseDTO toDTO(Course course) {
        String instructorName = userRepository.findById(course.getInstructorId())
                .map(user -> user.getFullName())
                .orElse(null);
        return CourseMapper.toDTO(course, instructorName);
    }
}
