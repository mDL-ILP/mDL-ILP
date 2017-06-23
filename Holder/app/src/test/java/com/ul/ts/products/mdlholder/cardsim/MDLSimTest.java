package com.ul.ts.products.mdlholder.cardsim;

import com.ul.ts.products.mdlholder.security.AESUtils;
import com.ul.ts.products.mdlholder.security.ECCUtils;
import com.ul.ts.products.mdlholder.security.EllipticCurveParameters;
import com.ul.ts.products.mdlholder.utils.Bytes;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.BERTags;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.junit.Test;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;

import static com.ul.ts.products.mdlholder.security.EllipticCurveParameters.brainpoolP320r1;
import static org.junit.Assert.assertTrue;

public class MDLSimTest {

    private final MDLSim mdl = new MDLSim(null, true, "doesn't matter");

    private byte[] unencryptedNonce;
    private byte[] terminalPublicKey;
    private byte[] cardPublicKey;
    private byte[] gCircumflex;
    private byte[] KEnc;
    private byte[] KMac;
    private byte[] SSC = Bytes.repeated(8, 0x00);

    private static final EllipticCurveParameters curveRef = brainpoolP320r1;

    @Test
    public void sendReadBinaryTest() throws Exception {
        byte[] response = mdl.send(Bytes.bytes("00 B0 9D 00 00"));
        System.out.println("Response: "+Bytes.hexString(response));
    }

    @Test
    public void sendManageSecurityTest() throws Exception {
        // "0.4.0.127.0.7.2.2.4.2.4" = "id-PACE-ECDH-GM-AES-CBC-CMAC-256"
        byte[] response = mdl.send(Bytes.bytes("00 22 C1 A4 0F 80 0A 04 00 7F 00 07 02 02 04 02 04 83 01 01"));
        System.out.println("Response: "+Bytes.hexString(response));
    }

    @Test
    public void sendGeneralAuthenticateTest() throws Exception {
        byte[] response = mdl.send(Bytes.bytes("00 86 00 00 02 7C 00 00"));
        System.out.println("Response: "+Bytes.hexString(response));
    }

