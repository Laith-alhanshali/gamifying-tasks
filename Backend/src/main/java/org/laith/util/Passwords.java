package org.laith.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Passwords {
    private Passwords() {}

    public static String hash(String raw) {
        if (raw == null) throw new IllegalArgumentException("password cannot be null");
        return sha256Hex(raw);
    }

    public static boolean verify(String raw, String hash) {
        if (raw == null || hash == null) return false;
        return sha256Hex(raw).equalsIgnoreCase(hash);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
