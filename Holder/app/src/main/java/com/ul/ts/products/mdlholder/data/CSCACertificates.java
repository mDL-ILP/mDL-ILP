package com.ul.ts.products.mdlholder.data;

import android.content.Context;

import com.ul.ts.products.mdlholder.R;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CSCACertificates {

    private Context context;

    public CSCACertificates(Context context) {
        this.context = context;
    }

    public X509Certificate findCertificate(String issuerDn) throws java.security.cert.CertificateException{
        issuerDn = issuerDn.toLowerCase().replace('-','_');
        InputStream serverInput = context.getResources().openRawResource(context.getResources().getIdentifier(issuerDn,"raw",context.getPackageName()));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate)cf.generateCertificate(serverInput);
    }
}
