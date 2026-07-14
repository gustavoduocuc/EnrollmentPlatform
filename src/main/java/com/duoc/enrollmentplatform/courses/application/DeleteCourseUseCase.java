package com.duoc.enrollmentplatform.courses.application;

import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.EnrollmentRepository;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;

public class DeleteCourseUseCase {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DeleteCourseUseCase(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public void execute(String courseId) {
        Id id = Id.create(courseId);
        if (courseRepository.findById(id).isEmpty()) {
            throw DomainError.notFound("Course " + courseId + " not found");
        }
        if (enrollmentRepository.existsByCourseId(id)) {
            throw DomainError.validation(
                    "Cannot delete course " + courseId + " because it has active enrollments");
        }
        courseRepository.delete(id);
    }
}
