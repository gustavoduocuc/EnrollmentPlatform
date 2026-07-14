package com.duoc.enrollmentplatform.users.infrastructure.http;

import com.duoc.enrollmentplatform.shared.domain.DomainError;
import com.duoc.enrollmentplatform.users.application.DeleteUserUseCase;
import com.duoc.enrollmentplatform.users.application.GetUserUseCase;
import com.duoc.enrollmentplatform.users.application.ListUsersUseCase;
import com.duoc.enrollmentplatform.users.application.LoginResultDTO;
import com.duoc.enrollmentplatform.users.application.LoginUserUseCase;
import com.duoc.enrollmentplatform.users.application.PreRegisterUserUseCase;
import com.duoc.enrollmentplatform.users.application.RegisterUserUseCase;
import com.duoc.enrollmentplatform.users.application.UpdateUserUseCase;
import com.duoc.enrollmentplatform.users.application.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final PreRegisterUserUseCase preRegisterUserUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(
            PreRegisterUserUseCase preRegisterUserUseCase,
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            ListUsersUseCase listUsersUseCase,
            GetUserUseCase getUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase) {
        this.preRegisterUserUseCase = preRegisterUserUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.getUserUseCase = getUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @PostMapping("/pre-registrations")
    public ResponseEntity<?> preRegister(@RequestBody Map<String, Object> body) {
        if (!(body.get("email") instanceof String email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required and must be a string"));
        }
        String role = body.get("role") instanceof String roleValue ? roleValue : null;
        try {
            return ResponseEntity.status(201).body(preRegisterUserUseCase.execute(email, role));
        } catch (Exception error) {
            return handleError(error);
        }
    }

    @PostMapping("/registrations")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        if (!(body.get("email") instanceof String email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required and must be a string"));
        }
        if (!(body.get("fullName") instanceof String fullName)) {
            return ResponseEntity.badRequest().body(Map.of("error", "fullName is required and must be a string"));
        }
        try {
            return ResponseEntity.status(201).body(registerUserUseCase.execute(email, fullName));
        } catch (Exception error) {
            return handleError(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        if (!(body.get("email") instanceof String email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "email is required and must be a string"));
        }
        try {
            LoginResultDTO result = loginUserUseCase.execute(email);
            return ResponseEntity.ok(result);
        } catch (Exception error) {
            return handleError(error);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> list() {
        return ResponseEntity.ok(listUsersUseCase.execute());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> get(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(getUserUseCase.execute(userId));
        } catch (Exception error) {
            return handleError(error);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> update(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        if (!(body.get("fullName") instanceof String fullName)) {
            return ResponseEntity.badRequest().body(Map.of("error", "fullName is required and must be a string"));
        }
        try {
            return ResponseEntity.ok(updateUserUseCase.execute(userId, fullName));
        } catch (Exception error) {
            return handleError(error);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> delete(@PathVariable String userId) {
        try {
            deleteUserUseCase.execute(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception error) {
            return handleError(error);
        }
    }

    private ResponseEntity<?> handleError(Exception error) {
        if (error instanceof DomainError domainError) {
            int status = switch (domainError.getType()) {
                case NOT_FOUND -> 404;
                case VALIDATION -> 422;
                default -> 400;
            };
            return ResponseEntity.status(status).body(Map.of("error", domainError.getMessage()));
        }
        return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
    }
}
