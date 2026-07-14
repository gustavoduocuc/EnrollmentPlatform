package com.duoc.enrollmentplatform.courses.tests.unit;

import com.duoc.enrollmentplatform.courses.domain.valueobjects.Section;
import com.duoc.enrollmentplatform.shared.domain.DomainError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TheSectionTest {

    @Test
    void acceptsValidUppercaseSectionLetter() {
        Section section = Section.create("A");

        assertEquals("A", section.value());
    }

    @Test
    void normalizesLowercaseSectionLetterToUppercase() {
        Section section = Section.create("b");

        assertEquals("B", section.value());
    }

    @Test
    void doesNotAllowBlankSection() {
        DomainError error = assertThrows(DomainError.class, () -> Section.create(""));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    @Test
    void doesNotAllowNullSection() {
        DomainError error = assertThrows(DomainError.class, () -> Section.create(null));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    @Test
    void doesNotAllowNumericSection() {
        DomainError error = assertThrows(DomainError.class, () -> Section.create("1"));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }

    @Test
    void doesNotAllowMultiCharacterSection() {
        DomainError error = assertThrows(DomainError.class, () -> Section.create("AB"));

        assertEquals(DomainError.Type.VALIDATION, error.getType());
    }
}
