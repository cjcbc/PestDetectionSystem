package com.gzy.pestdetectionsystem.dto.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.gzy.pestdetectionsystem.annotation.ContactCheck;
import com.gzy.pestdetectionsystem.annotation.validator.VerifyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
@Schema(description = "绑定参数")
public class BindDTO {
    private String id;// 唯一标识符

    @ContactCheck(type = VerifyType.PHONE, required = false, message = "手机号格式错误")
    private String phone;
    @ContactCheck(type = VerifyType.EMAIL, required = false, message = "邮箱格式错误")
    private String email;

    private BindType bindType;

    @AssertTrue(message = "手机号不能为空")
    @Schema(hidden = true)
    public boolean isPhonePresentWhenRequired() {
        return bindType != BindType.PHONE && bindType != BindType.BOTH || phone != null && !phone.isBlank();
    }

    @AssertTrue(message = "邮箱不能为空")
    @Schema(hidden = true)
    public boolean isEmailPresentWhenRequired() {
        return bindType != BindType.EMAIL && bindType != BindType.BOTH || email != null && !email.isBlank();
    }

    public enum BindType{
        PHONE,
        EMAIL,
        BOTH;

        @JsonCreator
        public static BindType forString(String value){
            if(value == null){
                return null;
            }
            try{
                return BindType.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e){
                return null;
            }
        }
    }
}
