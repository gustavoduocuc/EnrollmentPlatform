package com.duoc.enrollmentplatform.courses.tests.integration;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.courses.infrastructure.adapters.CourseStore;
import com.duoc.enrollmentplatform.courses.infrastructure.adapters.JpaCourseRepository;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class JpaCourseRepositoryIntegrationTest {

    @Autowired
    private CourseStore courseStore;

    @Test
    void persistsAndRetrievesCourse() {
        CourseRepository repository = new JpaCourseRepository(courseStore);
        Course course = Course.create(
                Id.generate(),
                "Programación Funcional",
                Id.create("u-teacher-001"),
                Section.create("F"),
                20,
                Money.create(95000));

        repository.save(course);
        List<Course> all = repository.findAll();

        assertTrue(all.stream().anyMatch(c -> c.toPrimitives().get("name").equals("Programación Funcional")));
    }

    @Test
    void returnsAllPersistedCourses() {
        CourseRepository repository = new JpaCourseRepository(courseStore);
        int initialCount = repository.findAll().size();

        repository.save(Course.create(
                Id.generate(),
                "Arquitectura Hexagonal",
                Id.create("u-teacher-001"),
                Section.create("G"),
                35,
                Money.create(200000)));

        assertEquals(initialCount + 1, repository.findAll().size());
    }

    @Test
    void findsByNameAndSection() {
        CourseRepository repository = new JpaCourseRepository(courseStore);
        assertTrue(repository.findByNameAndSection("Introducción a Java", Section.create("A")).isPresent());
    }
}
