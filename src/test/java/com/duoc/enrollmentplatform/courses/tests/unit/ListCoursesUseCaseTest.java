package com.duoc.enrollmentplatform.courses.tests.unit;

import com.duoc.enrollmentplatform.courses.application.CourseDTO;
import com.duoc.enrollmentplatform.courses.application.ListCoursesUseCase;
import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.InMemoryCourseRepository;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.InMemoryUserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListCoursesUseCaseTest {
    private final User teacher = User.reconstitute(
            Id.create("u-teacher-001"),
            Email.create("maria.gonzalez@duoc.cl"),
            "María González",
            null,
            Role.TEACHER,
            UserStatus.ACTIVE);

    @Test
    void returnsEmptyCatalogWhenNoCourses() {
        assertTrue(new ListCoursesUseCase(new InMemoryCourseRepository(), new InMemoryUserRepository())
                .execute()
                .isEmpty());
    }

    @Test
    void returnsAllAvailableCourses() {
        var courses = List.of(Course.create(
                Id.generate(), "Cloud Native", teacher.getId(), Section.create("A"), 25, Money.create(180000)));
        assertEquals(
                1,
                new ListCoursesUseCase(new InMemoryCourseRepository(courses), new InMemoryUserRepository(List.of(teacher)))
                        .execute()
                        .size());
    }

    @Test
    void returnsDetailedCourseInformation() {
        var course = Course.create(
                Id.create("c-1"),
                "Introducción a Java",
                teacher.getId(),
                Section.create("A"),
                40,
                Money.create(150000));
        CourseDTO dto = new ListCoursesUseCase(
                        new InMemoryCourseRepository(List.of(course)), new InMemoryUserRepository(List.of(teacher)))
                .execute()
                .get(0);
        assertEquals("c-1", dto.id);
        assertEquals("Introducción a Java", dto.name);
        assertEquals("u-teacher-001", dto.instructorId);
        assertEquals("María González", dto.instructorName);
        assertEquals("A", dto.section);
        assertEquals(40, dto.durationHours);
        assertEquals(150000.0, dto.price);
    }
}
