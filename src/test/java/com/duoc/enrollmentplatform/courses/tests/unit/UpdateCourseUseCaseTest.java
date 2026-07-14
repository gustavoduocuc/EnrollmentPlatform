package com.duoc.enrollmentplatform.courses.tests.unit;

import com.duoc.enrollmentplatform.courses.application.ActiveTeacherLookup;
import com.duoc.enrollmentplatform.courses.application.CourseDTO;
import com.duoc.enrollmentplatform.courses.application.UpdateCourseUseCase;
import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.InMemoryCourseRepository;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateCourseUseCaseTest {
    private InMemoryCourseRepository courseRepository;
    private InMemoryUserRepository userRepository;
    private UpdateCourseUseCase useCase;
    private User teacher;
    private User otherTeacher;

    @BeforeEach
    void setup() {
        teacher = User.reconstitute(
                Id.create("u-teacher-001"),
                Email.create("maria.gonzalez@duoc.cl"),
                "María González",
                null,
                Role.TEACHER,
                UserStatus.ACTIVE);
        otherTeacher = User.reconstitute(
                Id.create("u-teacher-002"),
                Email.create("carlos.perez@duoc.cl"),
                "Carlos Pérez",
                null,
                Role.TEACHER,
                UserStatus.ACTIVE);
        courseRepository = new InMemoryCourseRepository(List.of(Course.create(
                Id.create("c-1"),
                "Intro Java",
                teacher.getId(),
                Section.create("A"),
                40,
                Money.create(150000))));
        userRepository = new InMemoryUserRepository(List.of(teacher, otherTeacher));
        useCase = new UpdateCourseUseCase(courseRepository, userRepository, new ActiveTeacherLookup(userRepository));
    }

    @Test
    void updatesOnlyName() {
        CourseDTO dto = useCase.execute("c-1", "Java Avanzado", null);
        assertEquals("Java Avanzado", dto.name);
        assertEquals("u-teacher-001", dto.instructorId);
        assertEquals("A", dto.section);
    }

    @Test
    void updatesOnlyInstructor() {
        CourseDTO dto = useCase.execute("c-1", null, "u-teacher-002");
        assertEquals("Intro Java", dto.name);
        assertEquals("u-teacher-002", dto.instructorId);
        assertEquals("Carlos Pérez", dto.instructorName);
    }

    @Test
    void rejectsWhenNoFieldsProvided() {
        assertEquals(DomainError.Type.OTHER, assertThrows(DomainError.class, () -> useCase.execute("c-1", null, null))
                .getType());
    }

    @Test
    void rejectsWhenCourseNotFound() {
        assertEquals(
                DomainError.Type.NOT_FOUND,
                assertThrows(DomainError.class, () -> useCase.execute("missing", "Name", null)).getType());
    }

    @Test
    void rejectsDuplicateNameInSameSection() {
        courseRepository.save(Course.create(
                Id.create("c-2"),
                "Bases de datos",
                teacher.getId(),
                Section.create("A"),
                30,
                Money.create(120000)));
        assertEquals(
                DomainError.Type.VALIDATION,
                assertThrows(DomainError.class, () -> useCase.execute("c-1", "Bases de datos", null)).getType());
    }
}
