package com.ul.ts.products.mdlholder;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ul.ts.products.mdlholder.utils.StorageUtils;
import com.ul.ts.products.mdlholder.webapi.Certificate;
import com.ul.ts.products.mdlholder.webapi.MobileDrivingLicense;
import com.ul.ts.products.mdlholder.webapi.TransferID;
import com.ul.ts.products.mdlholder.webapi.WebAPI;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PersoActivity extends AppCompatActivity {

    @BindView(R.id.tokenTextView) TextView tokenText;

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perso);
        ButterKnife.bind(this);

        // Check if we have a client TLS certificate, if not launch RegisterFCMTokenTask otherwise just show activity
        if (!StorageUtils.objectExists(this, getString(R.string.tls_client_cert_key))) {
            new RegisterFCMTokenTask().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver, new IntentFilter("download_license"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadReceiver);
        finish();
    }

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {

                Log.d(getClass().getName(), "Received intent "+intent.toString());

                final WebAPI.DownloadLicenseDataTask task = new WebAPI.DownloadLicenseDataTask(PersoActivity.this);
                task.execute();

                // NOTE(JS): Handle the license download completing and update UI
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MobileDrivingLicense license = task.get();

                            // If the download was successful then move to main screen.
                            if (license != null) {
                                Intent intent = new Intent(PersoActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(getClass().getName(), e.getMessage(), e);
                        }
                    }
                }).start();
            }
        }
    };

    @OnClick(R.id.buttonEnroll)
    public void enroll() {
        new RequestTransferIdTask().execute();
    }

    @OnClick(R.id.buttonContinue)
    public void launchMain() {
        try {
            MobileDrivingLicense.fromJson(this.getResources().openRawResource(R.raw.example_license)).store(this);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not open example license: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class RegisterFCMTokenTask extends AsyncTask<Void, Void, Certificate> {

        private ProgressDialog progressDialog;

        public RegisterFCMTokenTask() {
            progressDialog = new ProgressDialog(PersoActivity.this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Registering Device");
            progressDialog.setMessage("Registering device with server...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected Certificate doInBackground(Void... voids) {

            Callable<Certificate> registerTask = new WebAPI.RegisterTask(PersoActivity.this);
            Future<Certificate> f = service.submit(registerTask);

            Certificate cert = null;
            try {
                cert = f.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }

            return cert;
        }

        @Override
        protected void onPostExecute(Certificate certificate) {
            progressDialog.dismiss();

            if (certificate != null) {
                StorageUtils.saveObject(PersoActivity.this, getString(R.string.tls_client_cert_key), certificate);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(PersoActivity.this);
                builder.setTitle(R.string.perso_problem_title);
                builder.setMessage(R.string.perso_problem_register);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.create().show();
            }
        }
    }

    private class RequestTransferIdTask extends AsyncTask<Void, Void, TransferID> {

        private ProgressDialog progressDialog;

        public RequestTransferIdTask() {
            progressDialog = new ProgressDialog(PersoActivity.this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Requesting Token");
            progressDialog.setMessage("Requesting token from server...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected TransferID doInBackground(Void... voids) {

            Callable<TransferID> registerTask = new WebAPI.RequestTransferIDTask();
            Future<TransferID> f = service.submit(registerTask);

            TransferID transferID = null;
            try {
                transferID = f.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            return transferID;
        }

        @Override
        protected void onPostExecute(TransferID transferID) {
            progressDialog.dismiss();

            if (transferID != null) {
               tokenText.setText(transferID.getId());

                // Set a timer for 10 mintes since the transfer id is only valid for that length of time
                new CountDownTimer(600000, 600000) {
                    public void onTick(long millisUntilFinished) {
                        // nothing to do
                    }

                    public void onFinish() {
                        tokenText.setText("*****");
                    }
                }.start();


            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(PersoActivity.this);
                builder.setTitle(R.string.perso_problem_title);
                builder.setMessage(R.string.perso_problem_token);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.create().show();
            }
        }
    }

}
