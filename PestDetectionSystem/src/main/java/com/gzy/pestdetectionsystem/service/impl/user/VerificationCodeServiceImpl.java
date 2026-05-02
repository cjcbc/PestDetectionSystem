package com.gzy.pestdetectionsystem.service.impl.user;

import com.gzy.pestdetectionsystem.exception.BusinessException;
import com.gzy.pestdetectionsystem.exception.CommonErrorCode;
import com.gzy.pestdetectionsystem.service.user.VerificationCodeService;
import com.gzy.pestdetectionsystem.utils.CreateVerificationCode;
import com.gzy.pestdetectionsystem.utils.RedisUtil;
import com.gzy.pestdetectionsystem.vo.user.VerificationCodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final String CACHE_PREFIX = "user:verification-code:";
    private static final long TTL_SECONDS = 300L;

    private final CreateVerificationCode createVerificationCode;
    private final RedisUtil redisUtil;

    @Override
    public VerificationCodeVO create() {
        CreateVerificationCode.VerificationCodeImage codeImage = createVerificationCode.create();
        String verificationCodeId = UUID.randomUUID().toString().replace("-", "");
        boolean stored = redisUtil.set(cacheKey(verificationCodeId),
                codeImage.code().toUpperCase(Locale.ROOT),
                TTL_SECONDS);
        if (!stored) {
            throw new BusinessException(CommonErrorCode.VERIFICATION_CODE_STORE_FAILED);
        }
        return new VerificationCodeVO(verificationCodeId, toDataUri(codeImage));
    }

    @Override
    public void validate(String verificationCodeId, String verificationCode) {
        if (isBlank(verificationCodeId) || isBlank(verificationCode)) {
            throw new BusinessException(CommonErrorCode.VERIFICATION_CODE_REQUIRED);
        }
        String key = cacheKey(verificationCodeId);
        String cached = redisUtil.get(key, String.class);
        if (cached != null) {
            redisUtil.del(key);
        }
        if (cached == null || !cached.equalsIgnoreCase(verificationCode.trim())) {
            throw new BusinessException(CommonErrorCode.VERIFICATION_CODE_INVALID);
        }
    }

    private String cacheKey(String verificationCodeId) {
        return CACHE_PREFIX + verificationCodeId;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String toDataUri(CreateVerificationCode.VerificationCodeImage codeImage) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(codeImage.image(), "png", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.VERIFICATION_CODE_STORE_FAILED, "验证码图片生成失败");
        }
    }
}
