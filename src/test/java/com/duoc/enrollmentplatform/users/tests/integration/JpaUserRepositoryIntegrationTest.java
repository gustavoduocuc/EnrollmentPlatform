package com.duoc.enrollmentplatform.users.tests.integration;

import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.infrastructure.adapters.JpaUserRepository;
import com.duoc.enrollmentplatform.users.infrastructure.adapters.UserStore;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class JpaUserRepositoryIntegrationTest {

    @Autowired
    private UserStore userStore;

    @Test
    void persistsAndRetrievesUserByEmailAndStudentId() {
        UserRepository repository = new JpaUserRepository(userStore);
        User user = User.preRegister(Email.create("nuevo.estudiante@duoc.cl"), Role.STUDENT);
        user.completeRegistration("Nuevo Estudiante");

        repository.save(user);

        assertTrue(repository.findByEmail(Email.create("nuevo.estudiante@duoc.cl")).isPresent());
        assertTrue(repository.findByStudentId(user.getStudentId()).isPresent());
        assertEquals("Nuevo Estudiante",
                repository.findById(user.getId()).orElseThrow().getFullName());
    }

    @Test
    void loadsSeededActiveStudents() {
        UserRepository repository = new JpaUserRepository(userStore);

        assertTrue(repository.findByStudentId(
                com.duoc.enrollmentplatform.shared.domain.valueobjects.Id.create("s-001")).isPresent());
        assertTrue(repository.findByEmail(Email.create("gus.dominguez@duocuc.cl")).isPresent());
    }
}
