package com.ul.ts.products.mdlholder.security;

import com.ul.ts.products.mdlholder.utils.Bytes;

import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;

public class ECCUtils {

    /**
     * Encode the provide ECC key in X509 format.
     * @param publicKeyRaw the bytes of the public key (uncompressed format).
     * @param curveReference the reference to the standard curve of the key.
     * @return the encoded key.
     */
    public static PublicKey encodeECCPublicKeyX509(byte[] publicKeyRaw, EllipticCurveParameters curveReference)
    {
        try
        {
            final ECParameterSpec curveParams = EllipticCurveParameters.encodeECParameterSpec(curveReference);
            final BigInteger wx = new BigInteger(1, getXFromUncompressedFormat(publicKeyRaw));
            final BigInteger wy = new BigInteger(1, getYFromUncompressedFormat(publicKeyRaw));
            final ECPoint w = new ECPoint(wx, wy);
            final ECPublicKeySpec keySpec = new ECPublicKeySpec(w, curveParams);

            // Try bouncy castle first then fall back to the built in provider.
            try
            {
                final KeyFactory kf = KeyFactory.getInstance("EC", "BC");

                return kf.generatePublic(keySpec);
            }
            catch (NoSuchProviderException e)
            {
                final KeyFactory kf = KeyFactory.getInstance("EC");

                return kf.generatePublic(keySpec);
            }
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            throw new IllegalStateException("Error encoding ECC public key", e);
        }
    }

    /**
     * Encode the provide ECC key in PKCS#8 format.
     * @param privateKeyRaw the bytes of the private key.
     * @param curveReference the reference to the standard curve of the key.
     * @return the encoded key.
     */
    public static PrivateKey encodeECCPrivateKeyPKCS8(byte[] privateKeyRaw, EllipticCurveParameters curveReference)
    {
        try
        {
            final ECParameterSpec curveParams = EllipticCurveParameters.encodeECParameterSpec(curveReference);
            final BigInteger s = new BigInteger(1, privateKeyRaw);
            final ECPrivateKeySpec keySpec = new ECPrivateKeySpec(s, curveParams);

            // Try bouncy castle first then fall back to the built in provider.
            try
            {
                final KeyFactory kf = KeyFactory.getInstance("EC", "BC");

                return kf.generatePrivate(keySpec);
            }
            catch (NoSuchProviderException e)
            {
                final KeyFactory kf = KeyFactory.getInstance("EC");

                return kf.generatePrivate(keySpec);
            }
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            throw new IllegalStateException("Error encoding ECC private key", e);
        }
    }

    public static byte[] getXFromUncompressedFormat(byte[] publicKeyRaw)
    {
        if (publicKeyRaw[0] != 0x04)
        {
            throw new IllegalStateException("EC component not in uncompressed format");
        }

        final int intLength = (publicKeyRaw.length - 1) / 2;
        return Bytes.sub(publicKeyRaw, 1, intLength);
    }

    public static byte[] getYFromUncompressedFormat(byte[] publicKeyRaw)
    {
        if (publicKeyRaw[0] != 0x04)
        {
            throw new IllegalStateException("EC component not in uncompressed format");
        }

        final int intLength = (publicKeyRaw.length - 1) / 2;
        return Bytes.tail(publicKeyRaw, intLength);
    }

    /**
     * Extract the raw bytes of the public ECC key in standard smart card format.
     * @param publicKey the key to extract the bytes of.
     * @param curveReference the reference to the standard curve of the key.
     * @return the extract bytes of the key.
     */
    public static byte[] decodeECCPublicKeyX509(PublicKey publicKey, EllipticCurveParameters curveReference)
    {
        byte[] publicKeyBytes = {};

        if (publicKey instanceof ECPublicKey)
        {
            final ECPoint w = ((ECPublicKey)publicKey).getW();

            final byte[] x = getStandardSizeInteger(w.getAffineX().toByteArray(), curveReference);
            final byte[] y = getStandardSizeInteger(w.getAffineY().toByteArray(), curveReference);

            publicKeyBytes = Bytes.concatenate(Bytes.bytes("04"), x, y); // Uncompressed format.
        }

        return publicKeyBytes;
    }

