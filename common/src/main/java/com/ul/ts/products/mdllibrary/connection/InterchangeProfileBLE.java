package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class InterchangeProfileBLE extends InterchangeProfile {
    public final static UUID profileUUID = UUID.fromString("8126c48f-50d0-4172-852e-47042091de14");
    public String version;
    public String MAC;
    public byte[] antiCollisionIdentifier;
    public String deviceName;
    public UUID protocolID;
    public int maxMTU = 0;

    public InterchangeProfileBLE(TLVData value) throws IOException {
        super(value);
    }

    public InterchangeProfileBLE(final String MAC, final byte[] antiCollisionIdentifier, final String deviceName, final int maxMTU) throws IOException {
        super(TLVData.EMPTY);
        this.version = "01.01.01";
        this.MAC = MAC;
        this.antiCollisionIdentifier = antiCollisionIdentifier;
        this.deviceName = deviceName;
        this.protocolID = new UUID(0,0);
        this.maxMTU = maxMTU;
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
            case (byte) 0x82:
                antiCollisionIdentifier = value;
                break;
            case (byte) 0x83:
                deviceName = new String(value);
                break;
            case (byte) 0x84:
                protocolID = Utils.getUuid(value);
                break;
            case (byte) 0x85:
                maxMTU = ((int) value[0]) & 0xFF;
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
        builder.addTag((byte) 0x81, HexStrings.fromUnspacedHexString(MAC.replace(":", "")));
        builder.addTag((byte) 0x82, antiCollisionIdentifier);
        builder.addTag((byte) 0x83, deviceName);
        builder.addTag((byte) 0x84, protocolID);
        if (maxMTU > 0) {
            builder.addTag((byte) 0x85, new byte[]{(byte) maxMTU});
        }
        return builder.getData();
    }

    @Override
    public String toString() {
        return "InterchangeProfileBLE{" +
                "version='" + version + '\'' +
                ", MAC='" + MAC + '\'' +
                ", antiCollisionIdentifier=" + HexStrings.toHexString(antiCollisionIdentifier) +
                ", deviceName='" + deviceName + '\'' +
                ", protocolID=" + protocolID +
                ", maxMTU=" + maxMTU +
                '}';
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (MAC != null ? MAC.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(antiCollisionIdentifier);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (protocolID != null ? protocolID.hashCode() : 0);
        result = 31 * result + maxMTU;
        return result;
    }
}
