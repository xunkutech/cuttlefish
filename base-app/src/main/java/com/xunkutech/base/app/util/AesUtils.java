package com.xunkutech.base.app.util;


import com.xunkutech.base.model.util.CodecUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class AesUtils {

    static final char[] ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZacdefhijkmnpqrstuvwxyz23456789".toCharArray();
    public static final byte[] RANDOM_SEED = randomSeed();
    /**
     * 密钥算法
     */
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String RNG_ALGORITHM = "SHA1PRNG";

    public static byte[] randomSeed() {
        byte[] seed = new byte[6];
        new SecureRandom().nextBytes(seed);
        return seed;
    }

    public static byte[] concatByteArray(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    public static String randomAlphabetCode(int len) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        while (len-- > 0) {
            sb.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }

    public static String randomDigitalCode(int bound) {
        int size = String.valueOf(bound).length() - 1;
        return String.format("%0" + size + "d", new Random().nextInt(bound));
    }

    /**
     * 初始化密钥
     *
     * @param seed
     * @return byte[] 密钥
     */
    private static byte[] initSecretKey(byte[] seed) {
        //返回生成指定算法的秘密密钥的 KeyGenerator 对象
        KeyGenerator kg = null;
        SecureRandom secureRandom = null;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            secureRandom = SecureRandom.getInstance(RNG_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        //初始化此密钥生成器，使其具有确定的密钥大小
        //AES 要求密钥长度为 128
        secureRandom.setSeed(seed);
        kg.init(128, secureRandom);
        //生成一个密钥
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @param seed 密钥
     * @return byte[]   加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] seed) {
        SecretKeySpec key = new SecretKeySpec(initSecretKey(seed), KEY_ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(data);// 加密
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 解密
     *
     * @param data 待解密数据
     * @param seed 二进制密钥
     * @return byte[]   解密数据
     */
    public static byte[] decrypt(byte[] data, byte[] seed) throws InvalidException {
        if (null == data || null == seed)
            throw new InvalidException();

        SecretKeySpec key = new SecretKeySpec(initSecretKey(seed), KEY_ALGORITHM);

        Cipher cipher;

        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }

        try {
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new InvalidException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        String data = "";

        String seed = "123";
        String fakeseed = "";
        System.out.println("加密前数据: string:" + data);
        System.out.println("加密前数据: byte[]:" + CodecUtils.toHex(data.getBytes()));
        System.out.println();
        byte[] encryptData = encrypt(data.getBytes("UTF-8"), seed.getBytes());
        System.out.println("加密后数据: byte[]:" + CodecUtils.toHex(encryptData));
        System.out.println();
        byte[] decryptData = decrypt(encryptData, seed.getBytes());
        System.out.println("解密后数据: byte[]:" + CodecUtils.toHex(decryptData));
        System.out.println("解密后数据: string:" + new String(decryptData));

        decryptData = decrypt(encryptData, fakeseed.getBytes());

    }
}
