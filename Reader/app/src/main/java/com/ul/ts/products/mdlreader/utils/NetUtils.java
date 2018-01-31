package com.ul.ts.products.mdlreader.utils;

import android.content.Context;
import android.util.Log;

import com.ul.ts.products.mdlreader.R;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class NetUtils {

    public static void setUpSSL(Context context) {
        // set up keystore
        try (

             InputStream serverInput = context.getResources().openRawResource(R.raw.rdw_poc_ssl)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");


            // this part trusts a remote certificate
            java.security.cert.Certificate serverCA = cf.generateCertificate(serverInput);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore serverStore = KeyStore.getInstance("PKCS12");
            serverStore.load(null, null);
            serverStore.setCertificateEntry("", serverCA);
            tmf.init(serverStore);

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tmf.getTrustManagers(), null);

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
            Log.e("TLS", "Something went wrong", e);
        }
    }
}
