package com.mixc.smartconcierge.util;

import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class CryptoUtil {

    private final AES aes;

    public CryptoUtil(@Value("${app.crypto.aes-key}") String aesKey) {
        byte[] keyBytes = Arrays.copyOf(aesKey.getBytes(StandardCharsets.UTF_8), 16);
        this.aes = new AES(keyBytes);
    }

    public byte[] encryptPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return aes.encrypt(phone);
    }

    public String decryptPhone(byte[] encrypted) {
        if (encrypted == null || encrypted.length == 0) {
            return null;
        }
        return aes.decryptStr(encrypted);
    }

    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
