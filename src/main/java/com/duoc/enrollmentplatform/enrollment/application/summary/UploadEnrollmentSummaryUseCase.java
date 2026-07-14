package com.duoc.enrollmentplatform.enrollment.application.summary;

import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.EnrollmentRepository;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentSummaryStorage;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

public class UploadEnrollmentSummaryUseCase {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentSummaryGenerator summaryGenerator;
    private final EnrollmentSummaryStorage summaryStorage;

    public UploadEnrollmentSummaryUseCase(EnrollmentRepository enrollmentRepository,
                                          UserRepository userRepository,
                                          EnrollmentSummaryGenerator summaryGenerator,
                                          EnrollmentSummaryStorage summaryStorage) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.summaryGenerator = summaryGenerator;
        this.summaryStorage = summaryStorage;
    }

    public SummaryUploadResult execute(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(Id.create(enrollmentId))
                .orElseThrow(() -> DomainError.notFound("Enrollment " + enrollmentId + " not found"));
        User student = userRepository.findByStudentId(enrollment.getStudentId())
                .orElseThrow(() -> DomainError.notFound("Student " + enrollment.getStudentId().getValue() + " not found"));
        byte[] content = summaryGenerator.toJsonBytes(enrollment, student);
        return summaryStorage.upload(enrollmentId, content);
    }
}
