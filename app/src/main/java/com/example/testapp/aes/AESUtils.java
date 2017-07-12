package com.example.testapp.aes;

import android.util.Base64;
import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by 93367 on 2017/5/18.
 */

public final class AESUtils {
    private static SecretKeySpec aesKey = null;

    public static byte[] getAESKey()
    {
        if (null == aesKey)
        {
            try {
                testAESGenerate();
            }
            catch (Exception e)
            {
                return null;
            }
        }
        return aesKey.getEncoded();
    }

    public static void generateAESByMsg(byte[] encodeKey) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        aesKey = new SecretKeySpec(encodeKey, "AES");
        return;
    }

    public static SecretKey createAESKey(byte[] aSecret) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        SecretKeySpec secret = new SecretKeySpec(aSecret, "AES");
        return secret;
    }

    public static void testAESGenerate() throws InvalidKeySpecException,NoSuchAlgorithmException
    {
        byte[] key = new byte[16];
        byte[] salt = new byte[16];

        SecureRandom localSecureRandom = SecureRandom.getInstance("SHA1PRNG");
        localSecureRandom.nextBytes(salt);
        localSecureRandom.nextBytes(key);
        //byte[] salt = {1,2,3,4,5,6,7};

        char[] pwd = key.toString().toCharArray();

        GenerateAESKey(pwd, salt);
    }

    public static void GenerateAESKey(char[] key, byte[] salt) throws InvalidKeySpecException,NoSuchAlgorithmException
    {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        KeySpec spec = new PBEKeySpec(key, salt, 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        aesKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        return;
    }

    public static String AESencryptString(String clearStr) throws Exception {
        String cipherStr = null;

        //encrypt
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        byte[] cipherByteArray = cipher.doFinal(clearStr.getBytes());

        //convert to base64
        cipherStr = Base64.encodeToString(cipherByteArray, Base64.DEFAULT);

        return cipherStr;
    }

    public static String AESdecryptString(String cipherStr) throws Exception {
        String clearStr = null;

        //decrypt
        Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(new byte[decipher.getBlockSize()]));
        byte[] content = Base64.decode(cipherStr, Base64.DEFAULT);
        byte[] clearByteArray = decipher.doFinal(content);

        return new String(clearByteArray);
    }

    public static byte[] encryptVoice(byte[] clearbyte)
            throws Exception {
        byte[] result = AESEncryptByte(clearbyte);
        return result;
    }

    public static byte[] decryptVoice(byte[] encrypted)
            throws Exception {
        byte[] result = AESDecryptByte(encrypted);
        return result;
    }


    private static byte[] AESEncryptByte(byte[] clear) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] AESDecryptByte(byte[] encrypted)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
}
