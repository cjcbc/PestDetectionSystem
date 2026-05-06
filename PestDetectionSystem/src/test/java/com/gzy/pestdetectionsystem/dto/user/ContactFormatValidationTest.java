package com.gzy.pestdetectionsystem.dto.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactFormatValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void registerShouldRejectInvalidEmail() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEncryptedPassword("encrypted-password");
        dto.setEmail("bad-email");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("邮箱"));
    }

    @Test
    void registerShouldRejectInvalidPhone() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEncryptedPassword("encrypted-password");
        dto.setPhone("12345");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("手机号"));
    }

    @Test
    void registerShouldAcceptValidEmailAndPhone() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEncryptedPassword("encrypted-password");
        dto.setEmail("test@example.com");
        dto.setPhone("13812345678");

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void bindPhoneShouldRejectInvalidPhone() {
        BindDTO dto = new BindDTO();
        dto.setBindType(BindDTO.BindType.PHONE);
        dto.setPhone("12345");

        Set<ConstraintViolation<BindDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("手机号"));
    }

    @Test
    void bindEmailShouldRejectInvalidEmail() {
        BindDTO dto = new BindDTO();
        dto.setBindType(BindDTO.BindType.EMAIL);
        dto.setEmail("bad-email");

        Set<ConstraintViolation<BindDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("邮箱"));
    }

    @Test
    void bindBothShouldRequireBothFieldsValid() {
        BindDTO dto = new BindDTO();
        dto.setBindType(BindDTO.BindType.BOTH);
        dto.setEmail("test@example.com");

        Set<ConstraintViolation<BindDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("手机号"));
    }

    @Test
    void authDtosShouldNotExposePlaintextPasswordField() {
        assertFalse(hasField(LoginDTO.class, "password"));
        assertFalse(hasField(RegisterDTO.class, "password"));
    }

    private boolean hasField(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredFields())
                .anyMatch(field -> field.getName().equals(name));
    }
}
