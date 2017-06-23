package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

import java.io.IOException;
import java.util.UUID;

public class InterchangeProfileWD extends InterchangeProfile {
    public final static UUID profileUUID = UUID.fromString("9072f42e-13f1-4d0e-84e6-bf56c321cabb");
    public String version;
    public String MAC;

    public InterchangeProfileWD(TLVData value) throws IOException {
        super(value);
    }

    public InterchangeProfileWD(final String MAC) throws IOException {
        super(TLVData.EMPTY);
        this.version = "01.01.01";
        this.MAC = MAC;
    }

    @Override
    protected void tlvSet(byte tag, byte[] value) throws IOException {
        switch(tag) {
            case (byte) 0x80:
                version = new String(value);
                break;
            case (byte) 0x34:
                break;
            case (byte) 0x81:
                StringBuilder sb = new StringBuilder();
                for (byte b : value) {
                    sb.append(HexStrings.toHexString(b));
                    sb.append(":");
                }
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }
                MAC = sb.toString();
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public byte[] toDER() {
        TLVByteBuilder builder = new TLVByteBuilder();
        builder.addTag((byte) 0x80, version.getBytes());
        builder.addTag((byte) 0x34, profileUUID);
        builder.addTag((byte) 0x81, HexStrings.fromUnspacedHexString(MAC.replace(":", "")));
        return builder.getData();
    }

    @Override
    public String toString() {
        return "InterchangeProfileWD{" +
                "version='" + version + '\'' +
                ", MAC='" + MAC + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (MAC != null ? MAC.hashCode() : 0);
        return result;
    }
}
