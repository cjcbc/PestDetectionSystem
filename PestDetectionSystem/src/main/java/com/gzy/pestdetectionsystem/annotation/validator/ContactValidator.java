package com.gzy.pestdetectionsystem.annotation.validator;

import com.gzy.pestdetectionsystem.annotation.ContactCheck;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContactValidator implements ConstraintValidator<ContactCheck, String> {
    private static final String PHONE_REGEX = "^1\\d{10}$";
    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private VerifyType type;
    private boolean required;

    @Override
    public void initialize(ContactCheck constraintAnnotation) {
        this.type = constraintAnnotation.type();
        this.required = constraintAnnotation.required();
    }


    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null || s.isBlank()) {
            if (required) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("不能为空")
                        .addConstraintViolation();
                return false;
            } else {
                //允许跳过
                return true;
            }
        }

        //非空判断
        String regex = (type == VerifyType.PHONE) ? PHONE_REGEX : EMAIL_REGEX;
        return s.matches(regex);
    }
}
