package com.duoc.enrollmentplatform.courses.application;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

public class UpdateCourseUseCase {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ActiveTeacherLookup activeTeacherLookup;

    public UpdateCourseUseCase(
            CourseRepository courseRepository,
            UserRepository userRepository,
            ActiveTeacherLookup activeTeacherLookup) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.activeTeacherLookup = activeTeacherLookup;
    }

    public CourseDTO execute(String courseId, String name, String instructorId) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasInstructor = instructorId != null && !instructorId.isBlank();
        if (!hasName && !hasInstructor) {
            throw DomainError.other("At least one of name or instructorId is required");
        }

        Course course = courseRepository.findById(Id.create(courseId))
                .orElseThrow(() -> DomainError.notFound("Course " + courseId + " not found"));

        if (hasName) {
            Course conflict = courseRepository.findByNameAndSection(name.trim(), course.getSection()).orElse(null);
            if (conflict != null && !conflict.getId().equals(course.getId())) {
                throw DomainError.validation(
                        "Course \"" + name.trim() + "\" section " + course.getSection().value() + " already exists");
            }
            course = course.withName(name);
        }

        User teacher = userRepository.findById(course.getInstructorId()).orElse(null);
        if (hasInstructor) {
            teacher = activeTeacherLookup.require(Id.create(instructorId));
            course = course.withInstructorId(teacher.getId());
        }
        if (teacher == null) {
            throw DomainError.notFound("User " + course.getInstructorId().getValue() + " not found");
        }

        courseRepository.save(course);
        return CourseMapper.toDTO(course, teacher.getFullName());
    }
}
