package com.gzy.pestdetectionsystem.service.user;

import com.gzy.pestdetectionsystem.config.common.UserProperties;
import com.gzy.pestdetectionsystem.dto.user.ChangePasswordDTO;
import com.gzy.pestdetectionsystem.entity.user.User;
import com.gzy.pestdetectionsystem.mapper.user.UserMapper;
import com.gzy.pestdetectionsystem.service.impl.user.UserServiceImpl;
import com.gzy.pestdetectionsystem.utils.PasswordUtil;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import com.gzy.pestdetectionsystem.utils.Sm2KeyManager;
import com.gzy.pestdetectionsystem.assembler.user.UserAssembler;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceChangePasswordTest {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final Sm2KeyManager sm2KeyManager = mock(Sm2KeyManager.class);
    private final UserServiceImpl userService = new UserServiceImpl(
            userMapper,
            mock(UserProperties.class),
            mock(RedisUtil.class),
            mock(UserAssembler.class),
            sm2KeyManager
    );

    @Test
    void changePasswordShouldAcceptEncryptedConfirmPassword() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setSalt("old-salt");
        user.setPassword(PasswordUtil.encryptPasswordSm3("old-password", user.getSalt()));
        when(userMapper.selectByIdForUpdate(userId)).thenReturn(user);
        when(sm2KeyManager.decrypt(any())).thenAnswer(invocation -> {
            byte[] cipherData = invocation.getArgument(0);
            String marker = new String(cipherData, StandardCharsets.UTF_8);
            if (marker.startsWith("old")) {
                return "old-password".getBytes(StandardCharsets.UTF_8);
            }
            return "new-password".getBytes(StandardCharsets.UTF_8);
        });

        userService.changePassword(userId, new ChangePasswordDTO(
                base64Cipher("old-cipher"),
                base64Cipher("new-cipher"),
                base64Cipher("confirm-cipher")
        ));

        verify(userMapper).updateById(user);
        assertTrue(PasswordUtil.verifyPasswordSm3("new-password", user.getSalt(), user.getPassword()));
    }

    private String base64Cipher(String marker) {
        String paddedMarker = marker.repeat(8);
        return Base64.getEncoder().encodeToString(paddedMarker.getBytes(StandardCharsets.UTF_8));
    }
}
