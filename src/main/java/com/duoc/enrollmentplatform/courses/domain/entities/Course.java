package com.duoc.enrollmentplatform.courses.domain.entities;

import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;

import java.util.HashMap;
import java.util.Map;

public class Course {
    private final Id id;
    private final String name;
    private final Id instructorId;
    private final Section section;
    private final int durationHours;
    private final Money price;

    private Course(Id id, String name, Id instructorId, Section section, int durationHours, Money price) {
        this.id = id;
        this.name = name;
        this.instructorId = instructorId;
        this.section = section;
        this.durationHours = durationHours;
        this.price = price;
    }

    public static Course create(Id id, String name, Id instructorId, Section section, int durationHours, Money price) {
        if (name == null || name.isBlank()) {
            throw DomainError.validation("Course name is required");
        }
        if (instructorId == null) {
            throw DomainError.validation("Instructor id is required");
        }
        if (section == null) {
            throw DomainError.validation("Section is required");
        }
        if (durationHours <= 0) {
            throw DomainError.validation("Duration must be positive");
        }
        return new Course(id, name.trim(), instructorId, section, durationHours, price);
    }

    public Course withName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw DomainError.validation("Course name is required");
        }
        return new Course(id, newName.trim(), instructorId, section, durationHours, price);
    }

    public Course withInstructorId(Id newInstructorId) {
        if (newInstructorId == null) {
            throw DomainError.validation("Instructor id is required");
        }
        return new Course(id, name, newInstructorId, section, durationHours, price);
    }

    public Id getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Id getInstructorId() {
        return instructorId;
    }

    public Section getSection() {
        return section;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public Money getPrice() {
        return price;
    }

    public Map<String, Object> toPrimitives() {
        Map<String, Object> primitives = new HashMap<>();
        primitives.put("id", id.getValue());
        primitives.put("name", name);
        primitives.put("instructorId", instructorId.getValue());
        primitives.put("section", section.value());
        primitives.put("durationHours", durationHours);
        primitives.put("price", price.getValue());
        return primitives;
    }
}
