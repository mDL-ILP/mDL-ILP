package com.ul.ts.products.mdlreader.data;

import android.graphics.Bitmap;
import android.util.Log;

import com.ul.ts.products.mdlreader.utils.ByteUtils;
import com.ul.ts.products.mdlreader.utils.Bytes;
import com.ul.ts.products.mdlreader.utils.ParsingUtils;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.BERTags;
import org.bouncycastle.asn1.DERApplicationSpecific;
import org.bouncycastle.asn1.DLSequence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class DrivingLicence {

    // DG1
    // 5F01
    private String typeApprovalNumber;
    // 5F03
    private String issuingMemberState;
    // 5F04
    private String surnamesOfTheHolder;
    // 5F05
    private String otherNamesOfTheHolder;
    // 5F06 (ddmmyyyy)
    private String dateOfBirth;
    // 5F07
    private String placeOfBirth;
    // 5F08
    private String nationality;
    // 5F09
    private String gender;
    // 5F0A
    private String dateOfIssue;
    // 5F0B
    private String dateOfExpiry;
    // 5F0C
    private String issuingAuthority;
    // 5F0D
    private String administrativeNumber;
    // 5F0E
    private String documentNumber;
    // 5F0F
    private String placeOfResidence;

    // 7F 63 - Categories
    private List<Category> categories;

    // DG5 Signature
    private byte[] signature;
    // DG6 Photo
    private byte[] photo;

    // DG10
    // 5F41
    private String formFactor;
    // 5F42
    private String upToDatePolicyVersion;
    // 5F43
    private String upToDatePolicyLast;
    // 5F44
    private int upToDatePolicyInterval;
    // 5F45
    private int upToDatePolicyLimit;
    // daysSinceUpdate
    private String daysSinceUpdate;

    // QR
    private Bitmap qr;

    // MRZ
    private String mrz;
    // BSN
    private String bsn;

    // DG 15 and 16
    private boolean is18;
    private boolean is21;
    private int age;

    // Hashes
    private byte[] hashDG1;
    private byte[] hashDG6;
    private byte[] hashDG10;
    private byte[] hashDG11;
    private byte[] hashDG13;
    private byte[] hashDG15;
    private byte[] hashDG16;
    private byte[] hashEFSOd;
    private X509Certificate dsCert;
    // checks
    private boolean PassiveAuthDG1;
    private boolean PassiveAuthDG6;
    private boolean PassiveAuthDG10;
    private boolean PassiveAuthDG11;
    private boolean PassiveAuthDG13;
    private boolean PassiveAuthDG15;
    private boolean PassiveAuthDG16;
    private boolean PassiveAuth;
    private String PassiveAuthFailureReason;
    private boolean ActiveAuth;
    private String ActiveAuthFailureReason;
    // AA PublicKey
    private PublicKey aaPublicKey;
    // EFSOd content
    private byte[] idsSecurityObject;
    // If GAT isSample is true, if production isSample is false
    private boolean isSample;
    private String issuerDn;

    private DateFormat dg10Format = new SimpleDateFormat("yyyyMMddhhmmss");

    private enum PictureType {PHOTO, SIGNATURE}

    /**
     * Picture and age only
     */
    public DrivingLicence(byte[] DG6, byte[] DG15, byte[] DG16, byte[] EFSOd, byte[] randomForAA, byte[] responseToInternalAuth) {

        parsePicture(DG6, PictureType.PHOTO);

        hashDG6 = calculateHash(DG6);
        hashDG15 = calculateHash(DG15);
        hashDG16 = calculateHash(DG16);
        hashEFSOd = calculateHash(EFSOd);

        parseDG15(DG15);
        parseDG16(DG16);
        parseEFSOd(EFSOd);

        passiveAuthShort();
        activeAuth(randomForAA, responseToInternalAuth);

    }

    // constructor with all checks
    public DrivingLicence(byte[] DG1, byte[] DG6, byte[] DG10,
                          byte[] DG11, byte[] DG13, byte[] DG15, byte[] DG16, byte[] EFSOd,
                          byte[] randomForAA, byte[] responseToInternalAuth/*, String mrz*/) {
        parseDG1(DG1);

        parsePicture(DG6, PictureType.PHOTO);
        parseDG10(DG10);
        parseDG11(DG11);

        hashDG1 = calculateHash(DG1);
        hashDG6 = calculateHash(DG6);
        hashDG10 = calculateHash(DG10);
        hashDG11 = calculateHash(DG11);
        hashDG13 = calculateHash(DG13);
        hashDG15 = calculateHash(DG15);
        hashDG16 = calculateHash(DG16);
        hashEFSOd = calculateHash(EFSOd);

        parseDG13(DG13);
        parseDG15(DG15);
        parseDG16(DG16);
        parseEFSOd(EFSOd);
        passiveAuth();
        activeAuth(randomForAA, responseToInternalAuth);

        Log.d("PassiveAuth", String.valueOf(this.PassiveAuth));
        Log.d("ActiveAuth", String.valueOf(this.ActiveAuth));
    }

    // Online full license
    public DrivingLicence(byte[] DG1, byte[] DG6, byte[] DG10,
                          byte[] DG11, byte[] EFSOd) {
        parseDG1(DG1);

        parsePicture(DG6, PictureType.PHOTO);
        parseDG10(DG10);
        parseDG11(DG11);

        hashDG1 = calculateHash(DG1);
        hashDG6 = calculateHash(DG6);
        hashDG10 = calculateHash(DG10);
        hashDG11 = calculateHash(DG11);
        hashEFSOd = calculateHash(EFSOd);

        parseEFSOd(EFSOd);
        passiveAuth();


        Log.d("PassiveAuth", String.valueOf(this.PassiveAuth));
    }

    // Online age license
    public DrivingLicence(byte[] DG6,
                          byte[] DG15, byte[] DG16, byte[] EFSOd) {

        parsePicture(DG6, PictureType.PHOTO);

        hashDG6 = calculateHash(DG6);
        hashDG15 = calculateHash(DG15);
        hashDG16 = calculateHash(DG16);
        hashEFSOd = calculateHash(EFSOd);

        parseDG15(DG15);
        parseDG16(DG16);
        parseEFSOd(EFSOd);

        passiveAuthShort();
        Log.d("PassiveAuth", String.valueOf(this.PassiveAuth));
    }


    private void parseDG1(byte[] DG1) {
        try {
            ASN1InputStream bIn = new ASN1InputStream(DG1);
            org.bouncycastle.asn1.DERApplicationSpecific app = (DERApplicationSpecific) bIn.readObject();

            ASN1Sequence seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);
            Enumeration secEnum = seq.getObjects();
            while (secEnum.hasMoreElements()) {
                ASN1Primitive seqObj = (ASN1Primitive) secEnum.nextElement();
                byte[] data = seqObj.getEncoded();
                if (data[0]== 0x41) {
                    Log.d("type approval number", ByteUtils.bytesToHex(data));
                    this.set5F01(data);
                } else if (data[0] == 0x42) {
                    byte[] input = Arrays.copyOfRange(data, 3, data.length);
                    parse5F02(input);
                } else if (data[0] == 0x7F) {
                    parse7F63(data);
                }
            }
            bIn.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parse5F02(byte[] input) {
        Log.d("input", ByteUtils.bytesToHex(input));
        try {
            ASN1InputStream bIn = new ASN1InputStream(input);
            ASN1Primitive obj;
            while ((obj = bIn.readObject())!= null) {
                byte[] data = obj.getEncoded();
                Log.d("5F02data", ByteUtils.bytesToHex(data));
                switch (data[0]) {
                    case 0x43:
                        this.set5F03(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x44:
                        this.set5F04(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x45:
                        this.set5F05(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x46:
                        this.set5F06(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x47:
                        this.set5F07(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x48:
                        this.set5F08(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x49:
                        this.set5F09(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x4A:
                        this.set5F0A(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x4B:
                        this.set5F0B(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x4C:
                        this.set5F0C(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x4E:
                        this.set5F0E(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    case 0x4F:
                        this.set5F0F(Arrays.copyOfRange(data, 2, data.length));
                        break;
                    default:
                        Log.d("UNKNOWN_TAG", "unknown tag");
                }
                bIn.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parse7F63(byte[] input) {
        Log.d("input", ByteUtils.bytesToHex(input));
        try {
            ASN1InputStream bIn = new ASN1InputStream(input);
            ASN1Primitive obj = bIn.readObject();
            DERApplicationSpecific app = (DERApplicationSpecific) obj;
            ASN1Sequence seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);
            Enumeration secEnum = seq.getObjects();
            List<byte[]> categories = new ArrayList<>();
            while (secEnum.hasMoreElements()) {
                ASN1Primitive seqObj = (ASN1Primitive) secEnum.nextElement();
                byte[] data = seqObj.getEncoded();
                Log.d("5F02data", ByteUtils.bytesToHex(data));
                switch (data[0]) {
                    case 0x02:
                        Log.d("#CATEGORY","number of categories:" + data[data.length-1]);
                        break;
                    case (byte) 0x87:
                        categories.add(Arrays.copyOfRange(data, 2, data.length));
                        break;
                }
            }
            bIn.close();
            this.set7F63(categories);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parsePicture(byte[] data, PictureType type) {
        byte[] image = ParsingUtils.getImage(data);
        int i = 0;
        while ((image[0]!=(byte) 0xFF) && i < 3) {
            image = Arrays.copyOfRange(image, 1, image.length);
            i++;
        }
        switch (type) {
            case PHOTO:
                this.photo = image;
            case SIGNATURE:
                this.signature = image;
            default:
                // Something went wrong
        }
    }

    private void parseDG10(byte[] DG10) {
        try {
            ASN1InputStream bIn = new ASN1InputStream(DG10);
            org.bouncycastle.asn1.DERApplicationSpecific app = (DERApplicationSpecific) bIn.readObject();


            ASN1Sequence seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);
            Enumeration secEnum = seq.getObjects();
            while (secEnum.hasMoreElements()) {
                ASN1Primitive seqObj = (ASN1Primitive) secEnum.nextElement();
                byte[] data = seqObj.getEncoded();
                if (data[0]== 0x5F) {

                    if (data[1] == 0x41) { // iDL form factor, mobile, binary encoded
                        String formFactorString = Bytes.hexString(Bytes.allButFirst(data, 3));
                        Log.d(getClass().getName(), "formFactorString: "+formFactorString);
                        this.set5F41(formFactorString);
                    }
                    else if (data[1] == 0x42) { // Up to data policy version, binary encoded
                        String policyVersion = Bytes.hexString(Bytes.allButFirst(data, 3));
                        Log.d(getClass().getName(), "upToDatePolicyVersion: "+policyVersion);
                        this.set5F42(policyVersion);
                    }
                    else if (data[1] == 0x43) { // Last update timestamp, encoded as yyyyMMddhhmmss
                        String timestampString = Bytes.unspacedHexString(Bytes.allButFirst(data, 3));
                        try {
                            Log.d(getClass().getName(), "upToDatePolicyLast: "+timestampString);
                            this.set5F43(timestampString);

                            // Todo: when we change the server side to UTC, update this to localtime
                            Date date =  dg10Format.parse(timestampString);
                            long timeDiffMillies = (new java.util.Date()).getTime() - date.getTime();
                            long timeDiffDays = timeDiffMillies / 86400000;
                            this.setDaysSinceUpdate(Long.toString(timeDiffDays));

                        } catch (ParseException e) {
                            Log.e(getClass().getName(), e.getMessage(), e);
                        }
                    }
                    else if (data[1] == 0x44) { // Up to date default interval, in days since last update, binary encoded
                        String daysString = Bytes.hexString(Bytes.allButFirst(data, 3));
                        int days = Bytes.bytesToInt(Bytes.allButFirst(data, 3));
                        Log.d(getClass().getName(), "upToDatePolicyInterval: "+days);
                        this.set5F44(days);
                    }
                    else if (data[1] == 0x45) { // Date to be updated, in days since last update, binary encoded
                        String daysString = Bytes.hexString(Bytes.allButFirst(data, 3));
                        int days = Bytes.bytesToInt(Bytes.allButFirst(data, 3));
                        Log.d(getClass().getName(), "upToDatePolicyLimit: "+days);
                        this.set5F45(days);
                    }
                }
            }
            bIn.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parseDG11(byte[] DG11) {
        try {
            ASN1InputStream bIn = new ASN1InputStream(DG11);
            org.bouncycastle.asn1.DERApplicationSpecific app = (DERApplicationSpecific) bIn.readObject();

            ASN1Sequence seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);
            Enumeration secEnum = seq.getObjects();
            while (secEnum.hasMoreElements()) {
                ASN1Primitive seqObj = (ASN1Primitive) secEnum.nextElement();
                byte[] data = seqObj.getEncoded();
                if (data[0]== 0x7F) {
                    parseDG11(data);
                } else if (data[0] == (byte) 0x80) {
                    this.setBSN(Arrays.copyOfRange(data, 2, data.length));
                }
            }
            bIn.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parseDG13(byte[] DG13) {
        byte[] keybytes = Arrays.copyOfRange(DG13, 3, DG13.length);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec (keybytes);
        try {
            this.aaPublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parseDG15(byte[] DG15) {

        try (ASN1InputStream bIn = new ASN1InputStream(DG15)) {
            DERApplicationSpecific app = (DERApplicationSpecific) bIn.readObject();

            ASN1Sequence seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);
            byte[] data = ((ASN1Primitive)seq.getObjects().nextElement()).getEncoded();

            Log.d(getClass().getName(), "Data = "+ Bytes.hexString(data));

            try (ASN1InputStream in = new ASN1InputStream(data)) {
                Enumeration seq1 = ((DLSequence) in.readObject()).getObjects();

                while (seq1.hasMoreElements()) {
                    ASN1Primitive obj = (ASN1Primitive)seq1.nextElement();
                    byte[] data1 = obj.getEncoded();
                    Log.d(getClass().getName(), "data1 = "+ Bytes.hexString(data1));

                    if (data1[0] == (byte) 0x01) {
                        this.set18(data1[2] == 0x01);
                    } else if (data1[0] == (byte) 0x02) {
                        this.setAge(Bytes.toInt(data1[2]));
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parseDG16(byte[] DG16) {

        try (ASN1InputStream bIn = new ASN1InputStream(DG16)) {
            DERApplicationSpecific app = (DERApplicationSpecific) bIn.readObject();

            ASN1Sequence seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);
            byte[] data = ((ASN1Primitive)seq.getObjects().nextElement()).getEncoded();

            Log.d(getClass().getName(), "Data = "+ Bytes.hexString(data));

            try (ASN1InputStream in = new ASN1InputStream(data)) {
                Enumeration seq1 = ((DLSequence) in.readObject()).getObjects();

                while (seq1.hasMoreElements()) {
                    ASN1Primitive obj = (ASN1Primitive)seq1.nextElement();
                    byte[] data1 = obj.getEncoded();
                    Log.d(getClass().getName(), "data1 = "+ Bytes.hexString(data1));

                    if (data1[0] == (byte) 0x01) {
                        this.set21(data1[2] == 0x01);
                    } else if (data1[0] == (byte) 0x02) {
                        this.setAge(Bytes.toInt(data1[2]));
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void parseEFSOd(byte[] EFSOd) {
        try {
            ASN1InputStream bIn = new ASN1InputStream(EFSOd);
            ASN1Primitive obj = bIn.readObject();
            ASN1Sequence seq;
            if (obj instanceof DERApplicationSpecific) {
                org.bouncycastle.asn1.DERApplicationSpecific app = (DERApplicationSpecific) obj;
                seq = (ASN1Sequence) app.getObject(BERTags.SEQUENCE);
            } else if (obj instanceof ASN1Sequence) {
                seq = (ASN1Sequence) obj;
            } else if (obj instanceof org.bouncycastle.asn1.DERTaggedObject) {
                seq = new org.bouncycastle.asn1.DERSequence(((org.bouncycastle.asn1.DERTaggedObject) obj).getObject());
            } else {
                seq = null;
                Log.d("something else","null");
                //org.bouncycastle.asn1.DERApplicationSpecific app = (DERApplicationSpecific) obj;
            }

            Enumeration secEnum = seq.getObjects();
            while (secEnum.hasMoreElements()) {
                ASN1Primitive seqObj = (ASN1Primitive) secEnum.nextElement();
                byte[] data = seqObj.getEncoded();
                Log.d("data", ByteUtils.bytesToHex(data));
                if (data[0] == (byte)0x30) {
                    Log.d("start", "30");
                    if (data.length > 30) {
                        if (data[10]==(byte)0x02 && data[11]==(byte)0x01 && data[12]==(byte)0x02) {
                            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                            InputStream in = new ByteArrayInputStream(data);
                            dsCert = (X509Certificate)certFactory.generateCertificate(in);
                            Log.d("cert","created");
                        } else
                            parseEFSOd(data);
                    } else
                        parseEFSOd(data);
                } else if (data[0] == (byte)0x31) {
                    Log.d("start", "31");
                    //parseEFSOd(data);
                } else if (data[0] == (byte)0x02) {
                    Log.d("start", "02");
                    Log.d("02", ByteUtils.bytesToHex(data));
                } else if (data[0] == (byte)0xA0) {
                    Log.d("start", "A0");
                    parseEFSOd(data);
                } else if (data[0] == (byte)0x04) {
                    Log.d("start", "04");
                    Log.d("IdsSecurityObject", ByteUtils.bytesToHex(data));
                    this.idsSecurityObject = Arrays.copyOfRange(data, 4, data.length);
                } else if (data[0] == (byte)0x06) {
                    Log.d("start", "06");
                    Log.d("OID", ByteUtils.bytesToHex(data));
                } else if (data[0] == (byte)0x05) {
                    Log.d("start", "05");
                    Log.d("other", ByteUtils.bytesToHex(data));
                } else if (data[0] == (byte)0x17) {
                    Log.d("start", "17");
                    Log.d("17 time?", ByteUtils.bytesToHex(data));
                } else {
                    Log.d("start", "other");
                    Log.d("other", ByteUtils.bytesToHex(data));
                    //parseEFSOd(data);
                }
            }
            bIn.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Getters:
    // 1
    // NL: NAAM
    public String getName() {
        if (this.surnamesOfTheHolder == null)
            return "-";
        return this.surnamesOfTheHolder;
    }

    // 2
    // NL: VOORNAMEN
    public String getFirstNames() {
        if (this.otherNamesOfTheHolder == null)
            return "-";
        return this.otherNamesOfTheHolder;
    }

    // 3
    // NL: GEBOORTEDATUM EN -PLAATS
    public String getDateAndPlaceOfBirth() {
        return this.dateOfBirth + "     " + this.placeOfBirth;
    }

    public String getDateBirth() {
        return dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    // 4a
    // NL: AFGIFTEDATUM
    public String getDateOfIssue() {
        if (this.dateOfIssue == null)
            return "-";
        return this.dateOfIssue;
    }

    // 4b
    // NL: DATUM GELDIG TOT
    public String getDateOfExpiry() {
        if (this.dateOfExpiry == null)
            return "-";
        return this.dateOfExpiry;
    }

    // 4c
    // NL: AFGEGEVEN DOOR
    public String getIssuingAuthority() {
        if (this.issuingAuthority == null)
            return "-";
        return "-";
        //return this.issuingAuthority;
    }

    // 5
    // NL: RIJBEWIJSNUMMER
    public String getDocumentNumber() {
        if (this.documentNumber == null)
            return "-";
        return this.documentNumber;
    }

    // 6
    // MISSING

    // 7
    // NL: HANDTEKENING
    public byte[] getSignature() {
        return this.signature;
    }

    // 8
    // MISSING

    // 9
    // NL: CATEGORIEEN

    // As single String
    public String getCategoriesAsString() {
        String result = "";
        if (this.categories == null)
            return result;
        for (Category cat : this.categories) {
            if (result == null || result.isEmpty())
                result = cat.getLabelAsString();
            else
                result = result + "-" + cat.getLabelAsString();
        }
        return result;
    }

    // 9-12
    // NL: CATEGORIEEN, VAN, TOT, BEPERKINGEN/VERMELDINGEN
    // List with categories
    public List<Category> getCategories() {
        return this.categories;
    }

    // 12
    // NL: BEPERKINGEN/VERMELDINGEN
    public String getRestrictionAsString() {
        String result = "";
        // TODO: get restrictions that apply to all
		/*if (this.categories == null)
			return result;
		for (Category cat : this.categories) {
			if (result.isEmpty() || result == null || result.equalsIgnoreCase("-"))
				result = cat.getRestrictions();
			else
				result = result + "-" + cat.getRestrictions();
		}*/
        return result;
    }

    public String getDaysSinceUpdate() {
        if (this.daysSinceUpdate == null)
            return "-";
        return daysSinceUpdate;
    }

    // MRZ
    public String getMrz() {
        if (this.mrz == null)
            return "";
        return this.mrz;
    }

    // BSN
    public String getBSN() {
        if (this.bsn == null)
            return "123456789/";
        return this.bsn + "/";
    }

    // Text for transparent circle
    public String getNlPlusYearOfBirth() {
        return "NL" + dateOfBirth.substring(dateOfBirth.length()-2);
    }

    // Photo
    public byte[] getPhoto() {
        return this.photo;
    }

    // QR
    public Bitmap getQR() {
        return this.qr;
    }

    public boolean getPassiveAuth() {
        return PassiveAuth;
    }

    public String getPassiveAuthIssue() {
        if (PassiveAuthFailureReason != null) {
            return PassiveAuthFailureReason;
        }
        return "Unknown";
    }

    public boolean getActiveAuth() {
        return ActiveAuth;
    }

    public String getActiveAuthIssue() {
        if (ActiveAuthFailureReason != null) {
            return ActiveAuthFailureReason;
        }
        return "Unknown";
    }

    // NL: AFGEGEVEN DOOR
    public String getIssuerDn() {
        if (this.issuerDn == null)
            return "-";
        return this.issuerDn;
    }

    // Setters:
    // 5F01 typeApprovalNumber
    public void set5F01(byte[] input) {
        Log.d("typeApprovalNumber",new String(input));
        this.typeApprovalNumber = new String(input);
    }

    // 5F03 issuingMemberState;
    public void set5F03(byte[] input) {
        Log.d("issuingMemberState",new String(input));
        this.issuingMemberState = new String(input);
    }
    // 5F04 surnamesOfTheHolder;
    public void set5F04(byte[] input) {
        Log.d("surnamesOfTheHolder",new String(input));
        this.surnamesOfTheHolder = new String(input);
    }
    // 5F05 otherNamesOfTheHolder;
    public void set5F05(byte[] input) {
        try {
            Log.d("otherNamesOfTheHolder",new String(input, "ISO-8859-1"));
            this.otherNamesOfTheHolder = new String(input, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    // 5F06 (ddmmyyyy) dateOfBirth;
    public void set5F06(byte[] input) {
        Log.d("dateOfBirth", ParsingUtils.addDotsToDate(ParsingUtils.BCDtoString(input)));
        this.dateOfBirth = ParsingUtils.addDotsToDate(ParsingUtils.BCDtoString(input));
    }
    // 5F07 placeOfBirth;
    public void set5F07(byte[] input) {
        Log.d("placeOfBirth",new String(input));
        this.placeOfBirth = new String(input);
    }
    // 5F08 nationality;
    public void set5F08(byte[] input) {
        Log.d("nationality",new String(input));
        this.nationality = new String(input);
    }
    // 5F09 gender;
    public void set5F09(byte[] input) {
        Log.d("gender",new String(input));
        this.gender = new String(input);
    }
    // 5F0A dateOfIssue;
    public void set5F0A(byte[] input) {
        Log.d("dateOfIssue",ParsingUtils.addDotsToDate(ParsingUtils.BCDtoString(input)));
        this.dateOfIssue = ParsingUtils.addDotsToDate(ParsingUtils.BCDtoString(input));
    }
    // 5F0B dateOfExpiry;
    public void set5F0B(byte[] input) {
        Log.d("dateOfExpiry",ParsingUtils.addDotsToDate(ParsingUtils.BCDtoString(input)));
        this.dateOfExpiry = ParsingUtils.addDotsToDate(ParsingUtils.BCDtoString(input));
    }
    // 5F0C issuingAuthority;
    public void set5F0C(byte[] input) {
        Log.d("issuingAuthority",new String(input));
        this.issuingAuthority = new String(input);
    }
    // 5F0D administrativeNumber;
    public void set5F0D(byte[] input) {
        Log.d("administrativeNumber",new String(input));
        this.administrativeNumber = new String(input);
    }
    // 5F0E documentNumber;
    public void set5F0E(byte[] input) {
        Log.d("documentNumber",new String(input));
        this.documentNumber = new String(input);
    }
    // 5F0F placeOfResidence;
    public void set5F0F(byte[] input) {
        Log.d("placeOfResidence",new String(input));
        this.placeOfResidence = new String(input);
    }
    // 5F0F placeOfResidence;
    public void setBSN(byte[] input) {
        Log.d("BSN",new String(input));
        this.bsn = new String(input);
    }

    // 5F41 formFactor
    public void set5F41(String formFactor) {
        this.formFactor = formFactor;
    }

    // 5F42 upToDatePolicyVersion
    public void set5F42(String upToDatePolicyVersion) {
        this.upToDatePolicyVersion = upToDatePolicyVersion;
    }

    // 5F43 upToDatePolicyLast
    public void set5F43(String upToDatePolicyLast) {
        this.upToDatePolicyLast = upToDatePolicyLast;
    }

    // 5F44 upToDatePolicyInterval
    public void set5F44(int upToDatePolicyInterval) {
        this.upToDatePolicyInterval = upToDatePolicyInterval;
    }

    // 5F45 upToDatePolicyLimit
    public void set5F45(int upToDatePolicyLimit) {
        this.upToDatePolicyLimit = upToDatePolicyLimit;
    }

    public void setDaysSinceUpdate(String daysSinceUpdate) {
        this.daysSinceUpdate = daysSinceUpdate;
    }

    // QR
    public void setQRandMRZ(String input) {
        Log.d("mrz",input);
        this.mrz = input;
        Bitmap temp = null;//QRUtils.getQR(input);
        if (temp==null)
            Log.d("qr","is null");
        else
            Log.d("qr","is not null");
        this.qr = temp;
    }

    // 7F63 Categories
    private void set7F63(List<byte[]> input) {
        List<Category> catList = new ArrayList<>();
        Log.d("#CATEGORY", Integer.toString(input.size()));
        for (byte[] b : input) {
            String in = new String(b);
            String[] parts = in.split(";");
            Category.CategoryLabel label = findLabelByString(parts[0]);
            String fromDate = ParsingUtils.addDotsToDate(ParsingUtils.BCDtoString(Arrays.copyOfRange(b, parts[0].length()+1,  parts[0].length()+1+4)));
            String expiryDate = ParsingUtils.addDotsToDate(ParsingUtils.BCDtoString(Arrays.copyOfRange(b, parts[0].length()+1+4+1,  parts[0].length()+1+4+1+4)));
            String restrictions = "";
            for (int i=3; i<parts.length; i++) {
                restrictions = restrictions + parts[i];
            }
            Log.d("fromDate",fromDate);
            Log.d("expiryDate",expiryDate);
            Log.d("restrictions",restrictions);
            Category cat = new Category(label, fromDate, expiryDate, restrictions);
            catList.add(cat);
        }
        this.categories = catList;
    }

    private Category.CategoryLabel findLabelByString(String input) {
        Category.CategoryLabel label;
        Log.d("Category",input);
        if (input.equalsIgnoreCase("AM"))
            label = Category.CategoryLabel.AM;
        else if (input.equalsIgnoreCase("A1"))
            label = Category.CategoryLabel.A1;
        else if (input.equalsIgnoreCase("A2"))
            label = Category.CategoryLabel.A2;
        else if (input.equalsIgnoreCase("A"))
            label = Category.CategoryLabel.A;
        else if (input.equalsIgnoreCase("B1"))
            label = Category.CategoryLabel.B1;
        else if (input.equalsIgnoreCase("B"))
            label = Category.CategoryLabel.B;
        else if (input.equalsIgnoreCase("C1"))
            label = Category.CategoryLabel.C1;
        else if (input.equalsIgnoreCase("C"))
            label = Category.CategoryLabel.C;
        else if (input.equalsIgnoreCase("D1"))
            label = Category.CategoryLabel.D1;
        else if (input.equalsIgnoreCase("D"))
            label = Category.CategoryLabel.D;
        else if (input.equalsIgnoreCase("BE"))
            label = Category.CategoryLabel.BE;
        else if (input.equalsIgnoreCase("C1E"))
            label = Category.CategoryLabel.C1E;
        else if (input.equalsIgnoreCase("CE"))
            label = Category.CategoryLabel.CE;
        else if (input.equalsIgnoreCase("D1E"))
            label = Category.CategoryLabel.D1E;
        else if (input.equalsIgnoreCase("DE"))
            label = Category.CategoryLabel.DE;
        else if (input.equalsIgnoreCase("T"))
            label = Category.CategoryLabel.T;
        else {
            Log.d("Category","Unknown category");
            return null;
        }
        return label;
    }

    private byte[] calculateHash(byte[] input) {
        MessageDigest md;
        byte[] hash = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            hash = md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
        return hash;
    }

    private void passiveAuthShort() {
        boolean dsCertVerified = verifyDSCert();
        Log.d("dsCertVerified", String.valueOf(dsCertVerified));
        if (dsCertVerified) {
            boolean signatureCorrect = verifySignature();
            Log.d("signatureCorrect", String.valueOf(signatureCorrect));
            if (signatureCorrect) {
                this.parseIdsSecurityObject(idsSecurityObject);
                if (this.PassiveAuthDG6 &&
                        this.PassiveAuthDG15 &&
                        this.PassiveAuthDG16) {
                    this.PassiveAuth = true;
                } else {
                    this.PassiveAuth = false;
                    this.PassiveAuthFailureReason = "Data group hash invalid";
                }
            } else {
                this.PassiveAuth = false;
                this.PassiveAuthFailureReason = "Signature failed verification";
            }
        } else {
            this.PassiveAuth = false;
            this.PassiveAuthFailureReason = "DS Certificate failed verification";
        }
    }

    private void passiveAuth() {
        boolean dsCertVerified = verifyDSCert();
        Log.d("dsCertVerified", String.valueOf(dsCertVerified));
        if (dsCertVerified) {
            boolean signatureCorrect = verifySignature();
            Log.d("signatureCorrect", String.valueOf(signatureCorrect));
            if (signatureCorrect) {
                this.parseIdsSecurityObject(idsSecurityObject);
                if (this.PassiveAuthDG1 &&
                        this.PassiveAuthDG6 &&
                        this.PassiveAuthDG11 &&
                        this.PassiveAuthDG13 &&
                        this.PassiveAuthDG15 &&
                        this.PassiveAuthDG16) {
                    this.PassiveAuth = true;
                } else {
                    this.PassiveAuth = false;
                    this.PassiveAuthFailureReason = "Data group hash invalid";
                }
            } else {
                this.PassiveAuth = false;
                this.PassiveAuthFailureReason = "Signature failed verification";
            }
        } else {
            this.PassiveAuth = false;
            this.PassiveAuthFailureReason = "DS Certificate failed verification";
        }
    }

    private boolean verifyDSCert() {
        // 1. Extract DS certificate from EF.SOd
        // Done in parseEFSOd
        // 2. DS Cert profile complies to CP/CPS
        // TODO check compliance with CP/CPS
        // Check keyUsage
        boolean[] keyUsage = dsCert.getKeyUsage();
        // keyUsage[0] should be true
        // 0 = digital signature
        if (keyUsage[0]) {
            // all others should be false
            for (int i = 1; i<keyUsage.length; i++) {
                if (keyUsage[i])
                    return false;
            }
        } else {
            return false;
        }
        // 3. DS Cert not expired
        try {
            dsCert.checkValidity();
        } catch (CertificateExpiredException e) {
            return false;
        } catch (CertificateNotYetValidException e) {
            return false;
        }
        // TODO 4. DS not present on CRL
        // 4.1 import CRL from file

        // TODO 5. DS Cert issued by CSCA
        Principal principal = dsCert.getSubjectDN();
        String subjectDn = principal.getName();
        Log.d("cert subject", subjectDn);
        principal = dsCert.getIssuerDN();
        String issuerDn = principal.getName();
        Log.d("cert name", issuerDn);
        this.issuerDn = issuerDn;
        if (issuerDn.contains("CSCA GAT NL eDL")) {
            this.isSample = true;
        }
        // 5.1 import CSCA certificate

        // 6. Store DS public key
        // Can be retrieved from dsCert using: this.dsCert.getPublicKey();
        return true;
    }

    private boolean verifySignature() {
        // i. Extract signature and algorithm from EFSOd
        // TODO in parseEFSOd
        // ii. Verify/Decrypt with DS public key

        // iii. Store signedAttrs from signature

        // iv. Extract signedAttrs from EFSOd
        // TODO in parseEFSOd
        // v. compare signedAttrs from iii. and iv.

        // vi. Extract AttributeValue from signedAttrs

        // vii. Extract eContent (=IdsSecurityObject) from EFSOd
        // Done in parseEFSOd
        // viii. Calculate hash voor eContent
        byte[] hashIdsSecurityObject = this.calculateHash(idsSecurityObject);
        // ix. Compare hashes

        return true;
    }

    private void parseIdsSecurityObject(byte[] idsSecurityObject) {
        try {
            ASN1InputStream bIn = new ASN1InputStream(idsSecurityObject);
            ASN1Primitive obj = bIn.readObject();
            ASN1Sequence seq;
            //org.bouncycastle.asn1.DERApplicationSpecific app = (DERApplicationSpecific) obj;
            seq = (ASN1Sequence) obj;

            Enumeration secEnum = seq.getObjects();
            byte dg = 0x00;
            while (secEnum.hasMoreElements()) {
                ASN1Primitive seqObj = (ASN1Primitive) secEnum.nextElement();
                byte[] data = seqObj.getEncoded();
                Log.d("data", ByteUtils.bytesToHex(data));
                if (data[0] == (byte)0x30) {
                    Log.d("start", "30");
                    parseIdsSecurityObject(data);
                } else if (data[0] == (byte)0x02) {
                    Log.d("start", "02");
                    dg = data[2];
                } else if (data[0] == (byte)0x04) {
                    Log.d("start", "04 hash");
                    compareHash(Arrays.copyOfRange(data, 2, data.length), dg);
                } else if (data[0] == (byte)0x06) {
                    Log.d("start", "06");
                    Log.d("OID", ByteUtils.bytesToHex(data));
                } else {
                    Log.d("start", "other");
                    Log.d("other", ByteUtils.bytesToHex(data));
                    parseIdsSecurityObject(data);
                }
            }
            bIn.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void compareHash(byte[] hashFromEFSOd, byte dg) {
        Log.d("dg", Byte.toString(dg));
        switch (dg) {
            case 0x01:	if (Arrays.equals(hashFromEFSOd, hashDG1)) {
                this.PassiveAuthDG1 = true;
            } else {
                this.PassiveAuthDG1 = false;
            }
                Log.d("PassiveAuthDG1", String.valueOf(PassiveAuthDG1));
                break;
            case 0x06:	if (Arrays.equals(hashFromEFSOd, hashDG6)) {
                this.PassiveAuthDG6 = true;
            } else {
                this.PassiveAuthDG6 = false;
            }
                Log.d("PassiveAuthDG6", String.valueOf(PassiveAuthDG6));
                break;
            case 0x0a:	if (Arrays.equals(hashFromEFSOd, hashDG10)) {
                this.PassiveAuthDG10 = true;
            } else {
                this.PassiveAuthDG10 = false;
            }
                Log.d("PassiveAuthDG10", String.valueOf(PassiveAuthDG10));
                break;
            case 0x0b:	if (Arrays.equals(hashFromEFSOd, hashDG11)) {
                this.PassiveAuthDG11 = true;
            } else {
                this.PassiveAuthDG11 = false;
            }
                Log.d("PassiveAuthDG11", String.valueOf(PassiveAuthDG11));
                break;
            case 0x0d:	if (Arrays.equals(hashFromEFSOd, hashDG13)) {
                this.PassiveAuthDG13 = true;
            } else {
                this.PassiveAuthDG13 = false;
            }
                Log.d("PassiveAuthDG13", String.valueOf(PassiveAuthDG13));
                break;
            case 0x0f:	if (Arrays.equals(hashFromEFSOd, hashDG15)) {
                this.PassiveAuthDG15 = true;
            } else {
                this.PassiveAuthDG15 = false;
            }
                Log.d("PassiveAuthDG15", String.valueOf(PassiveAuthDG15));
                break;
            case 0x10:	if (Arrays.equals(hashFromEFSOd, hashDG16)) {
                this.PassiveAuthDG16 = true;
            } else {
                this.PassiveAuthDG16 = false;
            }
                Log.d("PassiveAuthDG16", String.valueOf(PassiveAuthDG16));
                break;
        }
    }

    private void activeAuth(byte[] RNDIFD, byte[] responseToInternalAuth) {
        //Use the AA public key from EF.DG13 (stored in memory) to �decrypt�/�verify� the data from the response to the Internal Authenticate command (without the status words '90 00') with the algorithm from step d. (RSA).

        //This produces a string which according to ISO 9796-2 [15] Digital Signature Scheme 1  should consist of:
        //'6A ++ M1 ++ hash (M1 ++ RND.IFD) ++ 34 CC'
        //where M1 is a nonce generated by the chip.
        //Todo: add all the checks from ISO 9796-2

        try {
            Cipher asymmetricCipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
            asymmetricCipher.init(Cipher.DECRYPT_MODE, this.aaPublicKey);
            byte[] response = asymmetricCipher.doFinal(responseToInternalAuth);
            //byte[] response = asymmetricCipher.doFinal(new byte[260]);
            Log.d("response", ByteUtils.bytesToHex(response));
            // Extract M1 from the response.
            byte[] m1 = Arrays.copyOfRange(response, 1, response.length - 2 - 32);
            Log.d("m1", ByteUtils.bytesToHex(m1));
            // Extract hash from the response
            byte[] hashFromResponse = Arrays.copyOfRange(response, response.length - 2 - 32, response.length - 2);
            //Calculate with sha256 the hash over M1 ++ RND.IFD.
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashM1RNDIFD = md.digest(ByteUtils.concatByteArrays(m1, RNDIFD));
            //Compare this hash with the hash value from the response.
            Log.d("hashFromResponse", ByteUtils.bytesToHex(hashFromResponse));
            Log.d("hashM1RNDIFD", ByteUtils.bytesToHex(hashM1RNDIFD));
            if (Arrays.equals(hashFromResponse, hashM1RNDIFD)) {
                //If the 2 match Active Authentication of the chip is successful.*/
                this.ActiveAuth = true;
                return;
            } else {
                this.ActiveAuth = false;
                this.ActiveAuthFailureReason = "Active Authentication response unexpected";
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchProviderException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            this.ActiveAuthFailureReason = "Could not set up decryption keys for AA";
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            this.ActiveAuthFailureReason = "AA parsing failed";
        }
        this.ActiveAuth = false;
    }

    public void set18(boolean bool) {
        Log.d("18?", bool+"");
        this.is18 = bool;
    }

    public boolean is18() {
        return is18;
    }

    public void set21(boolean bool) {
        Log.d("21?", bool+"");
        this.is21 = bool;
    }

    public boolean is21() {
        return is21;
    }

    public void setAge(int age) {
        Log.d("Age", age+"");
        this.age = age;
    }

    public int getAge() {
        return age;
    }
}
