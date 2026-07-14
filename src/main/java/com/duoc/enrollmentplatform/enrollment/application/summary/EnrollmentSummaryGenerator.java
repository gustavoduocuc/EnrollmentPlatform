package com.duoc.enrollmentplatform.enrollment.application.summary;

import com.duoc.enrollmentplatform.enrollment.application.EnrollmentLineDTO;
import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EnrollmentSummaryGenerator {
    private final ObjectMapper objectMapper;

    public EnrollmentSummaryGenerator() {
        this.objectMapper = new ObjectMapper();
    }

    public byte[] toJsonBytes(Enrollment enrollment, User student) {
        EnrollmentSummaryDocumentDTO document = toDocument(enrollment, student);
        try {
            return objectMapper.writeValueAsBytes(document);
        } catch (Exception error) {
            throw new IllegalStateException("Failed to serialize enrollment summary", error);
        }
    }

    public EnrollmentSummaryDocumentDTO toDocument(Enrollment enrollment, User student) {
        var lineDtos = enrollment.getLines().stream()
                .map(line -> new EnrollmentLineDTO(
                        line.getCourseId().getValue(),
                        line.getCourseName(),
                        line.getUnitPrice().getValue()))
                .toList();
        return new EnrollmentSummaryDocumentDTO(
                enrollment.getId().getValue(),
                enrollment.getStudentId().getValue(),
                student.getFullName(),
                student.getEmail().getValue(),
                enrollment.getEnrolledAt().toString(),
                lineDtos,
                enrollment.calculateTotal().getValue());
    }
}
