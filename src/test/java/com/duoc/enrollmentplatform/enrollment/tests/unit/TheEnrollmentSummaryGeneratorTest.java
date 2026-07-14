package com.duoc.enrollmentplatform.enrollment.tests.unit;

import com.duoc.enrollmentplatform.enrollment.application.summary.EnrollmentSummaryGenerator;
import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.enrollment.domain.entities.EnrollmentLine;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TheEnrollmentSummaryGeneratorTest {

    @Test
    void serializesEnrollmentSummaryWithRequiredFields() throws Exception {
        Enrollment enrollment = Enrollment.create(Id.create("e-1"), Id.create("s-1"), List.of(
                EnrollmentLine.create(Id.generate(), Id.create("c-1"), "Intro Java", Money.create(150000))));
        User student = User.reconstitute(
                Id.create("u-1"),
                Email.create("juan@duoc.cl"),
                "Juan Soto",
                Id.create("s-1"),
                Role.STUDENT,
                UserStatus.ACTIVE);

        byte[] json = new EnrollmentSummaryGenerator().toJsonBytes(enrollment, student);
        JsonNode root = new ObjectMapper().readTree(json);

        assertEquals("e-1", root.get("enrollmentId").asText());
        assertEquals("s-1", root.get("studentId").asText());
        assertEquals("Juan Soto", root.get("studentFullName").asText());
        assertEquals(150000, root.get("totalAmount").asDouble());
        assertTrue(root.get("lines").isArray());
    }
}
