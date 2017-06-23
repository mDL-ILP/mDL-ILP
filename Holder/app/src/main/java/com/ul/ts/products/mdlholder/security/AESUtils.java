package com.ul.ts.products.mdlholder.security;

import com.ul.ts.products.mdlholder.utils.Bytes;
import com.ul.ts.products.mdlholder.utils.Preconditions;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    /**
     * AES [FIPS 197] SHALL be used in CMAC-mode [SP 800-38B] with a MAC length of 8 bytes.
     *
     * @param data the data to MAC
     * @param key the key to use
     * @return the 8 byte MAC of the data
     */
    public static byte[] performCBC8(byte[] data, byte[] key) {

        // mac size in bits (64 bits = 8 bytes)
        final Mac cbc8 = new CMac(new AESEngine(), 64);
        CipherParameters params = new KeyParameter(key);
        cbc8.init(params);

        byte[] result = new byte[8];
        cbc8.update(data, 0, data.length);
        cbc8.doFinal(result, 0);

        return result;
    }

    /**
     * Add AES padding to the provided data. Padding scheme appends '80' byte to data and then zero or more '00' bytes
     * until data length is modulo 16 (defined in GlobalPlatform Card Specification Amendment D section 4.1.4).
     * @param data the data to be padded
     * @return the padded data
     * @throws NullPointerException if the input parameter is null
     */
    public static byte[] addAESPadding(byte[] data)
    {
        Preconditions.checkForNull("Data", data);

        // Include the mandatory appended 80.
        int padLength = data.length + 1;

        if (padLength % 16 != 0)
        {
            // If the extra 80 doesn't make the data modulo 16, work out how many 00s to add.
            padLength += (16 - (padLength % 16));
        }

        byte[] paddedData = new byte[padLength];
        System.arraycopy(data, 0, paddedData, 0, data.length);
        paddedData[data.length] = (byte)0x80;

        return paddedData;
    }

    /**
     * Remove AES padding from the provided data.
     * @param data the data to remove the padded from
     * @return the unpadded data
     * @throws IllegalStateException if data is not padded with correct AES padding
     * @throws NullPointerException if the input parameter is null
     * @throws IllegalArgumentException if the input parameter is invalid
     */
    public static byte[] removeAESPadding(byte[] data)
    {
        Preconditions.checkForNull("Data", data);
        Preconditions.check(data.length >= 16, "Data must be at least 16 bytes");

        int dataLength = data.length;

        if (data[data.length - 1] == (byte)0x80)
        {
            // Only one byte of padding.
            --dataLength;
        }
        else
        {
            // Reverse search through the data looking for the padding 80. Should be 00 until we reach the 80.
            for (int i = data.length - 1; i >= 0; --i)
            {
                if (data[i] == (byte)0x00)
                {
                    --dataLength;
                }
                else if (data[i] == (byte)0x80)
                {
                    --dataLength;
                    break;
                }
                else
                {
                    // Invalid DES padding found
                    throw new IllegalStateException("Error removing AES padding");
                }
            }
        }

        return Arrays.copyOf(data, dataLength);
    }

    /**
     * Encrypt the provided data using AES in CBC mode and the specified key and initial vector. The data will
     * not be padded before encryption.
     * @param data the data to be encrypted
     * @param keyBytes the bytes of the AES key to use for the encryption
     * @return the encrypted data
     * @throws IllegalStateException if there was a problem during encryption
     * @throws NullPointerException if any of the input parameters are null
     * @throws IllegalArgumentException if any of the input parameters are invalid
     */
    public static byte[] encryptAESCBC(byte[] data, byte[] keyBytes)
    {
        Preconditions.checkForNull("Data", data);
        Preconditions.check(data.length >= 16, "Data must be at least 16 bytes");
        Preconditions.check((data.length % 16) == 0, "Data length not multiple of 16");
        Preconditions.checkForNull("Key bytes", keyBytes);
        Preconditions.check(checkAESKeyLength(keyBytes), "Key must be 16, 24 or 32 bytes");

        try
        {
            final SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            final IvParameterSpec iv = new IvParameterSpec(Bytes.repeated(16, 0x00));
            final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            return cipher.doFinal(data);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Error encrypting data", e);
        }
    }

    /**
     * Decrypt the provided data using AES in CBC mode with the specified key and initial vector. No padding will be
     * removed from the decrypted data.
     * @param data the data to be decrypted
     * @param keyBytes the bytes of the AES key to use for the decryption
     * @return the decrypted data
     * @throws IllegalStateException if there was a problem during decryption
     * @throws NullPointerException if any of the input parameters are null
     * @throws IllegalArgumentException if any of the input parameters are invalid
     */
    public static byte[] decryptAESCBC(byte[] data, byte[] keyBytes)
    {
        Preconditions.checkForNull("Data", data);
        Preconditions.check(data.length >= 16, "Data must be at least 16 bytes");
        Preconditions.check((data.length % 16) == 0, "Data length not multiple of 16");
        Preconditions.checkForNull("Key bytes", keyBytes);
        Preconditions.check(checkAESKeyLength(keyBytes), "Key must be 16, 24 or 32 bytes");

        try
        {
            final SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            final IvParameterSpec iv = new IvParameterSpec(Bytes.repeated(16, 0x00));
            final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            return cipher.doFinal(data);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Error decrypting data", e);
        }
    }

    private static boolean checkAESKeyLength(byte[] key)
    {
        return key.length == 16 || key.length == 24 || key.length == 32;
    }

}
