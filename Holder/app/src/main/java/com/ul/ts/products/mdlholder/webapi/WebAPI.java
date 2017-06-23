package com.ul.ts.products.mdlholder.webapi;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.ul.ts.products.mdlholder.R;
import com.ul.ts.products.mdlholder.utils.Bytes;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.security.auth.x500.X500Principal;

public class WebAPI {

    //private static final String REMOTE_ADDRESS = "http://192.168.1.100:8080";
    private static final String REMOTE_ADDRESS = "https://rdw-poc-mdl.westeurope.cloudapp.azure.com/RDW.MDL.SV.Backend";

    public static class RegisterTask implements Callable<Certificate> {

        private final Context context;

        public RegisterTask(Context context) {
            this.context = context;
        }

        @Override
        public Certificate call() {
            try {
                final String url = REMOTE_ADDRESS+"/Register";
                RestTemplate restTemplate = new RestTemplate();

                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");

                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 10);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(context.getString(R.string.key_key))
                        .setKeySize(2048)
                        .setSubject(new X500Principal(""))
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .setSerialNumber(BigInteger.ONE)
                        .build();

                kpg.initialize(spec);
                KeyPair keyPair = kpg.genKeyPair();
                byte[] pub = keyPair.getPublic().getEncoded();

                RegistrationData device = new RegistrationData();
                device.setDeviceToken(FirebaseInstanceId.getInstance().getToken());
                device.setDeviceDescription(getDeviceName());
                device.setPublicKey(pub);

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                Log.d(getClass().getName(), "Public key is: "+ Bytes.hexString(pub));

                return restTemplate.postForObject(url, device, Certificate.class);
            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return null;
        }

        public static String getDeviceName() {
            final String manufacturer = Build.MANUFACTURER, model = Build.MODEL;
            return model.startsWith(manufacturer) ? capitalizePhrase(model) : capitalizePhrase(manufacturer) + " " + model;
        }

        private static String capitalizePhrase(String s) {
            if (s == null || s.length() == 0)
                return s;
            else {
                StringBuilder phrase = new StringBuilder();
                boolean next = true;
                for (char c : s.toCharArray()) {
                    if (next && Character.isLetter(c) || Character.isWhitespace(c))
                        next = Character.isWhitespace(c = Character.toUpperCase(c));
                    phrase.append(c);
                }
                return phrase.toString();
            }
        }
    }

    private static class DownloadLicenseTask implements Callable<MobileDrivingLicense> {

        @Override
        public MobileDrivingLicense call() {
            try {
                final String url = REMOTE_ADDRESS+"/Download";
                RestTemplate restTemplate = new RestTemplate();

                Device device = new Device();
                device.setToken(FirebaseInstanceId.getInstance().getToken());

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                HttpEntity<Device> entity = new HttpEntity<>(device);

                try {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, MobileDrivingLicense.class);
                    return (MobileDrivingLicense) response.getBody();
                } catch (HttpClientErrorException e) {
                    HttpStatus status = e.getStatusCode();

                    if (status == HttpStatus.NOT_FOUND) { // Returns HTTP code 404 if the device token or license is not found.
                        Log.d(getClass().getName(), "The device token or license is not found.");
                    }
                    else if (status == HttpStatus.FORBIDDEN) { // Returns HTTP code 403 if the certificate is not found or the thumbprint does not match.
                        Log.d(getClass().getName(), "The certificate is not found or the thumbprint does not match.");
                    }
                    else {
                        Log.d(getClass().getName(), "Something went wrong fetching the driving licence data. HTTP status code: "+status.value());
                    }

                }
            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return null;
        }
    }

    public static class DownloadLicenseDataTask extends AsyncTask<Void, Void, MobileDrivingLicense> {

        private final ProgressDialog progressDialog;
        private final Context context;
        private final ExecutorService service = Executors.newSingleThreadExecutor();

        public DownloadLicenseDataTask(Context context) {
            progressDialog = new ProgressDialog(context);
            this.context = context;
            Log.d(getClass().getName(), "Context type: "+context.getClass().getCanonicalName());
        }

