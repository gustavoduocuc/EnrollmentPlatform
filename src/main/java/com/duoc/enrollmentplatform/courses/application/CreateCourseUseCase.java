package com.duoc.enrollmentplatform.courses.application;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;
import com.duoc.enrollmentplatform.courses.domain.repositories.CourseRepository;
import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;
import com.duoc.enrollmentplatform.users.domain.entities.User;

public class CreateCourseUseCase {
    private final CourseRepository courseRepository;
    private final ActiveTeacherLookup activeTeacherLookup;

    public CreateCourseUseCase(CourseRepository courseRepository, ActiveTeacherLookup activeTeacherLookup) {
        this.courseRepository = courseRepository;
        this.activeTeacherLookup = activeTeacherLookup;
    }

    public CourseDTO execute(CreateCourseRequest request) {
        Section section = Section.create(request.section);
        User teacher = activeTeacherLookup.require(Id.create(request.instructorId));
        if (courseRepository.findByNameAndSection(request.name, section).isPresent()) {
            throw DomainError.validation(
                    "Course \"" + request.name + "\" section " + section.value() + " already exists");
        }
        Course course = Course.create(
                Id.generate(),
                request.name,
                teacher.getId(),
                section,
                request.durationHours,
                Money.create(request.price));
        courseRepository.save(course);
        return CourseMapper.toDTO(course, teacher.getFullName());
    }
}
