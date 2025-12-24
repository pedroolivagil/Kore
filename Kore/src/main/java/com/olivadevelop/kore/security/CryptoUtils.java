package com.olivadevelop.kore.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.olivadevelop.kore.Constants;

import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public interface CryptoUtils {
    static SecretKey getOrCreateDeviceKey(String keyAlias) throws Exception {
        KeyStore ks = KeyStore.getInstance(Constants.Security.ANDROID_KEYSTORE);
        ks.load(null);
        if (!ks.containsAlias(keyAlias)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, Constants.Security.ANDROID_KEYSTORE);
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();
            keyGenerator.init(spec);
            keyGenerator.generateKey();
        }
        return ((SecretKey) ks.getKey(keyAlias, null));
    }
    static SecretKey deriveKeyFromPassword(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance(Constants.Security.ALGORITHM);
        KeySpec spec = new PBEKeySpec(password, salt, 200_000, 256); // iterations high for security
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, Constants.Security.ALGORITHM_TYPE);
    }
    static byte[] encryptAesGcm(byte[] plaintext, SecretKey key, byte[] outIv) throws Exception {
        Cipher cipher = Cipher.getInstance(Constants.Security.AES_GCM_NO_PADDING);
        byte[] iv = new byte[Constants.Security.GCM_IV_LENGTH];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(Constants.Security.GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] cipherText = cipher.doFinal(plaintext);
        // return iv + cipherText
        ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
        bb.put(iv);
        bb.put(cipherText);
        if (outIv != null && outIv.length >= iv.length) { System.arraycopy(iv, 0, outIv, 0, iv.length); }
        return bb.array();
    }
    static byte[] decryptAesGcm(byte[] ivAndCiphertext, SecretKey key) throws Exception {
        if (ivAndCiphertext.length < Constants.Security.GCM_IV_LENGTH) { throw new IllegalArgumentException("Invalid data"); }
        byte[] iv = Arrays.copyOfRange(ivAndCiphertext, 0, Constants.Security.GCM_IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(ivAndCiphertext, Constants.Security.GCM_IV_LENGTH, ivAndCiphertext.length);
        Cipher cipher = Cipher.getInstance(Constants.Security.AES_GCM_NO_PADDING);
        GCMParameterSpec spec = new GCMParameterSpec(Constants.Security.GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(ciphertext);
    }
}