        @Override
        protected void onPreExecute() {
            // This function can be called with getApplicationContext() which cannot be used for showing a progress dialog
            if (!(context instanceof android.app.Application)) {
                progressDialog.setTitle(context.getString(R.string.perso_title));
                progressDialog.setMessage(context.getString(R.string.perso_downloading));
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
            }

            super.onPreExecute();
        }

        @Override
        protected MobileDrivingLicense doInBackground(Void... voids) {

            Callable<MobileDrivingLicense> registerTask = new WebAPI.DownloadLicenseTask();
            Future<MobileDrivingLicense> f = service.submit(registerTask);

            MobileDrivingLicense license = null;
            try {
                license = f.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return license;
        }

        @Override
        protected void onPostExecute(MobileDrivingLicense license) {
            progressDialog.dismiss();

            if (license != null) {
                license.store(context);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.perso_problem_title);
                builder.setMessage(R.string.settings_problem_download);
                builder.setPositiveButton("OK", null);
                builder.create().show();
            }
        }
    }

    public static class RequestTransferIDTask implements Callable<TransferID> {

        @Override
        public TransferID call() {
            try {
                final String url = REMOTE_ADDRESS+"/RequestTransferID";
                RestTemplate restTemplate = new RestTemplate();

                Device device = new Device();
                device.setToken(FirebaseInstanceId.getInstance().getToken());

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                HttpEntity<Device> entity = new HttpEntity<>(device);

                try {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, TransferID.class);
                    return (TransferID)response.getBody();
                } catch (HttpClientErrorException e) {
                    HttpStatus status = e.getStatusCode();

                    if (status == HttpStatus.FORBIDDEN) { // Returns HTTP code 403 if the certificate is not found or the thumbprint does not match.
                        Log.d(getClass().getName(), "The certificate is not found or the thumbprint does not match.");
                    }
                    else if (status == HttpStatus.NOT_FOUND) { // Returns HTTP code 404 if the device token or MDL is not found.
                        Log.d(getClass().getName(), "The device token or MDL is not found.");
                    }
                    else {
                        Log.d(getClass().getName(), "Something went wrong requesting the transfer ID. HTTP status code: "+status.value());
                    }
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return null;
        }
    }

    public static class ShouldRevokeTask implements Callable<BooleanValue> {

        @Override
        public BooleanValue call() {
            try {
                final String url = REMOTE_ADDRESS+"/ShouldRevoke";
                RestTemplate restTemplate = new RestTemplate();

                Device device = new Device();
                device.setToken(FirebaseInstanceId.getInstance().getToken());

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                HttpEntity<Device> entity = new HttpEntity<>(device);

                try {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, BooleanValue.class);
                    return (BooleanValue)response.getBody();
                } catch (HttpClientErrorException e) {
                    HttpStatus status = e.getStatusCode();

                    if (status == HttpStatus.NOT_FOUND) { // Returns HTTP code 404 if the device token or license is not found.
                        Log.d(getClass().getName(), "The device token or license is not found.");
                    }
                    else {
                        Log.d(getClass().getName(), "Something went wrong querying the revoke status. HTTP status code: "+status.value());
                    }
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return null;
        }
    }

    public static class PermitTransferTask implements Callable<Boolean> {

        private final String transferId;

        public PermitTransferTask(String transferId) {
            this.transferId = transferId;
        }

        @Override
        public Boolean call() {
            try {
                final String url = REMOTE_ADDRESS+"/PermitTransfer";
                RestTemplate restTemplate = new RestTemplate();

                TransferData data = new TransferData();
                data.setTransferID(transferId);
                data.setDeviceToken(FirebaseInstanceId.getInstance().getToken());

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                HttpEntity<TransferData> entity = new HttpEntity<>(data);

                try {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        return true;
                    }
                } catch (HttpClientErrorException e) {
                    HttpStatus status = e.getStatusCode();

                    if (status == HttpStatus.FORBIDDEN) { // Returns HTTP code 403 if the certificate is not found or the thumbprint does not match.
                        Log.d(getClass().getName(), "The certificate is not found or the thumbprint does not match.");
                    }
                    else if (status == HttpStatus.NOT_FOUND) { // Returns HTTP code 404 if the device token or transfer is not found.
                        Log.d(getClass().getName(), "The device token or transfer is not found.");
                    }
                    else {
                        Log.d(getClass().getName(), "Something went wrong permitting the transfer. HTTP status code: "+status.value());
                    }
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return false;
        }
    }

