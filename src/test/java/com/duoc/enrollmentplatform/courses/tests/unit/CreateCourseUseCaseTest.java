package com.duoc.enrollmentplatform.courses.tests.unit;

import com.duoc.enrollmentplatform.courses.application.ActiveTeacherLookup;
import com.duoc.enrollmentplatform.courses.application.CourseDTO;
import com.duoc.enrollmentplatform.courses.application.CreateCourseRequest;
import com.duoc.enrollmentplatform.courses.application.CreateCourseUseCase;
import com.duoc.enrollmentplatform.courses.domain.repositories.InMemoryCourseRepository;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.InMemoryUserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateCourseUseCaseTest {
    private InMemoryCourseRepository courseRepository;
    private InMemoryUserRepository userRepository;
    private CreateCourseUseCase useCase;
    private User teacher;

    @BeforeEach
    void setup() {
        teacher = User.reconstitute(
                Id.create("u-teacher-001"),
                Email.create("maria.gonzalez@duoc.cl"),
                "María González",
                null,
                Role.TEACHER,
                UserStatus.ACTIVE);
        courseRepository = new InMemoryCourseRepository();
        userRepository = new InMemoryUserRepository(List.of(teacher));
        useCase = new CreateCourseUseCase(courseRepository, new ActiveTeacherLookup(userRepository));
    }

    private CreateCourseRequest request(String name, String section, String instructorId, int hours, double price) {
        CreateCourseRequest createRequest = new CreateCourseRequest();
        createRequest.name = name;
        createRequest.section = section;
        createRequest.instructorId = instructorId;
        createRequest.durationHours = hours;
        createRequest.price = price;
        return createRequest;
    }

    @Test
    void persistsNewCourseAndReturnsEnrichedDTO() {
        CourseDTO dto = useCase.execute(request("Bases de datos", "A", "u-teacher-001", 30, 120000));
        assertNotNull(dto.id);
        assertEquals("Bases de datos", dto.name);
        assertEquals("u-teacher-001", dto.instructorId);
        assertEquals("María González", dto.instructorName);
        assertEquals("A", dto.section);
        assertEquals(1, courseRepository.findAll().size());
    }

    @Test
    void rejectsWhenInstructorNotFound() {
        assertEquals(
                DomainError.Type.NOT_FOUND,
                assertThrows(DomainError.class, () -> useCase.execute(request("Curso", "A", "missing", 10, 100000)))
                        .getType());
    }

    @Test
    void rejectsWhenUserIsNotTeacher() {
        User student = User.reconstitute(
                Id.create("u-001"),
                Email.create("juan.soto@duoc.cl"),
                "Juan Soto",
                Id.create("s-001"),
                Role.STUDENT,
                UserStatus.ACTIVE);
        userRepository.save(student);
        assertEquals(
                DomainError.Type.VALIDATION,
                assertThrows(DomainError.class, () -> useCase.execute(request("Curso", "A", "u-001", 10, 100000)))
                        .getType());
    }

    @Test
    void rejectsWhenTeacherIsNotActive() {
        User pendingTeacher = User.reconstitute(
                Id.create("u-teacher-002"),
                Email.create("pending@duoc.cl"),
                null,
                null,
                Role.TEACHER,
                UserStatus.PENDING);
        userRepository.save(pendingTeacher);
        assertEquals(
                DomainError.Type.VALIDATION,
                assertThrows(
                                DomainError.class,
                                () -> useCase.execute(request("Curso", "A", "u-teacher-002", 10, 100000)))
                        .getType());
    }

    @Test
    void rejectsDuplicateNameAndSection() {
        useCase.execute(request("Bases de datos", "A", "u-teacher-001", 30, 120000));
        assertEquals(
                DomainError.Type.VALIDATION,
                assertThrows(
                                DomainError.class,
                                () -> useCase.execute(request("Bases de datos", "A", "u-teacher-001", 30, 120000)))
                        .getType());
    }

    @Test
    void rejectsNegativePrice() {
        assertEquals(
                DomainError.Type.VALIDATION,
                assertThrows(DomainError.class, () -> useCase.execute(request("Curso", "A", "u-teacher-001", 10, -500)))
                        .getType());
    }

    @Test
    void rejectsBlankCourseName() {
        assertThrows(DomainError.class, () -> useCase.execute(request("", "A", "u-teacher-001", 10, 100000)));
    }
}