    /**
     * Extract the raw bytes of the private ECC key in standard smart card format.
     * @param privateKey the key to extract the bytes of.
     * @param curveReference the reference to the standard curve of the key.
     * @return the extract bytes of the key.
     */
    public static byte[] decodeECCPrivateKeyPKCS8(PrivateKey privateKey, EllipticCurveParameters curveReference)
    {
        byte[] privateKeyBytes = {};

        if (privateKey instanceof ECPrivateKey)
        {
            final byte[] s = getStandardSizeInteger(((ECPrivateKey)privateKey).getS().toByteArray(), curveReference);
            privateKeyBytes = s;
        }

        return privateKeyBytes;
    }

    private static byte[] getStandardSizeInteger(byte[] i, EllipticCurveParameters curveReference)
    {
        // Strip any leading zero as fixed length encoding assumes positive integer.
        if (Bytes.isEqual(Bytes.head(i, 1), Bytes.bytes("00")))
        {
            i = Bytes.allButFirst(i, 1);
        }

        final int curveSize = curveReference.getCurveSize();

        int intSize;
        switch (curveSize)
        {
            case 128 :
                intSize = 16;
                break;
            case 192 :
                intSize = 24;
                break;
            case 256 :
                intSize = 32;
                break;
            case 320 :
                intSize = 40;
                break;
            case 512 :
                intSize = 64;
                break;

            default :
                throw new IllegalStateException("Unsupported ECC Curve size " + curveSize);
        }

        // Pad out the integer to meet the fixed length required by the format.
        while (i.length < intSize)
        {
            i = Bytes.concatenate(Bytes.bytes("00"), i);
        }

        return i;
    }

    /**
     * Implement the ECKA algorithm (EG variant) in order to calculate the ShS (shared secret) from the provided
     * static and ephemeral keys from the same curve.
     * @param privateKey the private key to use for the ShS calculation.
     * @param publicKey the public key to use for the ShS calculation.
     * @return the calculated shared secret.
     */
    public static byte[] calculateECKAShS(PrivateKey privateKey, PublicKey publicKey) {

        try {
            ECKABasicAgreement basicAgreement = new ECKABasicAgreement();
            basicAgreement.init(ECUtil.generatePrivateKeyParameter(privateKey));

            return basicAgreement.calculatePoint(ECUtil.generatePublicKeyParameter(publicKey)).getEncoded();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Performs a G-Circumflex mapping of a nonce and a shared secret to a curve reference
     *
     * @param nonce the random nonce to use
     * @param sharedSecret the shared secret point
     * @param curveReference the curve to map against
     * @return the mapped point on the curve
     */
    public static byte[] calculateECDHGenericMapping(byte[] nonce, byte[] sharedSecret, EllipticCurveParameters curveReference) {

        final ECParameterSpec curveParams = EllipticCurveParameters.encodeECParameterSpec(curveReference);

        org.bouncycastle.math.ec.ECCurve curve = EC5Util.convertCurve(curveParams.getCurve());

        org.bouncycastle.math.ec.ECPoint generator = EC5Util.convertPoint(curveParams, curveParams.getGenerator(), false);
        org.bouncycastle.math.ec.ECPoint secretPoint = curve.decodePoint(sharedSecret);

        //interpret nonce as positive number
        BigInteger nonceInteger = new BigInteger(1, nonce);

        org.bouncycastle.math.ec.ECPoint G = generator.multiply(nonceInteger).add(secretPoint);

        // first byte 04 that represents 'uncompressed format'?
        return G.getEncoded();

        //return new ECPoint(generator.getAffineXCoord().toBigInteger(), generator.getAffineYCoord().toBigInteger());
    }

    public static byte[] calculateKEnc(byte[] secretX) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            return  md.digest(Bytes.concatenate(secretX, Bytes.bytes("00 00 00 01")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] calculateKMac(byte[] secretX) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            return  md.digest(Bytes.concatenate(secretX, Bytes.bytes("00 00 00 02")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Generates a random key pair based on a curve
     * @param curve the curve to use
     * @return a randomly generated KeyPair
     */
    public static KeyPair generateEphemeralKeys(EllipticCurveParameters curve) {
        try {
            final ECParameterSpec curveParams = EllipticCurveParameters.encodeECParameterSpec(curve);
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGenerator.initialize(curveParams);

            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }
}
