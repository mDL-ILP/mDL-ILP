package com.ul.ts.products.mdlholder.utils;

import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CardIOUtils {
    static final byte[] ICV_ZERO = new byte[8];
    static final byte[] TEST_K_IFD = new byte[] {
            (byte) 0xC8, (byte) 0x8A, 0x23, 0x1C, (byte) 0xE6, (byte) 0xFE, (byte) 0xAB, 0x73,
            (byte) 0xD3, (byte) 0xDA, (byte) 0xE0, (byte) 0x85, (byte) 0xC1, (byte) 0x8A, 0x57, (byte) 0xB0};
    static final byte[] TEST_RND_IFD = new byte[] {
            (byte) 0xC8, (byte) 0xC0, (byte) 0xE7, 0x2C, (byte) 0xFF, 0x5B, (byte) 0xA6, 0x74};

    public static byte[] stripSW(byte[] resp) {
        return Arrays.copyOfRange(resp, 0, resp.length-2);
    }

    public static byte[] getSW(byte[] resp) {
        return Arrays.copyOfRange(resp, resp.length-2, resp.length);
    }

    public static IvParameterSpec getIcv(byte[] icv) {
        return new IvParameterSpec(pad(icv));
    }

    public static byte[] calculateKSessionEnc(byte[] kSessionSeed) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            // KSessionEnc = first 16 bytes of the SHA-1 hash over the concatenation of KSession_Seed ++ �00 00 00 01�
            byte[] unadjustedParityKenc = Arrays.copyOfRange(md.digest(Bytes.concatenate(kSessionSeed, new byte[] {0x00, 0x00, 0x00, 0x01})), 0, 16);
            byte[] kenc = adjustParityBits(unadjustedParityKenc);
            return kenc;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] calculateKSessionMac(byte[] kSessionSeed) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            // KSession_MAC  = the first 16 bytes of the SHA-1 hash over the concatenation of KSession_Seed ++ �00 00 00 02� with the parity bits adjusted
            byte[] unadjustedParityKmac = Arrays.copyOfRange(md.digest(Bytes.concatenate(kSessionSeed, new byte[] {0x00, 0x00, 0x00, 0x02})), 0, 16);
            byte[] kmac = adjustParityBits(unadjustedParityKmac);
            return kmac;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] adjustParityBits(byte[] unadjusted) {
        for (int i = 0; i < unadjusted.length; i++)
        {
            //for (int b)
            //if (unadjusted[i]
        }
        return unadjusted;
    }

    public static byte[] generateKifd() {
        byte[] keyIfd = TEST_K_IFD;
        //byte[] keyIfd = new byte[] {0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01}; // TODO randomize, fix parity: Kifd    = ExpandKey(CompressKey(AlignRight(Random(16),'00'{16})));
        return keyIfd;
    }

    public static byte[] generateRNDifd() {
        // TODO randomize
        byte[] rndIfd = TEST_RND_IFD;
        return rndIfd;
    }

    public static byte[] calculateSSC(byte[] rndifd, byte[] rndicc) {
        Log.d("rndifd", HexStrings.toHexString(rndifd));
        Log.d("rndicc", HexStrings.toHexString(rndicc));
        byte[] last4BytesOfRndifd = Arrays.copyOfRange(rndifd, 4, 8);
        byte[] last4BytesOfRndicc = Arrays.copyOfRange(rndicc, 4, 8);
        byte[] ssc = Bytes.concatenate(last4BytesOfRndicc, last4BytesOfRndifd);
        Log.d("SSC", HexStrings.toHexString(ssc));
        return ssc;
    }

    public static byte[] encrypt(byte[] toBeEncrypted, Key kenc, IvParameterSpec iv) {
        Cipher enc;
        try {
            enc = Cipher.getInstance("DESede/CBC/Nopadding");
            enc.init(Cipher.ENCRYPT_MODE, kenc, iv);
            return enc.doFinal(toBeEncrypted);

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] pad(byte[] dataBytes) {
        if ((dataBytes.length % 8) != 0) {
            int len = (dataBytes.length / 8) * 8;
            len += 8;
            byte[] result = new byte[len];
            System.arraycopy(dataBytes, 0, result, 0, dataBytes.length);
            return result;
        } else {
            return dataBytes;
        }
    }

    public static Key getKey24(byte[] key) {
        return new SecretKeySpec(getKeyBytes24(key), "DESede");
    }

    public static byte[] getKeyBytes24(byte[] keyBytes) {
        if (keyBytes.length == 8) {
            byte[] k = new byte[24];
            System.arraycopy(keyBytes, 0, k, 0, 8);
            System.arraycopy(keyBytes, 0, k, 8, 8);
            System.arraycopy(keyBytes, 0, k, 16, 8);
            return k;
        } else if (keyBytes.length == 16) {
            byte[] k = new byte[24];
            System.arraycopy(keyBytes, 0, k, 0, 16);
            System.arraycopy(keyBytes, 0, k, 16, 8);
            return k;
        } else if (keyBytes.length == 24) {
            return keyBytes;
        } else {
            return null;
        }
    }

    public static byte[] rMac(byte[] icv, byte[] data, byte[] keyBytes) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("DES/CBC/NoPadding");
            Key k1 = new SecretKeySpec(keyBytes, 0, 8, "DES");
            Key k2 = new SecretKeySpec(keyBytes, 8, 8, "DES");
            Key k3 = new SecretKeySpec(keyBytes, 16, 8, "DES");
            cipher.init(Cipher.ENCRYPT_MODE, k1, getIcv(icv));
            byte[] result = cipher.doFinal(pad(data));
            byte[] mac = new byte[8];
            System.arraycopy(result, result.length - 8, mac, 0, 8);
            cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, k2);
            mac = cipher.doFinal(mac);
            cipher.init(Cipher.ENCRYPT_MODE, k3);
            mac = cipher.doFinal(mac);
            return mac;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] pad80(byte[] dataBytes) {
        int length = dataBytes.length + 1;
        if (length % 8 != 0) {
            length = ((length / 8) + 1) * 8;
        }
        byte[] result = new byte[length];
        System.arraycopy(dataBytes, 0, result, 0, dataBytes.length);
        result[dataBytes.length] = (byte) 0x80;
        return result;
    }

    public static byte[] decrypt(byte[] toBeDecrypted, Key kenc, IvParameterSpec iv) {
        Cipher enc;
        try {
            enc = Cipher.getInstance("DESede/CBC/Nopadding");
            enc.init(Cipher.DECRYPT_MODE, kenc, iv);
            return enc.doFinal(toBeDecrypted);

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] calculateKSessionSeed(byte[] kicc, byte[] kifd) {
        byte[] ksessionseed = new byte[16];
        for (int n = 0; n < 16; n++)
        {
            ksessionseed[n] = (byte)(kifd[n] ^ kicc[n]);
        }
        return ksessionseed;
    }

    public static byte[] srmac(byte[] data, Key srmack1, Key srmack2) {
        IvParameterSpec iv = getIcv(ICV_ZERO);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("DES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, srmack1, iv);
            byte[] result = cipher.doFinal(pad80(data));
            byte[] mac = new byte[8];
            System.arraycopy(result, result.length - 8, mac, 0, 8);
            cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, srmack2);
            mac = cipher.doFinal(mac);
            cipher.init(Cipher.ENCRYPT_MODE, srmack1);
            mac = cipher.doFinal(mac);
            return mac;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptResponse(byte[] response, Cipher sdec, byte[] SSC, Key srmack1, Key srmack2) {
        //bac = false;
        byte[] data = new byte[0];
        int dataLength = 0;
        int swOffset = 0;
        int offset = 0;
        if (response[offset] == (byte) 0x87) {
            // encrypted data
            offset++;
            int len;
            if (response[offset] >= 0) {
                len = response[offset];
            } else if (response[offset] == (byte) 0x81) {
                offset++;
                len = response[offset] & 0xff;
            } else {
                len = 0;
                Log.d("decryptResponse","Unsupported length encoding");
            }
            offset++;
            if (response[offset] != 1) {
                len = 0;
                Log.d("decryptResponse","Unsupported padding indicator");
            }
            offset++;
            len--;
            try {
                data = sdec.doFinal(response, offset, len);
                dataLength = data.length;
                while (dataLength > 0 && data[dataLength - 1] == 0) {
                    dataLength--;
                }
                if (dataLength > 0 && data[dataLength - 1] == (byte) 0x80) {
                    dataLength--;
                } else {
                    Log.d("decryptResponse","Uncorrect data padding");
                }
                //breakPoint(Utils.base16Encode(data));
                offset += len;
            } catch (IllegalBlockSizeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (BadPaddingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (response[offset] == (byte) 0x99) {
            // sw
            offset++;
            if (response[offset] == (byte) 2) {
                offset++;
                swOffset = offset;
                offset += 2;
            } else {
                Log.d("decryptResponse","Unexpected sw length");
            }
        } else {
            Log.d("decryptResponse","Missing sw");
        }
        if (response[offset] == (byte) 0x8E) {
            // mac
            offset++;
            if (response[offset] == (byte) 8) {
                byte[] mac = srmac(Bytes.concatenate(SSC, Arrays.copyOfRange(response, 0, offset - 1)), srmack1, srmack2);
                for (int i = 0; i < 8; i++) {
                    if (response[++offset] != mac[i]) {
                        //Log.d("decryptResponse","Invalid mac");
                    }
                }
            } else {
                Log.d("decryptResponse","Unexpected mac length");
            }
        } else {
            Log.d("decryptResponse","Missing mac");
        }
        byte[] result = new byte[dataLength + 2];
        System.arraycopy(data, 0, result, 0, dataLength);
        System.arraycopy(response, swOffset, result, dataLength, 2);
        //bac = true;
        Log.d("responselength", Integer.toString(result.length));
        return result;
    }

    public static byte[] encryptCommand(byte cla, byte ins, byte p1, byte p2, byte le, byte[] SSC, Key srmack1, Key srmack2) {
        //Log.d("unencryptedCommand", Utils.bytesToHex(Utils.concatBytes(cla, ins, p1, p2, le)));
        byte[] securedData = new byte[]{(byte) 0x97, 0x01, le};

        byte[] macInput = Bytes.concatenate(SSC, new byte[]{0x0c, ins, p1, p2, (byte) 0x80, 0x00, 0x00, 0x00});
        macInput = Bytes.concatenate(macInput, securedData);

        byte[] mac = srmac(macInput, srmack1, srmack2);

        byte lc = (byte) (securedData.length + 2 + mac.length);

        byte[] result = Bytes.concatenate(new byte[]{0x0c, ins, p1, p2, lc}, securedData, new byte[]{(byte) 0x8e, 0x08}, mac, new byte[1]);
        //Log.d("encryptedCommand", Utils.bytesToHex(result));
        return result;
    }

    public static byte[] encryptCommand(byte cla, byte ins, byte p1, byte p2, byte[] data, byte le, byte[] SSC, Key srmack1, Key srmack2, Cipher senc) {
        //Log.d("unencryptedCommand", Utils.bytesToHex(Utils.concatBytes(cla, ins, p1, p2, le)));
        try {
            byte[] encInput = pad80(data);
            byte[] securedData = senc.doFinal(encInput);
            securedData = Bytes.concatenate(new byte[]{(byte) 0x87, (byte) (securedData.length + 1), 0x01}, securedData, new byte[]{(byte) 0x97, 0x01, le});

            byte[] macInput = Bytes.concatenate(SSC, new byte[]{0x0c, ins, p1, p2, (byte) 0x80, 0x00, 0x00, 0x00});
            macInput = Bytes.concatenate(macInput, securedData);

            byte[] mac = srmac(macInput, srmack1, srmack2);

            byte lc = (byte) (securedData.length + 2 + mac.length);

            byte[] result = Bytes.concatenate(new byte[]{0x0c, ins, p1, p2, lc}, securedData, new byte[]{(byte) 0x8e, 0x08}, mac, new byte[1]);
            Log.d(CardIOUtils.class.getSimpleName(), "encryptedCommand with data "  + HexStrings.toHexString(result));
            return result;
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] increaseSSC(byte[] SSC) {
        for (int i = 7; i >= 0; i--) {
            if (++SSC[i] != 0) break;
        }
        return SSC;
    }

    public static byte[] generateRNDforAA() {
        // TODO randomize
        byte[] rndIfd = TEST_RND_IFD;
        return rndIfd;
    }
}
