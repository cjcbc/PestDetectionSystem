package com.gzy.pestdetectionsystem.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "绑定参数")
public class BindDTO {
    private Long id;// 唯一标识符
    private String phone;
    private String email;

    private BindType bindType;

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

        public String toValue() {
            return this.name();
        }
    }
}
