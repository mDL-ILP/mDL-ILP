package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

import java.io.IOException;
import java.util.Arrays;

public class AuthenticationProtocolPACE extends AuthenticationProtocol {
    public static final ObjectIdentifier oid = new ObjectIdentifier("0.4.0.127.0.7.2.2.4");
    public byte[] pacePassword;

    public AuthenticationProtocolPACE(TLVData value) throws IOException {
        super(value);
    }

    public AuthenticationProtocolPACE(final byte[] pacePassword) throws IOException {
        super(TLVData.EMPTY);
        this.pacePassword = pacePassword;
    }

    @Override
    protected void tlvSet(byte tag, byte[] value) throws IOException {
        switch(tag) {
            case (byte) 0x06:
                break;
            case (byte) 0x80:
                pacePassword = value;
                break;
            default:
                throw new UnsupportedTagException(tag, value);
        }
    }

    @Override
    public byte[] toDER() {
        TLVByteBuilder builder = new TLVByteBuilder();
        builder.addTag((byte) 0x06, oid.toByteArray());
        builder.addTag((byte) 0x80, pacePassword);
        return builder.getData();
    }

    @Override
    public String toString() {
        return "AuthenticationProtocolPACE{" +
                "pacePassword=" + HexStrings.toHexString(pacePassword) +
                '}';
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pacePassword);
    }
}