    public static class RenewTask implements Callable<Boolean> {

        @Override
        public Boolean call() {
            try {
                final String url = REMOTE_ADDRESS+"/Renew";
                RestTemplate restTemplate = new RestTemplate();

                Device device = new Device();
                device.setToken(FirebaseInstanceId.getInstance().getToken());

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                HttpEntity<Device> entity = new HttpEntity<>(device);

                try {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        return true;
                    }
                } catch (HttpClientErrorException e) {
                    HttpStatus status = e.getStatusCode();

                    if (status == HttpStatus.FORBIDDEN) { // Returns HTTP code 403 if the certificate is not found or the thumbprint does not match.
                        Log.d(getClass().getName(), "The certificate is not found or the thumbprint does not match.");
                    }
                    else if (status == HttpStatus.NOT_FOUND) { // Returns HTTP code 404 if the device token or MDL is not found.
                        Log.d(getClass().getName(), "The device token or MDL is not found.");
                    }
                    else {
                        Log.d(getClass().getName(), "Something went wrong renewing. HTTP status code: "+status.value());
                    }
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return false;
        }
    }

    public static class RequestReaderTokenTask implements Callable<ReaderToken> {

        @Override
        public ReaderToken call() {
            try {
                final String url = REMOTE_ADDRESS+"/RequestReaderToken";
                RestTemplate restTemplate = new RestTemplate();

                ReaderTokenRequest request = new ReaderTokenRequest();
                request.setDeviceToken(FirebaseInstanceId.getInstance().getToken());
                request.setPermittedDatagroups(Arrays.asList("?", "??", "???"));

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                HttpEntity<ReaderTokenRequest> entity = new HttpEntity<>(request);

                try {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, ReaderToken.class);
                    return (ReaderToken)response.getBody();
                } catch (HttpClientErrorException e) {
                    HttpStatus status = e.getStatusCode();

                    if (status == HttpStatus.FORBIDDEN) { // Returns HTTP code 403 if the certificate is not found or the thumbprint does not match.
                        Log.d(getClass().getName(), "The certificate is not found or the thumbprint does not match.");
                    }
                    else if (status == HttpStatus.NOT_FOUND) { // Returns HTTP code 404 if the device token or license is not found.
                        Log.d(getClass().getName(), "The device token or license is not found.");
                    }
                    else {
                        Log.d(getClass().getName(), "Something went wrong requesting the reader token. HTTP status code: "+status.value());
                    }
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return null;
        }
    }

    public static class RevokeTask implements Callable<Boolean> {

        @Override
        public Boolean call() {
            try {
                final String url = REMOTE_ADDRESS+"/Revoke";
                RestTemplate restTemplate = new RestTemplate();

                Device device = new Device();
                device.setToken(FirebaseInstanceId.getInstance().getToken());

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                HttpEntity<Device> entity = new HttpEntity<>(device);

                try {
                    ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        return true;
                    }
                } catch (HttpClientErrorException e) {
                    HttpStatus status = e.getStatusCode();

                    if (status == HttpStatus.FORBIDDEN) { // Returns HTTP code 403 if the certificate is not found or the thumbprint does not match.
                        Log.d(getClass().getName(), "The certificate is not found or the thumbprint does not match.");
                    }
                    else if (status == HttpStatus.NOT_FOUND) { // Returns HTTP code 404 if the device token or MDL is not found.
                        Log.d(getClass().getName(), "The device token or MDL is not found.");
                    }
                    else {
                        Log.d(getClass().getName(), "Something went wrong revoking. HTTP status code: "+status.value());
                    }
                }

            } catch (Exception e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return false;
        }
    }

}
