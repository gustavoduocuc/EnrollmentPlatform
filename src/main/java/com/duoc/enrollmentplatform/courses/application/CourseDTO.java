package com.duoc.enrollmentplatform.courses.application;

public class CourseDTO {
    public String id;
    public String name;
    public String instructorId;
    public String instructorName;
    public String section;
    public int durationHours;
    public double price;

    public CourseDTO(
            String id,
            String name,
            String instructorId,
            String instructorName,
            String section,
            int durationHours,
            double price) {
        this.id = id;
        this.name = name;
        this.instructorId = instructorId;
        this.instructorName = instructorName;
        this.section = section;
        this.durationHours = durationHours;
        this.price = price;
    }
}
