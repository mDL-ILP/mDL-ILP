package com.ul.ts.products.mdllibrary.connection;

import java.io.IOException;
import java.util.UUID;

public class InterchangeProfileOnline extends InterchangeProfile {
    public final static UUID profileUUID = UUID.fromString("92e3f66d-c70f-423e-8d11-c2b0658b9a1d");
    public String version;
    public String URL;

    public InterchangeProfileOnline(TLVData value) throws IOException {
        super(value);
    }

    public InterchangeProfileOnline(final String URL) throws IOException {
        super(TLVData.EMPTY);
        this.version = "01.01.01";
        this.URL = URL;
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
                URL = new String(value);
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
        builder.addTag((byte) 0x81, URL);
        return builder.getData();
    }

    @Override
    public String toString() {
        return "InterchangeProfileOnline{" +
                "version='" + version + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result;
        return result;
    }
}
