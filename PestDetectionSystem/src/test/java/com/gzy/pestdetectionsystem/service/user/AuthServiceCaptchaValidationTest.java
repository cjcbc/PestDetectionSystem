package com.gzy.pestdetectionsystem.service.user;

import com.gzy.pestdetectionsystem.dto.user.LoginDTO;
import com.gzy.pestdetectionsystem.dto.user.RegisterDTO;
import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.mapper.user.UserMapper;
import com.gzy.pestdetectionsystem.service.impl.user.AuthServiceImpl;
import com.gzy.pestdetectionsystem.utils.SnowflakeIdGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthServiceCaptchaValidationTest {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final SnowflakeIdGenerator snowflakeIdGenerator = mock(SnowflakeIdGenerator.class);
    private final UserService userService = mock(UserService.class);
    private final VerificationCodeService verificationCodeService = mock(VerificationCodeService.class);
    private final AuthServiceImpl authService = new AuthServiceImpl(
            userMapper,
            snowflakeIdGenerator,
            userService,
            verificationCodeService
    );

    @Test
    void loginShouldValidateCaptchaBeforeLoadingUser() {
        LoginDTO dto = new LoginDTO();
        dto.setAccount("test@example.com");
        dto.setPassword("123456");
        dto.setVerificationCodeId("captcha-id");
        dto.setVerificationCode("bad");
        doThrow(new BusinessException(CommonErrorCode.VERIFICATION_CODE_INVALID))
                .when(verificationCodeService)
                .validate("captcha-id", "bad");

        assertThrows(BusinessException.class, () -> authService.login(dto));

        verifyNoInteractions(userMapper);
        verifyNoInteractions(userService);
    }

    @Test
    void registerShouldValidateCaptchaBeforeAccountChecks() {
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("123456");
        dto.setVerificationCodeId("captcha-id");
        dto.setVerificationCode("bad");
        doThrow(new BusinessException(CommonErrorCode.VERIFICATION_CODE_INVALID))
                .when(verificationCodeService)
                .validate("captcha-id", "bad");

        assertThrows(BusinessException.class, () -> authService.register(dto));

        verifyNoInteractions(userMapper);
        verifyNoInteractions(userService);
    }
}
