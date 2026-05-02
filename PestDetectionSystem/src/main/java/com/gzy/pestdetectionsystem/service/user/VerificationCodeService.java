package com.gzy.pestdetectionsystem.service.user;

import com.gzy.pestdetectionsystem.vo.user.VerificationCodeVO;

public interface VerificationCodeService {

    VerificationCodeVO create();

    void validate(String verificationCodeId, String verificationCode);
}
