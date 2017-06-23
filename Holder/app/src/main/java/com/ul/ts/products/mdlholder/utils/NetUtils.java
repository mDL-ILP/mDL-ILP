package com.ul.ts.products.mdlholder.utils;

import android.content.Context;
import android.util.Log;

import com.ul.ts.products.mdlholder.R;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class NetUtils {

    public static void setUpSSL(Context context) {
        // set up keystore
        try (InputStream clientInput = context.getResources().openRawResource(R.raw.rdw_poc_mdl_client_ca);
             //InputStream serverInput = context.getResources().openRawResource(R.raw.rdw_poc_ca)) {
             InputStream serverInput = context.getResources().openRawResource(R.raw.rdw_poc_ssl)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //java.security.cert.Certificate clientCA = cf.generateCertificate(clientInput);

            // This part sends my cert to server
//            KeyStore clientStore = KeyStore.getInstance("PKCS12");
//            clientStore.load(clientInput, "password".toCharArray());
            //clientStore.setCertificateEntry("", clientCA);

//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            keyManagerFactory.init(clientStore, null);

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
