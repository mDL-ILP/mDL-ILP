package com.ul.ts.products.mdlholder.webapi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Certificate implements Serializable {

    private static final long serialVersionUID = 99133744542L;

    private byte[] certificateBytes;

    public byte[] getCertificateBytes() {
        return certificateBytes;
    }

    public void setCertificateBytes(byte[] certificateBytes) {
        this.certificateBytes = certificateBytes;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(certificateBytes);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Certificate && Arrays.equals(certificateBytes, ((Certificate) obj).getCertificateBytes());
    }
}
