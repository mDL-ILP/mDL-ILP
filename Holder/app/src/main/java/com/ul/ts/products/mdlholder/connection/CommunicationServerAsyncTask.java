package com.ul.ts.products.mdlholder.connection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.cardsim.MDLSim;

/**
 * CommunicationServerAsyncTask takes care of the user interface and the card simulator interface.
 * The underlying communication task should only implement doInBackground, and should use
 * getApduInterface() to retrieve the (configured) card interface.
 */

public abstract class CommunicationServerAsyncTask extends AsyncTask<Void, String, String> {
    protected static String RESULT_SUCCESS = "License Transferred";
    protected static String PROGRESS_TRANSFERRING = "Transferring data...";
    protected final Activity activity;
    protected final ProgressDialog progressDialog;
    protected final APDUInterface card;

    public CommunicationServerAsyncTask(Activity activity, APDUInterface card) {
        this.activity = activity;
        this.card = card;
        progressDialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setTitle("Communicating to reader app...");
        progressDialog.setMessage("Waiting for connection...");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!activity.isFinishing()) {
                    activity.onBackPressed();
                }
            }
        });
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        if (progress.length > 0) {
            progressDialog.setMessage(progress[0]);
        } else {
            progressDialog.setMessage(PROGRESS_TRANSFERRING);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        progressDialog.setMessage(result);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(progressDialog.getMax());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                progressDialog.dismiss();
                if (!activity.isFinishing()) {
                    activity.onBackPressed();
                }
            }}, 5000);
    }
}
