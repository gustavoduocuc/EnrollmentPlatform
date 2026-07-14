package com.duoc.enrollmentplatform.courses.infrastructure.adapters;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "courses")
class CourseRecord {
    @Id @Column(name = "id") String id;
    @Column(name = "name", nullable = false) String name;
    @Column(name = "instructor_id", nullable = false) String instructorId;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "section", nullable = false, length = 1) String section;
    @Column(name = "duration_hours", nullable = false) int durationHours;
    @Column(name = "price", nullable = false, precision = 15, scale = 2) BigDecimal price;

    protected CourseRecord() {}

    CourseRecord(String id, String name, String instructorId, String section, int durationHours, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.instructorId = instructorId;
        this.section = section;
        this.durationHours = durationHours;
        this.price = price;
    }
}
