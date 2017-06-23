package com.ul.ts.products.mdlreader.data;

import android.util.Log;

import com.ul.ts.products.mdlreader.security.AESUtils;
import com.ul.ts.products.mdlreader.security.ECCUtils;
import com.ul.ts.products.mdlreader.security.EllipticCurveParameters;
import com.ul.ts.products.mdlreader.utils.Bytes;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.BERTags;
import org.bouncycastle.asn1.DERApplicationSpecific;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;


public class PACEAPDUInterface extends SocketAPDUInterface {

    private static final EllipticCurveParameters curveRef = EllipticCurveParameters.brainpoolP320r1;

    private final String CAN;

    private byte[] KEnc;

    private byte[] KMac;

    private byte[] SSC;

    // ready defines if we can send data through this interface. It will only be true if the PACE init() function has been successful.
    private boolean ready = false;
    private boolean failed = false;

    /**
     * @param socket an OPEN socket that we can use the input and output streams from
     */
    public PACEAPDUInterface(Socket socket, String CAN) {
        super(socket);
        this.CAN = CAN;
    }

    @Override
    public byte[] send(byte[] command) {
        if (!ready && !failed) {
            init();
        }

        if (ready) {
            byte[] resp = super.send(secure(command));
            return unsecure(resp);
        }
        throw new IllegalStateException("PACE is not set up correctly");
    }

    private void init() {

        failed = true; // assume any of these steps can fail. We only try to initialise once.

        byte[] response;

        // ----------Step one
        // send cryptographic "mechanism"
        response = super.send(Bytes.bytes("00 22 C1 A4 0F 80 0A 04 00 7F 00 07 02 02 04 02 02 83 01 01"));

        // request nonce;;
        response = super.send(Bytes.bytes("10 86 00 00 02 7C 00 00"));
        byte[] nonce = parseNonce(response);

        // ----------Step two
        KeyPair mappedPair = ECCUtils.generateEphemeralKeys(curveRef);

        // send public key
        response = super.send(generateMapCommand(mappedPair));
        PublicKey cardMappedPublicKey = parseCardMappedPublicKey(response);

        // generate the shared secret from the keys
        byte[] mappedSharedSecret = ECCUtils.calculateECKAShS(mappedPair.getPrivate(), cardMappedPublicKey);

        // map nonce to gCircumflex
        byte[] gCircumflex = ECCUtils.calculateECDHGenericMapping(nonce, mappedSharedSecret, curveRef);

        // ----------Step three
        EllipticCurveParameters newCurveDomainParams = EllipticCurveParameters.specifyCurve(curveRef.getA(), curveRef.getB(), ECCUtils.getXFromUncompressedFormat(gCircumflex), ECCUtils.getYFromUncompressedFormat(gCircumflex), curveRef.getH(), curveRef.getN(), curveRef.getP(), curveRef.getCurveSize());
        KeyPair agreementPair = ECCUtils.generateEphemeralKeys(newCurveDomainParams);

        // send public key
        response = super.send(generateAgreeCommand(agreementPair, newCurveDomainParams));
        PublicKey cardAgreedPublicKey = parseCardAgreedPublicKey(response);

        byte[] terminalPublicKey = ECCUtils.decodeECCPublicKeyX509(agreementPair.getPublic(), newCurveDomainParams);
        byte[] cardPublicKey = ECCUtils.decodeECCPublicKeyX509(cardAgreedPublicKey, newCurveDomainParams);

        // generate the shared secret from the keys
        byte[] agreedSharedSecretX = ECCUtils.getXFromUncompressedFormat(ECCUtils.calculateECKAShS(agreementPair.getPrivate(), cardAgreedPublicKey));

        // Calculate KEnc and KMac from shared X coord secret
        KEnc = ECCUtils.calculateKEnc(agreedSharedSecretX);
        KMac = ECCUtils.calculateKMac(agreedSharedSecretX);

        // ----------Step four
        byte[] TCard = AESUtils.performCBC8(terminalPublicKey, KMac);
        byte[] TTerminal = AESUtils.performCBC8(cardPublicKey, KMac);

        response = super.send(generateAuthCommand(TTerminal));
        byte[] cardAuthenticationToken = parseAuthCommandResponse(response);

        // tokens match?
        if (Arrays.equals(cardAuthenticationToken, TCard)) {
            // start session counter at 0 since we have successfully opened a secure channel
            SSC = Bytes.repeated(8, 0x00);
            ready = true;
        } else {
            throw new IllegalStateException("Could not mutually authenticate with card");
        }
    }

