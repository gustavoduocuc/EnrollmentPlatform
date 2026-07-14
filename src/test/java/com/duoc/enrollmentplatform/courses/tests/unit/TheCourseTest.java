package com.duoc.enrollmentplatform.courses.tests.unit;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TheCourseTest {

    private final Id teacherId = Id.create("u-teacher-001");
    private final Section sectionA = Section.create("A");

    @Test
    void createsWithValidAttributes() {
        assertNotNull(Course.create(Id.generate(), "Cloud Native", teacherId, sectionA, 25, Money.create(180000)));
    }

    @Test
    void rejectsEmptyName() {
        assertEquals(DomainError.Type.VALIDATION, assertThrows(DomainError.class,
                () -> Course.create(Id.generate(), "", teacherId, sectionA, 25, Money.create(180000))).getType());
    }

    @Test
    void rejectsNullInstructorId() {
        assertEquals(DomainError.Type.VALIDATION, assertThrows(DomainError.class,
                () -> Course.create(Id.generate(), "Curso", null, sectionA, 25, Money.create(180000))).getType());
    }

    @Test
    void rejectsZeroDuration() {
        assertThrows(DomainError.class,
                () -> Course.create(Id.generate(), "Curso", teacherId, sectionA, 0, Money.create(180000)));
    }

    @Test
    void withNameKeepsSectionAndInstructor() {
        Course course = Course.create(Id.create("c-1"), "Intro Java", teacherId, sectionA, 40, Money.create(150000));
        Course updated = course.withName("Java Avanzado");
        assertEquals("Java Avanzado", updated.getName());
        assertEquals(sectionA, updated.getSection());
        assertEquals(teacherId, updated.getInstructorId());
    }

    @Test
    void withInstructorIdKeepsNameAndSection() {
        Id otherTeacher = Id.create("u-teacher-002");
        Course course = Course.create(Id.create("c-1"), "Intro Java", teacherId, sectionA, 40, Money.create(150000));
        Course updated = course.withInstructorId(otherTeacher);
        assertEquals(otherTeacher, updated.getInstructorId());
        assertEquals("Intro Java", updated.getName());
        assertEquals(sectionA, updated.getSection());
    }

    @Test
    void returnsPrimitives() {
        var primitives = Course.create(Id.create("c-1"), "Intro Java", teacherId, sectionA, 40, Money.create(150000))
                .toPrimitives();
        assertEquals("c-1", primitives.get("id"));
        assertEquals("Intro Java", primitives.get("name"));
        assertEquals("u-teacher-001", primitives.get("instructorId"));
        assertEquals("A", primitives.get("section"));
    }
}