    @Test
    public void testNonce() throws Exception {
        sendManageSecurityTest();

        byte[] response = mdl.send(Bytes.bytes("10 86 00 00 02 7C 00 00"));
        System.out.println("Response: "+Bytes.hexString(response));

        try (ASN1InputStream bIn = new ASN1InputStream(response)) {
            DERApplicationSpecific app = (DERApplicationSpecific) bIn.readObject();

//            System.out.println(app);

            ASN1Sequence seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);

//            System.out.println(seq);

            ASN1Primitive tag80 = ((ASN1Primitive) seq.getObjects().nextElement());
            byte[] data = tag80.getEncoded();

//            System.out.println("Data = " + Bytes.hexString(data));

            if (data[0] == (byte) 0x80) {

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] kpi =  md.digest(Bytes.concatenate("123456".getBytes(), Bytes.bytes("00 00 00 03")));

//                System.out.println("KPI "+Bytes.hexString(kpi));
//                System.out.println("KPI length "+kpi.length);

                unencryptedNonce = AESUtils.decryptAESCBC(Bytes.allButFirst(data, 2), kpi);

//                System.out.println("unencryptedNonce "+Bytes.hexString(unencryptedNonce));
//                System.out.println("unencryptedNonce length "+unencryptedNonce.length);
            }

        }
    }

    @Test
    public void testMapping() throws Exception {
        sendManageSecurityTest();
        testNonce();

        KeyPair pair = ECCUtils.generateEphemeralKeys(curveRef);

        byte[] pub = ECCUtils.decodeECCPublicKeyX509(pair.getPublic(), curveRef);
        byte[] x = ECCUtils.getXFromUncompressedFormat(pub);
        byte[] y = ECCUtils.getYFromUncompressedFormat(pub);

        // Send public key X and Y to the card so it can send back its public key

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

        byte[] response = mdl.send(completeCommand);
        System.out.println("Response: "+Bytes.hexString(response));

        PublicKey cardKey = null;
        try (ASN1InputStream innerIn = new ASN1InputStream(Bytes.allButFirst(response, 2))) {
            ASN1Primitive innerObj = innerIn.readObject();
            byte[] innerTLV = innerObj.getEncoded();

            // tag 82 = Mapping Data
            if (innerTLV[0] == (byte) 0x82) {
                // Get the terminals Public key
                cardKey = ECCUtils.encodeECCPublicKeyX509(Bytes.allButFirst(innerTLV, 2), curveRef);
            }
        }

        // shared secret
        byte[] ephemeralSharedSecret = ECCUtils.calculateECKAShS(pair.getPrivate(), cardKey);
//        System.out.println("Terminal Shared Secret: "+Bytes.hexString(sharedSecret));

        // NOTE(JS): Both Card and Terminal have their Ephemeral Keys, Nonce and Shared Secret at this point

        // gCircumflex first byte is 04 that represents 'uncompressed format'
        gCircumflex = ECCUtils.calculateECDHGenericMapping(unencryptedNonce, ephemeralSharedSecret, curveRef);
//        System.out.println("Terminal gCircumflex: "+Bytes.hexString(gCircumflex));
//        System.out.println("gCircumflex length: "+gCircumflex.length);

    }

    @Test
    public void testKeyAgreement() throws Exception {

        sendManageSecurityTest();
        testNonce();
        testMapping();

        EllipticCurveParameters newCurveDomainParams = EllipticCurveParameters.specifyCurve(curveRef.getA(), curveRef.getB(), ECCUtils.getXFromUncompressedFormat(gCircumflex), ECCUtils.getYFromUncompressedFormat(gCircumflex), curveRef.getH(), curveRef.getN(), curveRef.getP(), curveRef.getCurveSize());
        KeyPair newPair = ECCUtils.generateEphemeralKeys(newCurveDomainParams);

        byte[] pub = ECCUtils.decodeECCPublicKeyX509(newPair.getPublic(), newCurveDomainParams);
        byte[] x = ECCUtils.getXFromUncompressedFormat(pub);
        byte[] y = ECCUtils.getYFromUncompressedFormat(pub);

        terminalPublicKey = pub;

//        System.out.println("Terminal NewPublicKey "+Bytes.hexString(newPair.getPublic().getEncoded()));

        byte[] command = Bytes.bytes("10 86 00 00");
        byte[] dynamicAuthData = Bytes.bytes("7C");
        byte[] terminalEphemeralPublicKeyTag = Bytes.bytes("83");
        byte[] uncompressedPoint = Bytes.bytes("04");
        byte[] length83 = Bytes.bytes(uncompressedPoint.length + x.length + y.length);
        byte[] length7C = Bytes.bytes(Bytes.bytesToInt(length83)+2);
        byte[] le = Bytes.bytes("00");
        byte[] data = Bytes.concatenate(dynamicAuthData, length7C, terminalEphemeralPublicKeyTag, length83, uncompressedPoint, x, y);
        byte[] lc = Bytes.bytes(data.length);

        byte[] completeCommand = Bytes.concatenate(
                command,
                lc,
                data,
                le
        );

        byte[] response = mdl.send(completeCommand);
        System.out.println("Response: "+Bytes.hexString(response));

        PublicKey cardKey = null;
        try (ASN1InputStream innerIn = new ASN1InputStream(Bytes.allButFirst(response, 2))) {
            ASN1Primitive innerObj = innerIn.readObject();
            byte[] innerTLV = innerObj.getEncoded();

            // tag 84 = Key Agreement Data
            if (innerTLV[0] == (byte) 0x84) {
                // Get the Card's Public key
                cardKey = ECCUtils.encodeECCPublicKeyX509(Bytes.allButFirst(innerTLV, 2), newCurveDomainParams);
            }
        }

        cardPublicKey = ECCUtils.decodeECCPublicKeyX509(cardKey, newCurveDomainParams);

//        System.out.println("Terminal cardPublicKey "+Bytes.hexString(cardKey.getEncoded()));

        byte[] sharedSecretX = ECCUtils.getXFromUncompressedFormat(ECCUtils.calculateECKAShS(newPair.getPrivate(), cardKey));

        KEnc = ECCUtils.calculateKEnc(sharedSecretX);
        KMac = ECCUtils.calculateKMac(sharedSecretX);

//        System.out.println("KEnc: "+Bytes.hexString(KEnc));
//        System.out.println("KMac: "+Bytes.hexString(KMac));
//
//        System.out.println("KEnc length: "+KEnc.length);
//        System.out.println("KMac length: "+KMac.length);
    }

    @Test
    public void testMutualAuth() throws Exception {
        sendManageSecurityTest();
        testNonce();
        testMapping();
        testKeyAgreement();

        //TTerminal = MAC(KSMAC,PKDHCard)
        //TCard = MAC(KSMAC,PKDHTerminal)

        // AES [FIPS 197] SHALL be used in CMAC-mode [SP 800-38B] with a MAC length of 8 bytes.

        byte[] TCard = AESUtils.performCBC8(terminalPublicKey, KMac);

//        System.out.println("Card Mac: "+Bytes.hexString(TCard));
//        System.out.println("Card Mac length: "+TCard.length);

        byte[] TTerminal = AESUtils.performCBC8(cardPublicKey, KMac);

//        System.out.println("Terminal Mac: "+Bytes.hexString(TTerminal));
//        System.out.println("Terminal Mac length: "+TTerminal.length);

        byte[] command = Bytes.bytes("00 86 00 00");
        byte[] dynamicAuthData = Bytes.bytes("7C");
        byte[] authenticationData = Bytes.bytes("85");
        byte[] length85 = Bytes.bytes(TTerminal.length);
        byte[] length7C = Bytes.bytes(Bytes.bytesToInt(length85)+2);
        byte[] le = Bytes.bytes("00");
        byte[] data = Bytes.concatenate(dynamicAuthData, length7C, authenticationData, length85, TTerminal);
        byte[] lc = Bytes.bytes(data.length);

        byte[] completeCommand = Bytes.concatenate(
                command,
                lc,
                data,
                le
        );

        byte[] response = mdl.send(completeCommand);
        System.out.println("Response: "+Bytes.hexString(response));

        byte[] cardToken = new byte[0];
        try (ASN1InputStream innerIn = new ASN1InputStream(Bytes.allButFirst(response, 2))) {
            ASN1Primitive innerObj = innerIn.readObject();
            byte[] innerTLV = innerObj.getEncoded();

            // tag 86 = Card Authentication token
            if (innerTLV[0] == (byte) 0x86) {
                // Get the Card's MAC
                cardToken = Bytes.allButFirst(innerTLV, 2);
            }
        }

        assertTrue("Terminal Authentication complete", Arrays.equals(cardToken, TCard));
        // start session counter at 0 since we have successfully opened a secure channel
        SSC = Bytes.repeated(8, 0x00);
    }

    @Test
    public void testSecureAPDU() throws Exception {
        sendManageSecurityTest();
        testNonce();
        testMapping();
        testKeyAgreement();
        testMutualAuth();

        byte[] encryptedResponse = mdl.send(secure(Bytes.bytes("00 B0 9D 00 00")));
        System.out.println("Response: "+Bytes.hexString(encryptedResponse));

        byte[] decryptedResponse = unsecure(encryptedResponse);
        System.out.println("decryptedResponse: "+Bytes.hexString(decryptedResponse));

        byte[] response = mdl.send(Bytes.bytes("00 B0 9D 00 00"));

        assertTrue("Decryption successful", Arrays.equals(decryptedResponse, response));
    }


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

    private byte[] unsecure(byte[] response) {

        System.out.println("Trying to decrypt: "+Bytes.hexString(response));

        if (!isGoodResponse(response)) {
            // nothing to do in an error situation
            return response;
        }

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

    private boolean isGoodResponse(byte[] response) {
        byte[] sw = Bytes.tail(response, 2);
        return sw[0] == (byte)0x90;
    }

}