    /**
     * Implementation of Secure Messaging which takes into account optional data for an ISO7816 command
     *
     * @param command the command to secure
     * @return the secured command
     */
    private byte[] secure(byte[] command) {

        //byte cla = command[0];
        byte cla = 0x0C;
        byte ins = command[1];
        byte p1 = command[2];
        byte p2 = command[3];
        byte lc = command[4];
        byte[] data = Bytes.allButLast(Bytes.allButFirst(command, 4), 1);
        byte le = Bytes.tail(command, 1)[0];

        byte[] data87 = new byte[]{};

        if (data.length > 0) { // if there is data then build the 87 block
            byte[] paddedData = AESUtils.addAESPadding(data);
            byte[] encryptedData = AESUtils.encryptAESCBC(paddedData, KEnc);
            byte[] length87 = Bytes.bytes(encryptedData.length+1);
            data87 = Bytes.concatenate(Bytes.bytes("87"), length87, Bytes.bytes("01"), encryptedData);
        }

        byte[] data97 = Bytes.concatenate(Bytes.bytes("97 01"), Bytes.bytes(le));

        byte[] paddedCommandHeader = new byte[] {cla, ins, p1, p2, (byte)0x80, 0x00, 0x00, 0x00};

        int ssc = Bytes.bytesToInt(SSC)+1;
        SSC = Bytes.bytes(ssc);
        byte[] newSSC = new byte[8];
        System.arraycopy(SSC, 0, newSSC, newSSC.length - SSC.length, SSC.length);
        SSC = newSSC;

        byte[] mac = AESUtils.performCBC8(Bytes.concatenate(SSC, paddedCommandHeader, data87, data97), KMac);

        byte[] data8E = Bytes.concatenate(Bytes.bytes("8E 08"), mac);

        byte[] partialCommand = Bytes.concatenate(data87, data97, data8E);

        byte[] fullCommand = Bytes.concatenate(new byte[] {cla, ins, p1, p2}, Bytes.bytes(partialCommand.length), partialCommand, Bytes.bytes(le));

        return fullCommand;
    }

    /**
     * Implementation of Secure Messaging
     *
     * @param response the response to unsecure
     * @return the unsecured response
     */
    private byte[] unsecure(byte[] response) {

        byte[] statusword = Bytes.allButFirst(response, response.length-2);

        byte[] protectedResponseData = Bytes.allButLast(response, 12);
        byte[] CC = Bytes.allButLast(Bytes.allButFirst(response, response.length-10), 2);

        int ssc = Bytes.bytesToInt(SSC)+1;
        SSC = Bytes.bytes(ssc);
        byte[] newSSC = new byte[8];
        System.arraycopy(SSC, 0, newSSC, newSSC.length - SSC.length, SSC.length);
        SSC = newSSC;

        byte[] mac = AESUtils.performCBC8(Bytes.concatenate(SSC, protectedResponseData), KMac);

        if (!Arrays.equals(mac, CC)) {
            throw new IllegalStateException("Could not verify integrity of response packet");
        }

        // strip off trailing 99
        byte[] encryptedResponseData = Bytes.allButLast(protectedResponseData, 4);

        byte[] data = new byte[]{};
        if (encryptedResponseData.length > 0) {
            // there's data to decrypt!
            byte[] encryptedData = Bytes.allButFirst(encryptedResponseData, 3);
            data = AESUtils.decryptAESCBC(encryptedData, KEnc);
            data = AESUtils.removeAESPadding(data);
        }

        byte[] constructedResponse = Bytes.concatenate(data, statusword);

        return constructedResponse;
    }

    /**
     * Checks whether the response is a good status word. This is considered to be 0x9000
     * @param response the response to be checked.
     * @return {@code true} if this response is a good response.
     */
    private boolean isGoodResponse(byte[] response) {
        byte[] sw = Bytes.tail(response, 2);
        return sw[0] == (byte)0x90;
    }

    private byte[] generateMapCommand(KeyPair mappedPair) {
        byte[] pub = ECCUtils.decodeECCPublicKeyX509(mappedPair.getPublic(), curveRef);
        byte[] x = ECCUtils.getXFromUncompressedFormat(pub);
        byte[] y = ECCUtils.getYFromUncompressedFormat(pub);

        byte[] command = Bytes.bytes("10 86 00 00");
        byte[] dynamicAuthData = Bytes.bytes("7C");
        byte[] mappingData = Bytes.bytes("81");
        byte[] uncompressedPoint = Bytes.bytes("04");
        byte[] length81 = Bytes.bytes(uncompressedPoint.length + x.length + y.length);
        byte[] length7C = Bytes.bytes(Bytes.bytesToInt(length81)+2);
        byte[] le = Bytes.bytes("00");
        byte[] data = Bytes.concatenate(dynamicAuthData, length7C, mappingData, length81, uncompressedPoint, x, y);
        byte[] lc = Bytes.bytes(data.length);

        byte[] completeCommand = Bytes.concatenate(
                command,
                lc,
                data,
                le
        );

        return completeCommand;
    }

