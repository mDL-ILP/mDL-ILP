package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

import java.util.ArrayList;
import java.util.UUID;

public class TLVByteBuilder {
    final private ArrayList<Byte> data;

    public TLVByteBuilder() {
        data = new ArrayList<>();
    }

    public void addTag(byte[] tag, byte[] value) {
        for (byte b: tag) {
            data.add(b);
        }
        for (byte b: getLengthBlock(value.length)) {
            data.add(b);
        }
        for (byte b: value) {
            data.add(b);
        }
    }

    public void addTag(byte tag, byte[] value) {
        addTag(new byte[] {tag}, value);
    }

    public void addTag(byte tag, String value) {
        addTag(new byte[] {tag}, value.getBytes());
    }
    public void addTag(byte[] tag, String value) {
        addTag(tag, value.getBytes());
    }
    public void addTag(byte tag, UUID value) {
        addTag(new byte[] {tag}, HexStrings.fromUnspacedHexString(value.toString().replace("-", "")));
    }

    public void addTag(byte tag, ToDER value) {
        addTag(new byte[] {tag}, value.toDER());
    }

    public void addTag(byte[] tag, ToDER value) {
        addTag(tag, value.toDER());
    }


    public byte[] getData() {
        byte[] result = new byte[this.data.size()];
        int i=0;
        for (byte b: this.data) {
            result[i] = b;
            i++;
        }
        return result;
    }

    private byte[] getLengthBlock(int length) {
        String formatted_length = String.format("%02x", length);
        if (formatted_length.length() % 2 != 0) {
            formatted_length = "0" + formatted_length;
        }

        if (length > 127) {
            String length_byte = String.format("%02x", (formatted_length.length() / 2) | 0x80);
            formatted_length = length_byte + formatted_length;
        }

        return HexStrings.fromUnspacedHexString(formatted_length);
    }
}
