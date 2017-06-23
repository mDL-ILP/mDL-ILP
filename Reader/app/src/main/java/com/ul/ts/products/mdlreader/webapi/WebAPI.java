package com.ul.ts.products.mdlreader.webapi;

import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Callable;

public class WebAPI {

    //private static final String REMOTE_ADDRESS = "http://192.168.1.100:8080";
    private static final String REMOTE_ADDRESS = "https://rdw-poc-mdl.westeurope.cloudapp.azure.com/RDW.MDL.SV.Backend";

    private class GetRevokedLicensesTask implements Callable<RevokedLicenses> {

        @Override
        public RevokedLicenses call() {
            try {
                final String url = REMOTE_ADDRESS + "/ReaderBackend/GetRevokedLicenses";
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.getForObject(url, RevokedLicenses.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }
    }

    private class GetLicenseTask implements Callable<PartialDrivingLicense> {

        private final String license;

        public GetLicenseTask(String license) {
            this.license = license;
        }

        @Override
        public PartialDrivingLicense call() {
            try {
                final String url = REMOTE_ADDRESS+"/ReaderBackend/GetLicense/"+license;
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.getForObject(url, PartialDrivingLicense.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }
    }

    private class GetCSCACertificatesTask implements Callable<CertificateList> {

        @Override
        public CertificateList call() {
            try {
                final String url = REMOTE_ADDRESS+"/ReaderBackend/GetCSCACertificates";
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.getForObject(url, CertificateList.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }
    }

    private class GetCSCACertificateTask implements Callable<Certificate> {

        private final String certificate;

        public GetCSCACertificateTask(String certificate) {
            this.certificate = certificate;
        }

        @Override
        public Certificate call() {
            try {
                final String url = REMOTE_ADDRESS+"/ReaderBackend/GetCSCACertificate/"+certificate;
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.getForObject(url, Certificate.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }
    }

    private class GetRevocationListTask implements Callable<RevocationList> {

        private final String certificate;

        public GetRevocationListTask(String certificate) {
            this.certificate = certificate;
        }

        @Override
        public RevocationList call() {
            try {
                final String url = REMOTE_ADDRESS+"/ReaderBackend/GetRevocationList/"+certificate;
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                return restTemplate.getForObject(url, RevocationList.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }
    }
}
