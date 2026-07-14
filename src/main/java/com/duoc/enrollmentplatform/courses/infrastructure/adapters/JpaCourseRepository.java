package com.duoc.enrollmentplatform.courses.infrastructure.adapters;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class JpaCourseRepository implements CourseRepository {
    private final CourseStore store;

    public JpaCourseRepository(CourseStore store) {
        this.store = store;
    }

    @Override
    public void save(Course course) {
        store.save(new CourseRecord(
                course.getId().getValue(),
                course.getName(),
                course.getInstructorId().getValue(),
                course.getSection().value(),
                course.getDurationHours(),
                BigDecimal.valueOf(course.getPrice().getValue())));
    }

    @Override
    public Optional<Course> findById(Id id) {
        return store.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public List<Course> findAll() {
        return store.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Course> findByNameAndSection(String name, Section section) {
        return store.findByNameAndSection(name, section.value()).map(this::toDomain);
    }

    @Override
    public void delete(Id id) {
        store.deleteById(id.getValue());
    }

    private Course toDomain(CourseRecord record) {
        return Course.create(
                Id.create(record.id),
                record.name,
                Id.create(record.instructorId),
                Section.create(record.section),
                record.durationHours,
                Money.create(record.price.doubleValue()));
    }
}
