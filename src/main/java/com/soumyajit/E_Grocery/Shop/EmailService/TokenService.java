package com.soumyajit.E_Grocery.Shop.EmailService;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenService {
    private final Map<String, String> codeStore = new HashMap<>();
    private final long codeExpirationTime = 30 * 60 * 1000; // 30 minutes
    private final Map<String, Long> codeExpirationStore = new HashMap<>();

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 8; // Length of the token

    public String createResetCode(String email) {
        String code = generateRandomToken();
        codeStore.put(code, email);
        codeExpirationStore.put(code, System.currentTimeMillis() + codeExpirationTime);
        return code;
    }

    public Optional<String> getEmailByCode(String code) {
        Long expirationTime = codeExpirationStore.get(code);
        if (expirationTime != null && expirationTime > System.currentTimeMillis()) {
            return Optional.ofNullable(codeStore.get(code));
        } else {
            codeStore.remove(code);
            codeExpirationStore.remove(code);
            return Optional.empty();
        }
    }

    public void invalidateCode(String code) {
        codeStore.remove(code);
        codeExpirationStore.remove(code);
    }

    private String generateRandomToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }
}