    private byte[] generateAgreeCommand(KeyPair mappedPair, EllipticCurveParameters curve) {
        byte[] pub = ECCUtils.decodeECCPublicKeyX509(mappedPair.getPublic(), curve);
        byte[] x = ECCUtils.getXFromUncompressedFormat(pub);
        byte[] y = ECCUtils.getYFromUncompressedFormat(pub);

        byte[] command = Bytes.bytes("10 86 00 00");
        byte[] dynamicAuthData = Bytes.bytes("7C");
        byte[] mappingData = Bytes.bytes("83");
        byte[] uncompressedPoint = Bytes.bytes("04");
        byte[] length81 = Bytes.bytes(uncompressedPoint.length + x.length + y.length);
        byte[] length7C = Bytes.bytes(Bytes.bytesToInt(length81)+2);
        byte[] le = Bytes.bytes("00");
        byte[] data = Bytes.concatenate(dynamicAuthData, length7C, mappingData, length81, uncompressedPoint, x, y);
        byte[] lc = Bytes.bytes(data.length);

        byte[] completeCommand = Bytes.concatenate(
                command,
                lc,
                data,
                le
        );

        return completeCommand;
    }

    private byte[] generateAuthCommand(byte[] T) {
        byte[] command = Bytes.bytes("00 86 00 00");
        byte[] dynamicAuthData = Bytes.bytes("7C");
        byte[] authenticationData = Bytes.bytes("85");
        byte[] length85 = Bytes.bytes(T.length);
        byte[] length7C = Bytes.bytes(Bytes.bytesToInt(length85)+2);
        byte[] le = Bytes.bytes("00");
        byte[] data = Bytes.concatenate(dynamicAuthData, length7C, authenticationData, length85, T);
        byte[] lc = Bytes.bytes(data.length);

        byte[] completeCommand = Bytes.concatenate(
                command,
                lc,
                data,
                le
        );

        return completeCommand;
    }

    private byte[] parseNonce(byte[] data) {
        try (ASN1InputStream bIn = new ASN1InputStream(data)) {
            DERApplicationSpecific app = (DERApplicationSpecific) bIn.readObject();

            ASN1Sequence seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);

            byte[] tag80 = ((ASN1Primitive) seq.getObjects().nextElement()).getEncoded();

            if (tag80[0] == (byte) 0x80) {

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] kpi =  md.digest(Bytes.concatenate(CAN.getBytes(), Bytes.bytes("00 00 00 03")));

                return AESUtils.decryptAESCBC(Bytes.allButFirst(tag80, 2), kpi);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e(getClass().getName(), "Failed to parse nonce from response data", e);
        }

        return null;
    }

    private PublicKey parseCardMappedPublicKey(byte[] data) {

        try (ASN1InputStream innerIn = new ASN1InputStream(Bytes.allButFirst(data, 2))) {
            ASN1Primitive innerObj = innerIn.readObject();
            byte[] innerTLV = innerObj.getEncoded();

            // tag 82 = Mapping Data
            if (innerTLV[0] == (byte) 0x82) {
                // Get the card's Public key
                return ECCUtils.encodeECCPublicKeyX509(Bytes.allButFirst(innerTLV, 2), curveRef);
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), "Failed to parse card public key", e);
        }

        return null;
    }

    private PublicKey parseCardAgreedPublicKey(byte[] data) {

        try (ASN1InputStream innerIn = new ASN1InputStream(Bytes.allButFirst(data, 2))) {
            ASN1Primitive innerObj = innerIn.readObject();
            byte[] innerTLV = innerObj.getEncoded();

            // tag 82 = Mapping Data
            if (innerTLV[0] == (byte) 0x84) {
                // Get the card's Public key
                return ECCUtils.encodeECCPublicKeyX509(Bytes.allButFirst(innerTLV, 2), curveRef);
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), "Failed to parse card public key", e);
        }

        return null;
    }

    private byte[] parseAuthCommandResponse(byte[] data) {

        try (ASN1InputStream innerIn = new ASN1InputStream(Bytes.allButFirst(data, 2))) {
            ASN1Primitive innerObj = innerIn.readObject();
            byte[] innerTLV = innerObj.getEncoded();

            // tag 86 = Mapping Data
            if (innerTLV[0] == (byte) 0x86) {
                // Get the card's Public key
                return Bytes.allButFirst(innerTLV, 2);
            }
        } catch (IOException e) {
            Log.e(getClass().getName(), "Failed to parse card token", e);
        }

        return null;
    }
}
