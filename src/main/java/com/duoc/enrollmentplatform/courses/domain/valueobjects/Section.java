package com.duoc.enrollmentplatform.courses.domain.valueobjects;

import com.duoc.enrollmentplatform.shared.domain.DomainError;

public class Section {

    private final String value;

    private Section(String value) {
        this.value = value;
    }

    public static Section create(String raw) {
        if (raw == null || raw.isBlank()) {
            throw DomainError.validation("Section is required");
        }
        String normalized = raw.trim().toUpperCase();
        if (normalized.length() != 1 || normalized.charAt(0) < 'A' || normalized.charAt(0) > 'Z') {
            throw DomainError.validation("Section must be a single letter A-Z");
        }
        return new Section(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Section section)) {
            return false;
        }
        return value.equals(section.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
