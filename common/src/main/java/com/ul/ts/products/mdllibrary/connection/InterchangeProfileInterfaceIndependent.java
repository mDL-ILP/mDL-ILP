package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class InterchangeProfileInterfaceIndependent extends InterchangeProfile {
    public final static UUID profileUUID = UUID.fromString("8554b533-4b48-4a6b-ae7d-4034b4ac97e6");

    public String version;
    public byte[] sessionID;
    public byte[] nonce;
    public DataMinimizationParameter dataMinimizationParameter;
    public List<AuthenticationProtocol> authenticationProtocols;
    public List<String> languageCodes;

    public InterchangeProfileInterfaceIndependent(TLVData value) throws IOException {
        super(value);
    }

    public InterchangeProfileInterfaceIndependent(final byte[] sessionID, final byte[] nonce, final DataMinimizationParameter dataMinimizationParameter, final List<AuthenticationProtocol> authenticationProtocols, final List<String> languageCodes) throws IOException {
        super(TLVData.EMPTY);
        this.version = "01.01.01";
        this.sessionID = sessionID;
        this.nonce = nonce;
        this.dataMinimizationParameter = dataMinimizationParameter;
        this.authenticationProtocols = authenticationProtocols;
        this.languageCodes = languageCodes;
    }

    @Override
    protected void init() {
        authenticationProtocols = new ArrayList<>();
        languageCodes = new ArrayList<>();
    }

    @Override
    protected void tlvSet(byte tag, byte[] value) throws IOException {
        switch (tag) {
            case (byte) 0x80:
                version = new String(value, "US-ASCII");
                break;
            case (byte) 0x34: // UUID
                break;
            case (byte) 0x81:
                sessionID = value;
                break;
            case (byte) 0x82:
                nonce = value;
                break;
            case (byte) 0x83:
                dataMinimizationParameter = new DataMinimizationParameter(new TLVData(value));
                break;
            case (byte) 0x67:
                authenticationProtocols.add(AuthenticationProtocolFactory.build(new TLVData(value)));
                break;
            case (byte) 0x2D:
                for (int i=0; i < value.length; i += 2) {
                    languageCodes.add(new String(value, i, 2));
                }
                break;
            default:
                throw new UnsupportedTagException(tag, value);
        }
    }

    @Override
    public byte[] toDER() {
        TLVByteBuilder builder = new TLVByteBuilder();
        builder.addTag((byte) 0x80, version);
        builder.addTag((byte) 0x34, profileUUID);
        builder.addTag((byte) 0x81, sessionID);
        builder.addTag((byte) 0x82, nonce);
        builder.addTag((byte) 0x83, dataMinimizationParameter);

        for (AuthenticationProtocol a : authenticationProtocols) {
            builder.addTag((byte) 0x67, a);
        }

        String joined = "";
        for (String lc : languageCodes) {
            joined = joined + lc;
        }

        builder.addTag(new byte[] {0x5F, 0x2D}, joined);
        return builder.getData();
    }


    @Override
    public String toString() {
        return "InterchangeProfileInterfaceIndependent{" +
                "version='" + version + '\'' +
                ", sessionID=" + HexStrings.toHexString(sessionID) +
                ", nonce=" + HexStrings.toHexString(nonce) +
                ", dataMinimizationParameter=" + dataMinimizationParameter +
                ", authenticationProtocols=" + authenticationProtocols +
                ", languageCodes=" + languageCodes +
                '}';
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(sessionID);
        result = 31 * result + Arrays.hashCode(nonce);
        result = 31 * result + (dataMinimizationParameter != null ? dataMinimizationParameter.hashCode() : 0);
        result = 31 * result + (authenticationProtocols != null ? authenticationProtocols.hashCode() : 0);
        result = 31 * result + (languageCodes != null ? languageCodes.hashCode() : 0);
        return result;
    }
}
