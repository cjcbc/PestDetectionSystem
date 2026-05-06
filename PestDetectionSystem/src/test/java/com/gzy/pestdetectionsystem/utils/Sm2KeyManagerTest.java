package com.gzy.pestdetectionsystem.utils;

import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Sm2KeyManagerTest {

    @TempDir
    Path keyDir;

    @Test
    void initShouldReloadPersistedPublicKey() {
        Sm2KeyManager first = new Sm2KeyManager();
        ReflectionTestUtils.setField(first, "keyPath", keyDir.toString());
        first.init();
        String firstPublicKey = first.getPublicKeyHex();

        Sm2KeyManager second = new Sm2KeyManager();
        ReflectionTestUtils.setField(second, "keyPath", keyDir.toString());
        second.init();

        assertEquals(firstPublicKey, second.getPublicKeyHex());
    }

    @Test
    void decryptShouldSupportC1C3C2Ciphertext() {
        Sm2KeyManager keyManager = new Sm2KeyManager();
        ReflectionTestUtils.setField(keyManager, "keyPath", keyDir.toString());
        keyManager.init();

        byte[] plaintext = "login-password".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(
                plaintext,
                keyManager.decrypt(encrypt(keyManager, plaintext, SM2Engine.Mode.C1C3C2))
        );
    }

    @Test
    void decryptShouldSupportC1C2C3Ciphertext() {
        Sm2KeyManager keyManager = new Sm2KeyManager();
        ReflectionTestUtils.setField(keyManager, "keyPath", keyDir.toString());
        keyManager.init();

        byte[] plaintext = "login-password".getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(
                plaintext,
                keyManager.decrypt(encrypt(keyManager, plaintext, SM2Engine.Mode.C1C2C3))
        );
    }

    private byte[] encrypt(Sm2KeyManager keyManager, byte[] plaintext, SM2Engine.Mode mode) {
        ECPublicKeyParameters publicKey = Sm2Util.createPublicKeyFromHex(
                keyManager.getPublicKeyHex(),
                keyManager.getDomainParams()
        );
        try {
            SM2Engine engine = new SM2Engine(mode);
            engine.init(true, new ParametersWithRandom(publicKey, new SecureRandom()));
            return engine.processBlock(plaintext, 0, plaintext.length);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
