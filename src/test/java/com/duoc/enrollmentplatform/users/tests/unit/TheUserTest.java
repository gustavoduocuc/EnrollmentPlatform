package com.duoc.enrollmentplatform.users.tests.unit;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TheUserTest {

    @Test
    void acceptsPendingStudentPreRegistrationWithAssignedStudentId() {
        Email email = Email.create("juan.soto@duoc.cl");

        User user = User.preRegister(email, Role.STUDENT);

        assertEquals(UserStatus.PENDING, user.getStatus());
        assertEquals(Role.STUDENT, user.getRole());
        assertNotNull(user.getStudentId());
        assertNull(user.getFullName());
        assertNotNull(user.getId());
    }

    @Test
    void allowsTeacherPreRegistrationWithoutStudentId() {
        Email email = Email.create("profesor@duoc.cl");

        User user = User.preRegister(email, Role.TEACHER);

        assertEquals(Role.TEACHER, user.getRole());
        assertNull(user.getStudentId());
        assertEquals(UserStatus.PENDING, user.getStatus());
    }

    @Test
    void allowsAdminPreRegistrationWithoutStudentId() {
        Email email = Email.create("admin@duoc.cl");

        User user = User.preRegister(email, Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
        assertNull(user.getStudentId());
    }

    @Test
    void assignsStudentRoleWhenRoleIsOmitted() {
        Email email = Email.create("nuevo@duoc.cl");

        User user = User.preRegister(email, null);

        assertEquals(Role.STUDENT, user.getRole());
        assertNotNull(user.getStudentId());
    }

    @Test
    void validatesStudentIdentityRequiresStudentId() {
        Id userId = Id.generate();
        Email email = Email.create("juan@duoc.cl");

        DomainError error = assertThrows(DomainError.class, () -> User.reconstitute(
                userId,
                email,
                null,
                null,
                Role.STUDENT,
                UserStatus.PENDING));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    @Test
    void activatesUserAfterCompletingRegistrationWithFullName() {
        User user = pendingStudent("juan.soto@duoc.cl");

        user.completeRegistration("Juan Soto");

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("Juan Soto", user.getFullName());
    }

    @Test
    void doesNotAllowCompletingRegistrationWithoutFullName() {
        User user = pendingStudent("juan.soto@duoc.cl");

        DomainError error = assertThrows(DomainError.class, () -> user.completeRegistration("  "));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    @Test
    void doesNotAllowCompletingRegistrationTwice() {
        User user = pendingStudent("juan.soto@duoc.cl");
        user.completeRegistration("Juan Soto");

        DomainError error = assertThrows(DomainError.class, () -> user.completeRegistration("Otro Nombre"));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    private User pendingStudent(String email) {
        return User.preRegister(Email.create(email), Role.STUDENT);
    }
}
