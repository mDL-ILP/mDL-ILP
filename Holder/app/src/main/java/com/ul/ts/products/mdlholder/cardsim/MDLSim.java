package com.ul.ts.products.mdlholder.cardsim;

import android.content.Context;

import com.ul.ts.products.mdlholder.R;
import com.ul.ts.products.mdlholder.security.AESUtils;
import com.ul.ts.products.mdlholder.security.ECCUtils;
import com.ul.ts.products.mdlholder.security.EllipticCurveParameters;
import com.ul.ts.products.mdlholder.utils.BitUtils;
import com.ul.ts.products.mdlholder.utils.Bytes;
import com.ul.ts.products.mdlholder.utils.HexStrings;
import com.ul.ts.products.mdlholder.utils.StorageUtils;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;

public class MDLSim extends BasicCard {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // r-APDUs
    private static final byte[] FAIL_PACE_AUTHENTICATION = new byte[]{(byte)0x63, (byte)0x00};
    private static final byte[] FAIL_SECURE_MESSAGING_DATA_OBJECT_MISSING = new byte[]{(byte)0x69, (byte)0x87};
    private static final byte[] FAIL_SECURE_MESSAGING_DATA_OBJECT_INCORRECT = new byte[]{(byte)0x69, (byte)0x88};

    // Authentication commands
    private static final byte[] INTERNAL_AUTHENTICATE = new byte[]{0x00, (byte)0x88};

    // PACE commands
    private static final byte[] MANAGE_SECURITY = new byte[]{0x00, (byte)0x22};
    private static final byte[] GENERAL_AUTHENTICATE = new byte[]{0x00, (byte)0x86};
    private static final byte[] GENERAL_AUTHENTICATE_CHAINED = new byte[]{0x10, (byte)0x86};
    private static final byte[] SECURED_COMMAND = new byte[]{0x0C};

    // File IDs
    private static final byte EF_DG1_ID = (byte)0x01;
    private static final byte EF_DG6_ID = (byte)0x06;
    private static final byte EF_DG10_ID = (byte)0x0A;
    private static final byte EF_DG11_ID = (byte)0x0B;
    private static final byte EF_DG13_ID = (byte)0x0D;
    private static final byte EF_DG15_ID = (byte)0x0F;
    private static final byte EF_DG16_ID = (byte)0x10;
    private static final byte EF_CARD_ACCESS_ID = (byte)0x1C;
    private static final byte EF_SOD_ID = (byte)0x1D;
    private static final byte EF_COM_ID = (byte)0x1E;

    private final String password;
    private boolean paceSetUp = false;
    private static final EllipticCurveParameters curveRef = EllipticCurveParameters.brainpoolP320r1;
    private byte[] nonce;
    private byte[] gCircumflex;
    private byte[] TCard;
    private byte[] TTerminal;
    private byte[] KEnc;
    private byte[] KMac;
    private byte[] SSC; // Send Sequence Counter for secure messaging gets set up when a secure channel is successfully established

    /**
     * if full license is false you can only access EF_SOD, EF_DG06, EF_DG15 and EF_DG16.
     */
    private final Context context;
    private final boolean fullAccess;
    private final List<Byte> restrictedFiles = Arrays.asList(EF_DG1_ID, EF_DG11_ID, EF_DG13_ID);

    /**
     * Construct an APDU simulator which you can send data to and receive responses from.
     *
     * The idea is this is a SIM card which holds a drivers license.
     *
     * @param fullAccess true if the caller should have full access to all the files on the SIM.
     *                   false if the caller should only have access to the age related files (DG6, 15, and 16.)
     */
    public MDLSim(Context context, boolean fullAccess, String password) {
        // maxDataLength = 255 should be supported on any transmission layer
        this(context, fullAccess, password, 255);
    }
    public MDLSim(Context context, boolean fullAccess, String password, int maxDataLength) {
        super(maxDataLength);
        this.context = context;
        this.fullAccess = fullAccess;
        this.password = password;

        loadDownloadedData();
    }

    @Override
    protected byte[] getAID() {
        return HexStrings.fromHexString("A0 00 00 02 48 02 00");
    }

    private void loadDownloadedData() {
        Object o = StorageUtils.loadObject(context, context.getString(R.string.data_key));
        fileContent = (Map<Byte, byte[]>) o;
    }

