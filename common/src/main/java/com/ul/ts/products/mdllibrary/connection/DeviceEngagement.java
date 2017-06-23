package com.ul.ts.products.mdllibrary.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceEngagement extends TLVObject {
    protected List<InterchangeProfile> interchangeProfiles;

    public DeviceEngagement(TLVData value) throws IOException {
        super(value);
    }

    public DeviceEngagement(final List<InterchangeProfile> interchangeProfiles) throws IOException {
        super(TLVData.EMPTY);
        this.interchangeProfiles = interchangeProfiles;
    }

    public InterchangeProfileInterfaceIndependent getInterfaceIndependentProfile() {
        for (InterchangeProfile ip : interchangeProfiles) {
            if (ip instanceof InterchangeProfileInterfaceIndependent) {
                return (InterchangeProfileInterfaceIndependent) ip;
            }
        }
        return null;
    }

    public InterchangeProfile getTransferInterchangeProfile() {
        for (InterchangeProfile ip : interchangeProfiles) {
            if (!(ip instanceof InterchangeProfileInterfaceIndependent)) {
                return ip;
            }
        }
        return null;
    }

    @Override
    protected void init() {
        interchangeProfiles = new ArrayList<>();
    }

    @Override
    protected void tlvSet(byte tag, byte[] value) throws IOException {
        switch(tag) {
            case 0x29:
                this.interchangeProfiles.add(InterchangeProfileFactory.build(new TLVData(value)));
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public byte[] toDER() {
        TLVByteBuilder builder = new TLVByteBuilder();
        for(InterchangeProfile ip: interchangeProfiles) {
            builder.addTag(new byte[] {0x5F, 0x29}, ip.toDER());
        }
        return builder.getData();
    }

    public byte[] encapsulated() {
        TLVByteBuilder builder = new TLVByteBuilder();
        builder.addTag((byte) 0x6E, this.toDER());
        return builder.getData();
    }

    public DeviceEngagement fromEncapsulated(byte[] encapsulated) throws IOException {
        return new DeviceEngagement(new TLVData(Utils.getValue(encapsulated, (byte) 0x6E)));
    }

    @Override
    public String toString() {
        return "DeviceEngagement{" +
                "interchangeProfiles=" + interchangeProfiles +
                '}';
    }

    @Override
    public int hashCode() {
        return interchangeProfiles.hashCode();
    }
}
