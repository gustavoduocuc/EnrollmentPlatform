package com.duoc.enrollmentplatform.users.tests.unit;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.users.application.DeleteUserUseCase;
import com.duoc.enrollmentplatform.users.application.GetUserUseCase;
import com.duoc.enrollmentplatform.users.application.ListUsersUseCase;
import com.duoc.enrollmentplatform.users.application.PreRegisterUserUseCase;
import com.duoc.enrollmentplatform.users.application.UpdateUserUseCase;
import com.duoc.enrollmentplatform.users.application.UserDTO;
import com.duoc.enrollmentplatform.users.domain.repositories.InMemoryUserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TheUserAdminCrudTest {

    @Test
    void listsAllUsers() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        new PreRegisterUserUseCase(repository).execute("a@duoc.cl", "STUDENT");
        new PreRegisterUserUseCase(repository).execute("b@duoc.cl", "TEACHER");

        List<UserDTO> users = new ListUsersUseCase(repository).execute();

        assertEquals(2, users.size());
    }

    @Test
    void getsUserById() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UserDTO created = new PreRegisterUserUseCase(repository).execute("a@duoc.cl", "ADMIN");

        UserDTO found = new GetUserUseCase(repository).execute(created.id);

        assertEquals(created.email, found.email);
        assertEquals("ADMIN", found.role);
    }

    @Test
    void updatesUserFullName() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UserDTO created = new PreRegisterUserUseCase(repository).execute("a@duoc.cl", "TEACHER");

        UserDTO updated = new UpdateUserUseCase(repository).execute(created.id, "Ana Ruiz");

        assertEquals("Ana Ruiz", updated.fullName);
    }

    @Test
    void deletesUser() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UserDTO created = new PreRegisterUserUseCase(repository).execute("a@duoc.cl", "STUDENT");

        new DeleteUserUseCase(repository).execute(created.id);

        assertEquals(DomainError.Type.NOT_FOUND,
                assertThrows(DomainError.class, () -> new GetUserUseCase(repository).execute(created.id)).getType());
    }
}
