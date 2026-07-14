package com.duoc.enrollmentplatform.users.application;

public class UserDTO {
    public final String id;
    public final String email;
    public final String fullName;
    public final String studentId;
    public final String role;
    public final String status;

    public UserDTO(String id, String email, String fullName, String studentId, String role, String status) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.studentId = studentId;
        this.role = role;
        this.status = status;
    }

    public static UserDTO from(com.duoc.enrollmentplatform.users.domain.entities.User user) {
        return new UserDTO(
                user.getId().getValue(),
                user.getEmail().getValue(),
                user.getFullName(),
                user.getStudentId() == null ? null : user.getStudentId().getValue(),
                user.getRole().name(),
                user.getStatus().name());
    }
}
