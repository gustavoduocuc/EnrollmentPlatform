package com.duoc.enrollmentplatform.enrollment.application;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.enrollment.domain.entities.EnrollmentLine;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.EnrollmentRepository;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentSummaryStorage;
import com.duoc.enrollmentplatform.enrollment.application.summary.EnrollmentSummaryGenerator;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

import java.util.List;

public class UpdateEnrollmentUseCase {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentSummaryGenerator summaryGenerator;
    private final EnrollmentSummaryStorage summaryStorage;
    private final EnrollmentDtoMapper mapper;

    public UpdateEnrollmentUseCase(CourseRepository courseRepository,
                                   EnrollmentRepository enrollmentRepository,
                                   UserRepository userRepository,
                                   EnrollmentSummaryGenerator summaryGenerator,
                                   EnrollmentSummaryStorage summaryStorage) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.summaryGenerator = summaryGenerator;
        this.summaryStorage = summaryStorage;
        this.mapper = new EnrollmentDtoMapper();
    }

    public EnrollmentSummaryDTO execute(String enrollmentId, List<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            throw DomainError.validation("Enrollment must include at least one course");
        }
        Enrollment enrollment = enrollmentRepository.findById(Id.create(enrollmentId))
                .orElseThrow(() -> DomainError.notFound("Enrollment " + enrollmentId + " not found"));

        List<EnrollmentLine> lines = courseIds.stream()
                .map(courseId -> {
                    Course course = courseRepository.findById(Id.create(courseId))
                            .orElseThrow(() -> DomainError.notFound("Course " + courseId + " not found"));
                    return EnrollmentLine.create(Id.generate(), course.getId(), course.getName(), course.getPrice());
                })
                .toList();

        enrollment.replaceLines(lines);
        enrollmentRepository.save(enrollment);

        if (summaryStorage.exists(enrollmentId)) {
            User student = userRepository.findByStudentId(enrollment.getStudentId())
                    .orElseThrow(() -> DomainError.notFound("Student " + enrollment.getStudentId().getValue() + " not found"));
            summaryStorage.replace(enrollmentId, summaryGenerator.toJsonBytes(enrollment, student));
        }

        return mapper.toSummaryDto(enrollment);
    }
}