    @Override
    protected byte[] processCommand(final byte[] header, final byte[] command) {
        byte[] response = super.processCommand(header, command);
        if (response != null) {
            return response;
        }

        if (Arrays.equals(header, INTERNAL_AUTHENTICATE)) {
            return processInternalAuthCommand(command);
        }
        else if (Arrays.equals(header, MANAGE_SECURITY)) {
            return processManageSecurityCommand(command);
        }
        else if (Arrays.equals(header, GENERAL_AUTHENTICATE)) {
            return processGeneralAuthenticateCommand(command);
        }
        else if (Arrays.equals(header, GENERAL_AUTHENTICATE_CHAINED)) {
            return processGeneralAuthenticateChainedCommand(command);
        }
        else if (command[0] == SECURED_COMMAND[0]) {
            if (!paceSetUp) {
                return FAIL_PACE_AUTHENTICATION;
            }

            return processSecuredCommand(command);
        }

        return null;
    }

    @Override
    boolean mayReadFile(final byte currentFile) {
        if (fullAccess) {
            return true;
        }

        if (restrictedFiles.contains(currentFile)) {
            return false;
        }

        return true;
    }

    private byte[] processInternalAuthCommand(byte[] command) {

        // Command format is '00 88 00 00 08 RR RR RR RR RR RR RR RR 00'

        byte p1 = command[2];
        byte p2 = command[3];
        byte[] random = Bytes.allButLast(Bytes.allButFirst(command, 5), 1);

        final byte[] p1p2 = new byte[]{p1, p2};
        final byte[] auth = new byte[]{0x00, 0x00};

        if (Arrays.equals(p1p2, auth)) {
            // data holds a random used for active authentication

            // encrypt random using private key
            try {
                KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
                byte[] rawKey = (byte[])StorageUtils.loadObject(context, context.getString(R.string.aaprivatekey_key));
                PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(rawKey));

                Cipher asymmetricCipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
                asymmetricCipher.init(Cipher.ENCRYPT_MODE, privateKey);

                // Message: '6A ++ M1 ++ hash (M1 ++ RND.IFD) ++ 34 CC'

                // Calculate m1
                byte[] m1 = new byte[asymmetricCipher.getBlockSize()-34];
                new Random().nextBytes(m1);
                // Create the hash
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(Bytes.concatenate(m1,random));
                // Calculate the message
                byte[] message = Bytes.concatenate(Bytes.bytes("6A"),m1,hash,Bytes.bytes("34 CC"));
                byte[] encrypted = asymmetricCipher.doFinal(message);

                return Bytes.concatenate(encrypted, SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return FAIL_INCORRECT_DATA;
        }

        return FAIL_FUNCTION_NOT_SUPPORTED;
    }

    private byte[] processManageSecurityCommand(byte[] command) {

        byte p1 = command[2];
        byte p2 = command[3];
        byte lc = command[4];
        byte[] data = Bytes.allButFirst(command, 4);

        final byte[] p1p2 = new byte[]{p1, p2};
        final byte[] mutualAuth = new byte[]{(byte)0xC1, (byte)0xA4};

        if (Arrays.equals(p1p2, mutualAuth)) {
            // data holds TLVs

            // tag 80 = Crypto mechanism

            // tag 83 = Password

            // tag 84 = domain params reference

            // Any other tags are unsupported and a failure will be returned

            paceSetUp = true;
            return SUCCESS;
        }

        return FAIL_FUNCTION_NOT_SUPPORTED;
    }

    private byte[] processGeneralAuthenticateCommand(byte[] command) {

        byte p1 = command[2];
        byte p2 = command[3];
        byte lc = command[4];
        byte[] data = Bytes.allButLast(Bytes.allButFirst(command, 5), 1);
        byte le = Bytes.tail(command, 1)[0];

        final byte[] p1p2 = new byte[]{p1, p2};
        final byte[] keyProtocolImplicitlyKnown = new byte[]{0x00, 0x00};

        if (Arrays.equals(p1p2, keyProtocolImplicitlyKnown) && paceSetUp) {
            try (ASN1InputStream bIn = new ASN1InputStream(data)) {
                DERApplicationSpecific obj = (DERApplicationSpecific) bIn.readObject();
                byte[] tlv = obj.getEncoded();

                // tag 7C = Dynamic Authentication Data
                if (tlv[0] == 0x7C) {
                    try (ASN1InputStream innerIn = new ASN1InputStream(Bytes.allButFirst(tlv, 2))) {
                        ASN1Primitive innerObj = innerIn.readObject();
                        byte[] innerTLV = innerObj.getEncoded();

                        // tag 85 = Terminal's Authentication Token
                        if (innerTLV[0] == (byte) 0x85) {
                            byte[] terminalToken = Bytes.allButFirst(innerTLV, 2);

                            if (Arrays.equals(terminalToken, TTerminal)) {
                                System.out.println("Card Authentication complete"); // Authentication successful from the card's point of view?
                            }

                            // start session counter at 0 since we have successfully opened a secure channel
                            SSC = Bytes.repeated(8, 0x00);

                            byte[] dynamicAuthData = Bytes.bytes("7C");
                            byte[] cardAuthToken = Bytes.bytes("86");
                            byte[] length86 = Bytes.bytes(TCard.length);
                            byte[] length7C = Bytes.bytes(Bytes.bytesToInt(length86)+2);
                            byte[] returnData = Bytes.concatenate(dynamicAuthData, length7C, cardAuthToken, length86, TCard);

                            byte[] completeReturnData = Bytes.concatenate(
                                    returnData,
                                    SUCCESS
                            );

                            return completeReturnData;
                        }
                    }
                }
            }
            catch (Exception e) {
                // TODO(JS): ?
                // return incorrect data
                e.printStackTrace();
            }

            return FAIL_INCORRECT_DATA;
        }

        return FAIL_INCORRECT_P1_P2;
    }

    private byte[] processGeneralAuthenticateChainedCommand(byte[] command) {

        byte p1 = command[2];
        byte p2 = command[3];
        byte lc = command[4];
        byte[] data = Bytes.allButLast(Bytes.allButFirst(command, 5), 1);
        byte le = Bytes.tail(command, 1)[0];

        final byte[] p1p2 = new byte[]{p1, p2};
        final byte[] keyProtocolImplicitlyKnown = new byte[]{0x00, 0x00};

        if (Arrays.equals(p1p2, keyProtocolImplicitlyKnown) && paceSetUp) {
            // data holds TLV

            try (ASN1InputStream bIn = new ASN1InputStream(data)) {
                DERApplicationSpecific obj = (DERApplicationSpecific)bIn.readObject();
                byte[] tlv = obj.getEncoded();

                // tag 7C = Dynamic Authentication Data
                if (tlv[0] == 0x7C) {

                    // No other TLVs under 7C means requesting a nonce value
                    if (tlv[1] == 0x00) {
                        // Internal state matters here, a ManageSecurityCommand should have specified how to generate the NONCE (?)

                        byte[] encryptedNonce = generateNonce();

                        // Return structure is a TLV:
                        // 7C <length> 80 <length> <encrypted nonce> 90 00

                        byte[] totalLength = Bytes.bytes(encryptedNonce.length+2);
                        byte[] nonceLength = Bytes.bytes(encryptedNonce.length);

                        return Bytes.concatenate(Bytes.bytes("7C"), totalLength, Bytes.bytes("80"), nonceLength, encryptedNonce, SUCCESS);
                    }
                    else {
                        try (ASN1InputStream innerIn = new ASN1InputStream(Bytes.allButFirst(tlv, 2))) {
                            ASN1Primitive innerObj = innerIn.readObject();
                            byte[] innerTLV = innerObj.getEncoded();

                            // tag 81 = Mapping Data
                            if (innerTLV[0] == (byte)0x81) {

//                                byte uncompressedPoint = innerTLV[2];
//                                byte[] coords = Bytes.allButFirst(innerTLV, 3);
//                                byte[] xCoord = Bytes.sub(coords, 0, (coords.length/2));
//                                byte[] yCoord = Bytes.sub(coords, (coords.length/2), coords.length);
//
//                                System.out.println("point: "+Bytes.hexString(new byte[]{uncompressedPoint}));
//                                System.out.println("xCoord: "+Bytes.hexString(xCoord));
//                                System.out.println("yCoord: "+Bytes.hexString(yCoord));

                                // Get the terminals Public key
                                PublicKey terminalPublicKey = ECCUtils.encodeECCPublicKeyX509(Bytes.allButFirst(innerTLV, 2), curveRef);
//
//                                System.out.println("Terminal PublicKey: "+Bytes.hexString(terminalPublicKey.getEncoded()));

                                // Send back the card's public key
                                KeyPair ephemeralKeys = ECCUtils.generateEphemeralKeys(curveRef);

                                byte[] sharedSecret = ECCUtils.calculateECKAShS(ephemeralKeys.getPrivate(), terminalPublicKey);
//                                System.out.println("Card Shared Secret: "+Bytes.hexString(sharedSecret));
//
                                gCircumflex = ECCUtils.calculateECDHGenericMapping(nonce, sharedSecret, curveRef);
//                                System.out.println("Card gCircumflex: "+Bytes.hexString(gCircumflex));

//                                System.out.println("Card PublicKey: "+Bytes.hexString(paceKeys.getPublic().getEncoded()));

                                byte[] pub = ECCUtils.decodeECCPublicKeyX509(ephemeralKeys.getPublic(), curveRef);
                                byte[] x = ECCUtils.getXFromUncompressedFormat(pub);
                                byte[] y = ECCUtils.getYFromUncompressedFormat(pub);

                                byte[] dynamicAuthData = Bytes.bytes("7C");
                                byte[] mappingData = Bytes.bytes("82");
                                byte[] uncompressedPoint = Bytes.bytes("04");
                                byte[] length82 = Bytes.bytes(uncompressedPoint.length + x.length + y.length);
                                byte[] length7C = Bytes.bytes(Bytes.bytesToInt(length82)+2);
                                byte[] returnData = Bytes.concatenate(dynamicAuthData, length7C, mappingData, length82, uncompressedPoint, x, y);

                                byte[] completeReturnData = Bytes.concatenate(
                                        returnData,
                                        SUCCESS
                                );

                                return completeReturnData;
                            }
                            // tag 83 = Key Agreement
                            else if (innerTLV[0] == (byte)0x83) {

                                EllipticCurveParameters newCurveDomainParams = EllipticCurveParameters.specifyCurve(curveRef.getA(), curveRef.getB(), ECCUtils.getXFromUncompressedFormat(gCircumflex), ECCUtils.getYFromUncompressedFormat(gCircumflex), curveRef.getH(), curveRef.getN(), curveRef.getP(), curveRef.getCurveSize());
                                PublicKey terminalPublicKey = ECCUtils.encodeECCPublicKeyX509(Bytes.allButFirst(innerTLV, 2), newCurveDomainParams);

//                                System.out.println("newTerminalPublicKey: "+Bytes.hexString(terminalPublicKey.getEncoded()));

                                KeyPair ephemeralKeys = ECCUtils.generateEphemeralKeys(newCurveDomainParams);

                                byte[] pub = ECCUtils.decodeECCPublicKeyX509(ephemeralKeys.getPublic(), newCurveDomainParams);
                                byte[] x = ECCUtils.getXFromUncompressedFormat(pub);
                                byte[] y = ECCUtils.getYFromUncompressedFormat(pub);

//                                System.out.println("Card cardPublicKey     "+Bytes.hexString(ephemeralKeys.getPublic().getEncoded()));

                                byte[] sharedSecretX = ECCUtils.getXFromUncompressedFormat(ECCUtils.calculateECKAShS(ephemeralKeys.getPrivate(), terminalPublicKey));

                                KEnc = ECCUtils.calculateKEnc(sharedSecretX);
                                KMac = ECCUtils.calculateKMac(sharedSecretX);

//                                System.out.println("KEac: "+Bytes.hexString(KEnc));
//                                System.out.println("KMac: "+Bytes.hexString(KMac));

                                byte[] terminalPublicKeyBytes = ECCUtils.decodeECCPublicKeyX509(terminalPublicKey, newCurveDomainParams);

                                TCard = AESUtils.performCBC8(terminalPublicKeyBytes, KMac);
                                TTerminal = AESUtils.performCBC8(pub, KMac);

                                // return 0x84 tag
                                byte[] dynamicAuthData = Bytes.bytes("7C");
                                byte[] mappingData = Bytes.bytes("84");
                                byte[] uncompressedPoint = Bytes.bytes("04");
                                byte[] length82 = Bytes.bytes(uncompressedPoint.length + x.length + y.length);
                                byte[] length7C = Bytes.bytes(Bytes.bytesToInt(length82)+2);
                                byte[] returnData = Bytes.concatenate(dynamicAuthData, length7C, mappingData, length82, uncompressedPoint, x, y);

                                byte[] completeReturnData = Bytes.concatenate(
                                        returnData,
                                        SUCCESS
                                );

                                return completeReturnData;
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                // TODO(JS): ?
                // return incorrect data
                e.printStackTrace();
            }

            // Any other tags are unsupported and a failure will be returned

            return FAIL_INCORRECT_DATA;
        }
        else {
            return FAIL_INCORRECT_P1_P2;
        }
    }

    private byte[] processSecuredCommand(byte[] command) {

        byte cla = command[0];
        byte newCla = 0x00;
        byte ins = command[1];
        byte p1 = command[2];
        byte p2 = command[3];
        byte lc = command[4];
        //byte[] data = Bytes.allButLast(Bytes.allButFirst(command, 5), 1);
        byte le = Bytes.tail(command, 1)[0];

        byte[] CC = Bytes.allButLast(Bytes.allButFirst(command, command.length-9), 1);

        byte[] paddedCommandHeader = new byte[] {cla, ins, p1, p2, (byte)0x80, 0x00, 0x00, 0x00};

        byte[] partialCommand = Bytes.concatenate(paddedCommandHeader, Bytes.allButLast(Bytes.allButFirst(command, 5), 11));

        int ssc = Bytes.bytesToInt(SSC)+1;
        SSC = Bytes.bytes(ssc);
        byte[] newSSC = new byte[8];
        System.arraycopy(SSC, 0, newSSC, newSSC.length - SSC.length, SSC.length);
        SSC = newSSC;

        byte[] mac = AESUtils.performCBC8(Bytes.concatenate(SSC, partialCommand), KMac);

        if (!Arrays.equals(mac, CC)) {
            // failed integrity check
            return FAIL_SECURITY;
        }

        byte[] encryptedCommandData = Bytes.allButLast(Bytes.allButFirst(partialCommand, 11), 3);

        byte[] data = new byte[]{};

        if (encryptedCommandData.length > 0) {
            byte[] decryptedCommandData = AESUtils.decryptAESCBC(encryptedCommandData, KEnc);

            byte[] unpaddedDecryptedData = AESUtils.removeAESPadding(decryptedCommandData);

            data = Bytes.concatenate(Bytes.bytes(unpaddedDecryptedData.length), unpaddedDecryptedData);
        }

        byte[] rebuiltCommand = Bytes.concatenate(new byte[]{newCla, ins, p1, p2}, data, Bytes.bytes(le));

//        System.out.println("rebuilt: "+Bytes.hexString(rebuiltCommand));

        return secureResponse(send(rebuiltCommand));
    }

    private byte[] secureResponse(byte[] response) {

        byte[] rawData = Bytes.allButLast(response, 2);
        byte[] statusword = Bytes.allButFirst(response, response.length-2);

        byte[] data87 = new byte[]{};
        if (rawData.length > 0) {
            byte[] paddedData = AESUtils.addAESPadding(rawData);
            byte[] encryptedData = AESUtils.encryptAESCBC(paddedData, KEnc);

            byte[] data87Length = Bytes.bytes(encryptedData.length);

            data87 = Bytes.concatenate(Bytes.bytes("87"), data87Length, Bytes.bytes("01"), encryptedData);
        }

        byte[] data99 = Bytes.concatenate(Bytes.bytes("99 02"), statusword);

        int ssc = Bytes.bytesToInt(SSC)+1;
        SSC = Bytes.bytes(ssc);
        byte[] newSSC = new byte[8];
        System.arraycopy(SSC, 0, newSSC, newSSC.length - SSC.length, SSC.length);
        SSC = newSSC;

        byte[] CC = AESUtils.performCBC8(Bytes.concatenate(SSC, data87, data99), KMac);

        byte[] data8E = Bytes.concatenate(Bytes.bytes("8E 08"), CC);

        byte[] finalResponse = Bytes.concatenate(data87, data99, data8E, statusword);

        return finalResponse;
    }

    private byte[] generateNonce() {
        // Internal state matters here, a ManageSecurityCommand should have specified how to generate the NONCE (?)

        nonce = new byte[32];
        new Random().nextBytes(nonce);

        try {
            // KPI = sha256(CAN || '00 00 00 03')
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] kpi =  md.digest(Bytes.concatenate(password.getBytes(), Bytes.bytes("00 00 00 03")));

            return AESUtils.encryptAESCBC(nonce, kpi);

        } catch (NoSuchAlgorithmException e) {
            //Log.e(getClass().getName(), "Problem with encryption", e);
            e.printStackTrace();
        }

        return null;
    }
}
