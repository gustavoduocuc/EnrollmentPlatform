package com.duoc.enrollmentplatform.users.tests.unit;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.users.application.LoginResultDTO;
import com.duoc.enrollmentplatform.users.application.LoginUserUseCase;
import com.duoc.enrollmentplatform.users.application.PreRegisterUserUseCase;
import com.duoc.enrollmentplatform.users.application.RegisterUserUseCase;
import com.duoc.enrollmentplatform.users.application.UserDTO;
import com.duoc.enrollmentplatform.users.application.ports.IdentityTenantRegister;
import com.duoc.enrollmentplatform.users.domain.repositories.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TheUserRegistrationFlowTest {

    @Test
    void acceptsStudentPreRegistrationWithDefaultRoleAndStudentId() {
        InMemoryUserRepository repository = emptyUserRepository();

        UserDTO user = new PreRegisterUserUseCase(repository).execute("juan.soto@duoc.cl", null);

        assertEquals("PENDING", user.status);
        assertEquals("STUDENT", user.role);
        assertNotNull(user.studentId);
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void doesNotAllowPreRegisteringDuplicateEmail() {
        InMemoryUserRepository repository = emptyUserRepository();
        PreRegisterUserUseCase preRegister = new PreRegisterUserUseCase(repository);
        preRegister.execute("juan.soto@duoc.cl", "STUDENT");

        DomainError error = assertThrows(DomainError.class,
                () -> preRegister.execute("juan.soto@duoc.cl", "TEACHER"));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    @Test
    void completesRegistrationForPendingUserInIdentityTenant() {
        InMemoryUserRepository repository = emptyUserRepository();
        RecordingIdentityTenantRegister identityTenantRegister = new RecordingIdentityTenantRegister();
        preRegisterStudent(repository, "juan.soto@duoc.cl");

        UserDTO user = new RegisterUserUseCase(repository, identityTenantRegister)
                .execute("juan.soto@duoc.cl", "Juan Soto");

        assertEquals("ACTIVE", user.status);
        assertEquals("Juan Soto", user.fullName);
        assertEquals(List.of("juan.soto@duoc.cl|Juan Soto"), identityTenantRegister.recordedProvisions());
    }

    @Test
    void doesNotAllowRegistrationWithoutPreRegistration() {
        InMemoryUserRepository repository = emptyUserRepository();

        DomainError error = assertThrows(DomainError.class,
                () -> new RegisterUserUseCase(repository, new RecordingIdentityTenantRegister())
                        .execute("desconocido@duoc.cl", "Nombre"));

        assertEquals(DomainError.Type.NOT_FOUND, error.getType());
    }

    @Test
    void allowsLoginForActiveUser() {
        InMemoryUserRepository repository = emptyUserRepository();
        preRegisterStudent(repository, "juan.soto@duoc.cl");
        completeRegistration(repository, "juan.soto@duoc.cl", "Juan Soto");

        LoginResultDTO result = new LoginUserUseCase(repository).execute("juan.soto@duoc.cl");

        assertTrue(result.ready);
        assertEquals("STUDENT", result.role);
    }

    @Test
    void doesNotAllowLoginForPendingUser() {
        InMemoryUserRepository repository = emptyUserRepository();
        preRegisterStudent(repository, "juan.soto@duoc.cl");

        DomainError error = assertThrows(DomainError.class,
                () -> new LoginUserUseCase(repository).execute("juan.soto@duoc.cl"));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    private InMemoryUserRepository emptyUserRepository() {
        return new InMemoryUserRepository();
    }

    private void preRegisterStudent(InMemoryUserRepository repository, String email) {
        new PreRegisterUserUseCase(repository).execute(email, "STUDENT");
    }

    private void completeRegistration(InMemoryUserRepository repository, String email, String fullName) {
        new RegisterUserUseCase(repository, new RecordingIdentityTenantRegister())
                .execute(email, fullName);
    }

    private static class RecordingIdentityTenantRegister implements IdentityTenantRegister {
        private final List<String> provisions = new ArrayList<>();

        @Override
        public void provision(String email, String fullName) {
            provisions.add(email + "|" + fullName);
        }

        private List<String> recordedProvisions() {
            return List.copyOf(provisions);
        }
    }
}
