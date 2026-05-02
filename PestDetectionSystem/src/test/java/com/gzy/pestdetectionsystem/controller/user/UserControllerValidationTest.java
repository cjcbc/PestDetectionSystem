package com.gzy.pestdetectionsystem.controller.user;

import com.gzy.pestdetectionsystem.service.user.AuthService;
import com.gzy.pestdetectionsystem.service.user.UserService;
import com.gzy.pestdetectionsystem.service.user.VerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerValidationTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        UserService userService = mock(UserService.class);
        VerificationCodeService verificationCodeService = mock(VerificationCodeService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, authService, verificationCodeService))
                .setValidator(validator)
                .build();
    }

    @Test
    void registerShouldRejectInvalidContactBeforeCallingService() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "password": "123456",
                                  "email": "bad-email"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
