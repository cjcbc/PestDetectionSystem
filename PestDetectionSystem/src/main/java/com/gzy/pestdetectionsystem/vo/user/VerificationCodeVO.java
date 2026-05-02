package com.gzy.pestdetectionsystem.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCodeVO {

    private String verificationCodeId;

    private String image;
}
