package com.ul.ts.products.mdlreader.webapi;

import java.util.List;

public class RevokedLicenses {

    private List<RevokedLicense> licenses;
    private byte[] signature;

    public List<RevokedLicense> getLicenses() {
        return licenses;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setLicenses(List<RevokedLicense> licenses) {
        this.licenses = licenses;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

}
