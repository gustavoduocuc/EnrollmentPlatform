package com.duoc.enrollmentplatform.users.domain.entities;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;

import java.util.HashMap;
import java.util.Map;

public class User {
    private final Id id;
    private final Email email;
    private String fullName;
    private final Id studentId;
    private final Role role;
    private UserStatus status;

    private User(Id id, Email email, String fullName, Id studentId, Role role, UserStatus status) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.studentId = studentId;
        this.role = role;
        this.status = status;
    }

    public static User preRegister(Email email, Role role) {
        Role effectiveRole = role == null ? Role.STUDENT : role;
        Id studentId = effectiveRole == Role.STUDENT ? Id.generate() : null;
        return new User(Id.generate(), email, null, studentId, effectiveRole, UserStatus.PENDING);
    }

    public static User reconstitute(
            Id id, Email email, String fullName, Id studentId, Role role, UserStatus status) {
        if (role == Role.STUDENT && studentId == null) {
            throw DomainError.validation("Student id is required for STUDENT role");
        }
        if (role != Role.STUDENT && studentId != null) {
            throw DomainError.validation("Student id must be null when role is not STUDENT");
        }
        return new User(id, email, fullName, studentId, role, status);
    }

    public void completeRegistration(String fullName) {
        if (status != UserStatus.PENDING) {
            throw DomainError.validation("Only pending users can complete registration");
        }
        if (fullName == null || fullName.isBlank()) {
            throw DomainError.validation("Full name is required");
        }
        this.fullName = fullName;
        this.status = UserStatus.ACTIVE;
    }

    public void updateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw DomainError.validation("Full name is required");
        }
        this.fullName = fullName;
    }

    public void requireActiveTeacher() {
        if (role != Role.TEACHER) {
            throw DomainError.validation("User " + id.getValue() + " is not a TEACHER");
        }
        if (status != UserStatus.ACTIVE) {
            throw DomainError.validation("Teacher " + id.getValue() + " is not ACTIVE");
        }
    }

    public Id getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Id getStudentId() {
        return studentId;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Map<String, Object> toPrimitives() {
        Map<String, Object> primitives = new HashMap<>();
        primitives.put("id", id.getValue());
        primitives.put("email", email.getValue());
        primitives.put("fullName", fullName);
        primitives.put("studentId", studentId == null ? null : studentId.getValue());
        primitives.put("role", role.name());
        primitives.put("status", status.name());
        return primitives;
    }
}
