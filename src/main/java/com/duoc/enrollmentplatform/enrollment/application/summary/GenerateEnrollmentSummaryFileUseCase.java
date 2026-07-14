package com.duoc.enrollmentplatform.enrollment.application.summary;

import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.EnrollmentRepository;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;

public class GenerateEnrollmentSummaryFileUseCase {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final EnrollmentSummaryGenerator summaryGenerator;

    public GenerateEnrollmentSummaryFileUseCase(EnrollmentRepository enrollmentRepository,
                                                UserRepository userRepository,
                                                EnrollmentSummaryGenerator summaryGenerator) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.summaryGenerator = summaryGenerator;
    }

    public GeneratedSummaryFile execute(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(Id.create(enrollmentId))
                .orElseThrow(() -> DomainError.notFound("Enrollment " + enrollmentId + " not found"));
        User student = userRepository.findByStudentId(enrollment.getStudentId())
                .orElseThrow(() -> DomainError.notFound("Student " + enrollment.getStudentId().getValue() + " not found"));
        byte[] content = summaryGenerator.toJsonBytes(enrollment, student);
        String filename = "summary-" + enrollmentId + ".json";
        return new GeneratedSummaryFile(content, filename, "application/json");
    }
}
