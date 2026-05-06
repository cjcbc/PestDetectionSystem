package com.gzy.pestdetectionsystem.controller.user;

import com.gzy.pestdetectionsystem.service.user.AuthService;
import com.gzy.pestdetectionsystem.service.user.UserService;
import com.gzy.pestdetectionsystem.service.user.VerificationCodeService;
import com.gzy.pestdetectionsystem.utils.Sm2KeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerValidationTest {

    private AuthService authService;
    private Sm2KeyManager sm2KeyManager;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        UserService userService = mock(UserService.class);
        VerificationCodeService verificationCodeService = mock(VerificationCodeService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        sm2KeyManager = mock(Sm2KeyManager.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, authService, verificationCodeService, sm2KeyManager))
                .setValidator(validator)
                .build();
    }

    @Test
    void registerShouldRejectInvalidContactBeforeCallingService() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "encryptedPassword": "encrypted-password",
                                  "email": "bad-email"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void sm2PublicKeyShouldReturnPublicKeyInData() throws Exception {
        when(sm2KeyManager.getPublicKeyHex()).thenReturn("public-key");

        mockMvc.perform(get("/user/sm2-public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value("public-key"));
    }
}
