package com.ul.ts.products.mdlreader.webapi;

public class RevokedLicense {

    private byte[] aapublicKeyHash;
    private byte[] signature;

    public byte[] getAapublicKeyHash() {
        return aapublicKeyHash;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setAapublicKeyHash(byte[] aapublicKeyHash) {
        this.aapublicKeyHash = aapublicKeyHash;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}
