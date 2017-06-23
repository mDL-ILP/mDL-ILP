package com.ul.ts.products.mdlholder.security;

import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class ECKABasicAgreement implements BasicAgreement {

    private ECPrivateKeyParameters key;

    public void init(CipherParameters key) {
        this.key = (ECPrivateKeyParameters) key;
    }

    public int getFieldSize() {
        return (key.getParameters().getCurve().getFieldSize() + 7) / 8;
    }

    public BigInteger calculateAgreement(CipherParameters pubKey) {
        ECPoint P = calculatePoint(pubKey);
        return P.getAffineXCoord().toBigInteger();
    }

    public ECPoint calculatePoint(CipherParameters pubKey) {
        ECPublicKeyParameters pub = (ECPublicKeyParameters) pubKey;
        ECPoint P = pub.getQ().multiply(key.getD()).normalize();

        if (P.isInfinity()) {
            throw new IllegalStateException("Infinity is not a valid agreement value for ECDH");
        }
        return P;
    }
}
