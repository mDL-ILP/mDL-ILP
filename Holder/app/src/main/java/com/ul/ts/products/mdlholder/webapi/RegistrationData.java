package com.ul.ts.products.mdlholder.webapi;

public class RegistrationData {

    private String deviceToken;
    private String deviceDescription;
    private byte[] publicKey;

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getDeviceDescription() {
        return deviceDescription;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
