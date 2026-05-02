package com.gzy.pestdetectionsystem.annotation;

import com.gzy.pestdetectionsystem.annotation.validator.ContactValidator;
import com.gzy.pestdetectionsystem.annotation.validator.VerifyType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContactValidator.class)
public @interface ContactCheck {
    VerifyType type();
    boolean required() default true;

    String message() default "格式错误";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
