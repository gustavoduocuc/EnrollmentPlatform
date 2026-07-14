package com.duoc.enrollmentplatform.enrollment.tests.unit;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.InMemoryCourseRepository;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.enrollment.application.CreateEnrollmentUseCase;
import com.duoc.enrollmentplatform.enrollment.application.EnrollmentSummaryDTO;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentMessagePublisher;
import com.duoc.enrollmentplatform.enrollment.application.summary.EnrollmentSummaryGenerator;
import com.duoc.enrollmentplatform.enrollment.application.UpdateEnrollmentUseCase;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.InMemoryEnrollmentRepository;
import com.duoc.enrollmentplatform.enrollment.infrastructure.adapters.InMemoryEnrollmentSummaryStorage;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.InMemoryUserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpdateEnrollmentUseCaseTest {

    @Test
    void replacesSummaryInStorageWhenEnrollmentIsUpdated() throws Exception {
        InMemoryEnrollmentSummaryStorage storage = new InMemoryEnrollmentSummaryStorage();
        EnrollmentSummaryGenerator generator = new EnrollmentSummaryGenerator();
        InMemoryCourseRepository courses = new InMemoryCourseRepository(List.of(
                Course.create(
                        Id.create("c-1"),
                        "Intro",
                        Id.create("u-teacher-001"),
                        Section.create("A"),
                        10,
                        Money.create(150000)),
                Course.create(
                        Id.create("c-2"),
                        "DB",
                        Id.create("u-teacher-001"),
                        Section.create("B"),
                        10,
                        Money.create(120000))));
        InMemoryUserRepository users = new InMemoryUserRepository(List.of(
                User.reconstitute(
                        Id.create("u-1"),
                        Email.create("juan@duoc.cl"),
                        "Juan",
                        Id.create("s-1"),
                        Role.STUDENT,
                        UserStatus.ACTIVE)));
        InMemoryEnrollmentRepository enrollments = new InMemoryEnrollmentRepository();

        CreateEnrollmentUseCase create = new CreateEnrollmentUseCase(
                courses, users, enrollments, generator, storage, mock(EnrollmentMessagePublisher.class));
        EnrollmentSummaryDTO created = create.execute("s-1", List.of("c-1"));

        UpdateEnrollmentUseCase update = new UpdateEnrollmentUseCase(courses, enrollments, users, generator, storage);
        EnrollmentSummaryDTO updated = update.execute(created.enrollmentId, List.of("c-1", "c-2"));

        assertEquals(270000, updated.totalAmount);
        byte[] stored = storage.download(created.enrollmentId).orElseThrow().content;
        JsonNode root = new ObjectMapper().readTree(stored);
        assertEquals(270000, root.get("totalAmount").asDouble());
    }
}
