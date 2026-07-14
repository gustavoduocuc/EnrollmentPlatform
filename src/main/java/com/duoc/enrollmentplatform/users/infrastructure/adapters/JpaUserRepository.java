package com.duoc.enrollmentplatform.users.infrastructure.adapters;

import com.duoc.enrollmentplatform.shared.domain.valueobjects.Email;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.users.domain.entities.User;
import com.duoc.enrollmentplatform.users.domain.repositories.UserRepository;
import com.duoc.enrollmentplatform.users.domain.valueobjects.Role;
import com.duoc.enrollmentplatform.users.domain.valueobjects.UserStatus;

import java.util.List;
import java.util.Optional;

public class JpaUserRepository implements UserRepository {
    private final UserStore store;

    public JpaUserRepository(UserStore store) {
        this.store = store;
    }

    @Override
    public void save(User user) {
        store.save(toRecord(user));
    }

    @Override
    public Optional<User> findById(Id id) {
        return store.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return store.findByEmail(email.getValue()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByStudentId(Id studentId) {
        return store.findByStudentId(studentId.getValue()).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return store.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(Id id) {
        store.deleteById(id.getValue());
    }

    private UserRecord toRecord(User user) {
        return new UserRecord(
                user.getId().getValue(),
                user.getEmail().getValue(),
                user.getFullName(),
                user.getStudentId() == null ? null : user.getStudentId().getValue(),
                user.getRole().name(),
                user.getStatus().name());
    }

    private User toDomain(UserRecord record) {
        return User.reconstitute(
                Id.create(record.id),
                Email.create(record.email),
                record.fullName,
                record.studentId == null ? null : Id.create(record.studentId),
                Role.valueOf(record.role),
                UserStatus.valueOf(record.status));
    }
}
