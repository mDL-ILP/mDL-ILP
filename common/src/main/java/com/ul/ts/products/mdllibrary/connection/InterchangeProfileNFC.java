package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

import java.io.IOException;
import java.util.UUID;

public class InterchangeProfileNFC extends InterchangeProfile {
    public final static UUID profileUUID = UUID.fromString("b75ce6f4-98f5-438b-be02-5927a010fde9");
    public String version;
    public int maxApduLength;

    public InterchangeProfileNFC(TLVData value) throws IOException {
        super(value);
        System.out.println(maxApduLength);
    }

    public InterchangeProfileNFC(final int maxApduLength) throws IOException {
        super(TLVData.EMPTY);
        this.version = "01.01.01";
        this.maxApduLength = maxApduLength;
    }

    @Override
    protected void tlvSet(byte tag, byte[] value) throws IOException {
        System.out.println(HexStrings.toHexString(tag) + ": " + HexStrings.toHexString(value));
        switch(tag) {
            case (byte) 0x80:
                version = new String(value);
                break;
            case (byte) 0x34:
                break;
            case (byte) 0x81:
                maxApduLength = Utils.intFromBytes(value);
                System.out.println(maxApduLength);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public byte[] toDER() {
        TLVByteBuilder builder = new TLVByteBuilder();
        builder.addTag((byte) 0x80, version);
        builder.addTag((byte) 0x34, profileUUID);
        if (maxApduLength > 0) {
            builder.addTag((byte) 0x81, Utils.bytesFromInt(maxApduLength));
        }
        return builder.getData();
    }
    @Override
    public String toString() {
        return "InterchangeProfileNFC{" +
                "version='" + version + '\'' +
                ", maxApduLength=" + maxApduLength +
                '}';
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + maxApduLength;
        return result;
    }
}
