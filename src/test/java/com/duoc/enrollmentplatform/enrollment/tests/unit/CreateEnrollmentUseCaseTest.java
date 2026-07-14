package com.duoc.enrollmentplatform.enrollment.tests.unit;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.InMemoryCourseRepository;
import com.duoc.enrollmentplatform.enrollment.application.CreateEnrollmentUseCase;
import com.duoc.enrollmentplatform.enrollment.application.EnrollmentSummaryDTO;
import com.duoc.enrollmentplatform.enrollment.application.ports.EnrollmentMessagePublisher;
import com.duoc.enrollmentplatform.enrollment.application.summary.EnrollmentSummaryGenerator;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.InMemoryEnrollmentRepository;
import com.duoc.enrollmentplatform.enrollment.infrastructure.adapters.InMemoryEnrollmentSummaryStorage;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.InMemoryUserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateEnrollmentUseCaseTest {
    private Course javaIntro;
    private Course databases;
    private User student;
    private InMemoryEnrollmentSummaryStorage summaryStorage;

    @BeforeEach void setup() {
        javaIntro = Course.create(Id.create("c-1"), "Introducción a Java", "María González", 40, Money.create(150000));
        databases = Course.create(Id.create("c-2"), "Bases de datos", "Carlos Pérez", 30, Money.create(120000));
        student = User.reconstitute(
                Id.create("u-1"),
                Email.create("juan.soto@duoc.cl"),
                "Juan Soto",
                Id.create("s-1"),
                Role.STUDENT,
                UserStatus.ACTIVE);
        summaryStorage = new InMemoryEnrollmentSummaryStorage();
    }

    private CreateEnrollmentUseCase useCase(InMemoryCourseRepository courses, InMemoryUserRepository users) {
        return new CreateEnrollmentUseCase(
                courses,
                users,
                new InMemoryEnrollmentRepository(),
                new EnrollmentSummaryGenerator(),
                summaryStorage,
                mock(EnrollmentMessagePublisher.class));
    }

    @Test void createsEnrollmentWithCorrectTotalForMultipleCourses() {
        EnrollmentSummaryDTO dto = useCase(
                new InMemoryCourseRepository(List.of(javaIntro, databases)),
                new InMemoryUserRepository(List.of(student))
        ).execute("s-1", List.of("c-1", "c-2"));
        assertEquals(2, dto.lines.size()); assertEquals(270000, dto.totalAmount); assertNotNull(dto.enrollmentId);
        assertTrue(summaryStorage.exists(dto.enrollmentId));
    }

    @Test void capturesPriceSnapshotAtEnrollmentTime() {
        EnrollmentSummaryDTO dto = useCase(
                new InMemoryCourseRepository(List.of(javaIntro)),
                new InMemoryUserRepository(List.of(student))
        ).execute("s-1", List.of("c-1"));
        assertEquals(150000, dto.lines.get(0).unitPrice); assertEquals("Introducción a Java", dto.lines.get(0).courseName);
    }

    @Test void throwsNotFoundWhenStudentDoesNotExist() {
        assertEquals(DomainError.Type.NOT_FOUND, assertThrows(DomainError.class, () ->
                useCase(new InMemoryCourseRepository(List.of(javaIntro)), new InMemoryUserRepository())
                        .execute("unknown", List.of("c-1"))).getType());
    }

    @Test void throwsNotFoundWhenCourseDoesNotExist() {
        assertEquals(DomainError.Type.NOT_FOUND, assertThrows(DomainError.class, () ->
                useCase(new InMemoryCourseRepository(), new InMemoryUserRepository(List.of(student)))
                        .execute("s-1", List.of("unknown"))).getType());
    }

    @Test void throws422WhenCourseListIsEmpty() {
        assertEquals(DomainError.Type.VALIDATION, assertThrows(DomainError.class, () ->
                useCase(new InMemoryCourseRepository(List.of(javaIntro)), new InMemoryUserRepository(List.of(student)))
                        .execute("s-1", List.of())).getType());
    }
}
