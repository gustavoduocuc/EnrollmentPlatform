package com.duoc.enrollmentplatform.enrollment.infrastructure.adapters;

import com.duoc.enrollmentplatform.enrollment.domain.entities.Enrollment;
import com.duoc.enrollmentplatform.enrollment.domain.entities.EnrollmentLine;
import com.duoc.enrollmentplatform.enrollment.domain.repositories.EnrollmentRepository;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Id;
import com.duoc.enrollmentplatform.shared.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class JpaEnrollmentRepository implements EnrollmentRepository {
    private final EnrollmentStore store;
    public JpaEnrollmentRepository(EnrollmentStore store) { this.store = store; }

    @Override
    public void save(Enrollment enrollment) {
        List<EnrollmentLineRecord> lineRecords = enrollment.getLines().stream()
                .map(l -> new EnrollmentLineRecord(l.getId().getValue(), l.getCourseId().getValue(),
                        l.getCourseName(), BigDecimal.valueOf(l.getUnitPrice().getValue())))
                .toList();
        store.save(new EnrollmentRecord(enrollment.getId().getValue(),
                enrollment.getStudentId().getValue(), enrollment.getEnrolledAt(),
                BigDecimal.valueOf(enrollment.calculateTotal().getValue()), lineRecords));
    }

    @Override
    public Optional<Enrollment> findById(Id id) { return store.findById(id.getValue()).map(this::toDomain); }

    @Override
    public List<Enrollment> findAll() {
        return store.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(Id id) {
        store.deleteById(id.getValue());
    }

    @Override
    public boolean existsByCourseId(Id courseId) {
        return store.existsByCourseId(courseId.getValue());
    }

    private Enrollment toDomain(EnrollmentRecord r) {
        List<EnrollmentLine> lines = r.lines.stream()
                .map(l -> EnrollmentLine.create(Id.create(l.id), Id.create(l.courseId),
                        l.courseName, Money.create(l.unitPrice.doubleValue())))
                .toList();
        return Enrollment.reconstitute(Id.create(r.id), Id.create(r.studentId), lines, r.enrolledAt);
    }
}
