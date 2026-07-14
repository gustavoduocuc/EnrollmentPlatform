package com.duoc.enrollmentplatform.courses.tests.unit;

import com.duoc.enrollmentplatform.courses.application.DeleteCourseUseCase;
import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.InMemoryCourseRepository;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.enrollment.domain.entities.EnrollmentLine;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.InMemoryEnrollmentRepository;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteCourseUseCaseTest {
    private InMemoryCourseRepository courseRepository;
    private InMemoryEnrollmentRepository enrollmentRepository;
    private DeleteCourseUseCase useCase;

    @BeforeEach
    void setup() {
        courseRepository = new InMemoryCourseRepository(List.of(Course.create(
                Id.create("c-1"),
                "Intro Java",
                Id.create("u-teacher-001"),
                Section.create("A"),
                40,
                Money.create(150000))));
        enrollmentRepository = new InMemoryEnrollmentRepository();
        useCase = new DeleteCourseUseCase(courseRepository, enrollmentRepository);
    }

    @Test
    void deletesCourseWithoutEnrollments() {
        useCase.execute("c-1");
        assertTrue(courseRepository.findById(Id.create("c-1")).isEmpty());
    }

    @Test
    void rejectsWhenCourseNotFound() {
        assertEquals(
                DomainError.Type.NOT_FOUND,
                assertThrows(DomainError.class, () -> useCase.execute("missing")).getType());
    }

    @Test
    void rejectsWhenCourseHasEnrollments() {
        EnrollmentLine line = EnrollmentLine.create(
                Id.create("l-1"), Id.create("c-1"), "Intro Java", Money.create(150000));
        enrollmentRepository.save(Enrollment.create(Id.create("e-1"), Id.create("s-001"), List.of(line)));
        assertEquals(
                DomainError.Type.VALIDATION,
                assertThrows(DomainError.class, () -> useCase.execute("c-1")).getType());
    }
}
