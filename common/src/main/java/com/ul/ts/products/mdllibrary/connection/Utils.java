package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

import net.sf.scuba.tlv.TLVInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class Utils {
    public static UUID getUuid(byte[] value) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(value);
        return new UUID(bb.getLong(), bb.getLong());
    }

    public static int intFromBytes(byte[] value) {
        int result = 0;
        for (byte b: value) {
            result <<= 8;
            result += intFromByte(b);
        }
        return result;
    }

    public static byte[] bytesFromInt(int value) {
        String formatted = String.format("%02x", value);
        if (formatted.length() % 2 != 0) {
            formatted = "0" + formatted;
        }
        return HexStrings.fromUnspacedHexString(formatted);
    }

    public static byte[] getValue(TLVData input, byte tag) throws IOException {
        return getValue(input.data, tag);
    }

    public static byte[] getValue(byte[] input, byte tag) throws IOException {
        final TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(input));
        stream.skipToTag(tag);
        stream.readLength();
        return stream.readValue();
    }

    public static int intFromByte(byte value) {
        return ((int) value) & 0xFF;
    }

    public static String makeQR(byte[] data) {
        return org.bouncycastle.util.encoders.Base64.toBase64String(data);
    }

    public static byte[] readQR(String data)
    {
        return org.bouncycastle.util.encoders.Base64.decode(data);
    }
}
