package com.duoc.enrollmentplatform.courses.application;

import com.duoc.enrollmentplatform.courses.domain.entities.Course;

final class CourseMapper {
    private CourseMapper() {}

    static CourseDTO toDTO(Course course, String instructorName) {
        return new CourseDTO(
                course.getId().getValue(),
                course.getName(),
                course.getInstructorId().getValue(),
                instructorName,
                course.getSection().value(),
                course.getDurationHours(),
                course.getPrice().getValue());
    }
}
