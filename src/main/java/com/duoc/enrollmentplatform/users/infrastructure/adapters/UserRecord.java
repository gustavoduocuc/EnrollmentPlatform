package com.duoc.enrollmentplatform.users.infrastructure.adapters;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
class UserRecord {
    @Id
    @Column(name = "id")
    String id;

    @Column(name = "email", nullable = false, unique = true)
    String email;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "student_id", unique = true)
    String studentId;

    @Column(name = "role", nullable = false)
    String role;

    @Column(name = "status", nullable = false)
    String status;

    protected UserRecord() {}

    UserRecord(String id, String email, String fullName, String studentId, String role, String status) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.studentId = studentId;
        this.role = role;
        this.status = status;
    }
}
