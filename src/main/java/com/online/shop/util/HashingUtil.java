package com.online.shop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class HashingUtil {

    private HashingUtil() { }

    public static byte[] getSHA1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unable to get 'SHA-1'");
        }
    }

    public static byte[] getSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unable to get 'SHA-256'");
        }
    }

    public static String getHexStrFromBytes(byte[] input) {
        StringBuilder sb = new StringBuilder();
        for (byte b : input) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String getBase64StrFromBytes(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    public static byte[] getBase64BytesFromStr(String base64Str) {
        return Base64.getDecoder().decode(base64Str);
    }

}
