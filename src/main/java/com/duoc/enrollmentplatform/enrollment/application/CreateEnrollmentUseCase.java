package com.duoc.enrollmentplatform.enrollment.application;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.enrollment.domain.entities.EnrollmentLine;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.EnrollmentRepository;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentSummaryStorage;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentMessagePublisher;
import com.duoc.enrollmentplatform.enrollment.application.summary.EnrollmentSummaryGenerator;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

import java.util.List;

public class CreateEnrollmentUseCase {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentSummaryGenerator summaryGenerator;
    private final EnrollmentSummaryStorage summaryStorage;
    private final EnrollmentMessagePublisher messagePublisher;
    private final EnrollmentDtoMapper mapper;

    public CreateEnrollmentUseCase(CourseRepository courseRepository,
                                   UserRepository userRepository,
                                   EnrollmentRepository enrollmentRepository,
                                   EnrollmentSummaryGenerator summaryGenerator,
                                   EnrollmentSummaryStorage summaryStorage,
                                   EnrollmentMessagePublisher messagePublisher) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.summaryGenerator = summaryGenerator;
        this.summaryStorage = summaryStorage;
        this.messagePublisher = messagePublisher;
        this.mapper = new EnrollmentDtoMapper();
    }

    public EnrollmentSummaryDTO execute(String studentId, List<String> courseIds) {
        User student = userRepository.findByStudentId(Id.create(studentId))
                .orElseThrow(() -> DomainError.notFound("Student " + studentId + " not found"));

        List<EnrollmentLine> lines = courseIds.stream()
                .map(courseId -> {
                    Course course = courseRepository.findById(Id.create(courseId))
                            .orElseThrow(() -> DomainError.notFound("Course " + courseId + " not found"));
                    return EnrollmentLine.create(Id.generate(), course.getId(), course.getName(), course.getPrice());
                })
                .toList();

        Enrollment enrollment = Enrollment.create(Id.generate(), Id.create(studentId), lines);
        enrollmentRepository.save(enrollment);

        String enrollmentId = enrollment.getId().getValue();
        summaryStorage.upload(enrollmentId, summaryGenerator.toJsonBytes(enrollment, student));

        messagePublisher.publish(enrollment);

        return mapper.toSummaryDto(enrollment);
    }
}
