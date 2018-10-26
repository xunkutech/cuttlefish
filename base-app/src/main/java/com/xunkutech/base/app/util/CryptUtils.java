package com.xunkutech.base.app.util;

import org.soulwing.crypt4j.Crypt;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * We use random seed to generate hash for client validation. It is safe in standalone server.
 * But for server cluster. We have to share the seed among the servers and use AES encrypt
 * method instead.
 */
public class CryptUtils {

    static final char[] ALPHABET_64 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz./".toCharArray();
//    static final String RANDOM_SEED = randomSeed();
//
//    static String randomSeed() {
//        byte[] seed = new byte[32];
//        new SecureRandom().nextBytes(seed);
//        return new String(seed);
//    }

    static String randomSalt() {
        int len = 6;
        byte[] bb = new byte[len];
        new SecureRandom().nextBytes(bb);
//        return CodecUtils.toBase64(bb).replace("+", ".");
        char[] salt = "$6$12345678".toCharArray();
        int j = 3;
        while (len > 0) {
            int n = (bb[--len] << 16 & 0x00ff0000) | (bb[--len] << 8 & 0x0000ff00) | (bb[--len] & 0x000000ff);
            for (int i = 0; i < 4; i++) {
                salt[j++] = ALPHABET_64[n & 0x0000003f];
                n >>>= 6;
            }
        }
        return new String(salt);
    }

    public static String crypt(String password) {
        return crypt(password, randomSalt());
    }

    public static String crypt(String password, String salt) {
        Objects.requireNonNull(password);
        try {
            return Crypt.crypt(password.toCharArray(), "$6$" + salt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Can not happen here.");
    }

    public static boolean validate(String password, String hashValue) {
        if (null == password || password.isEmpty()) return false;
        if (null == hashValue || hashValue.isEmpty()) return false;
        try {
            return Crypt.validate(password.toCharArray(), hashValue);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
//        long start = System.nanoTime();
//        for (int i = 0; i < 100000; i++) System.out.println(randomSalt());
//        long end = System.nanoTime();
//        System.out.println((end - start));
        System.out.println(crypt("992a861bbebacd7a7f2d8f82c2edd334"));
//        System.out.println(validate("86f3059b228c8acf99e69734b6bb32cc","$6$$YzyYPJWjEg4dzGgit1PMqdEkdUrsxJFTplXb7UBDGjgSQuONQ43J.rY97BjmS7a7EJPT8ZNu24zQBrtmJCTSy1"));
    }
}
