package com.ul.ts.products.mdlholder.connection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Waits for the WiFi Direct callback to dismiss the progress dialog
 */
public class InterfaceAsyncTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog progressDialog;
    private final CountDownLatch latch = new CountDownLatch(1);

    public InterfaceAsyncTask(Activity activity,
                              String title,
                              String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    public void dismiss() {
        latch.countDown();
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            latch.await();
        } catch (InterruptedException e) {

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressDialog.dismiss();
    }

}
