package com.ul.ts.products.mdlreader;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.ul.ts.products.mdlreader.data.APDUInterface;
import com.ul.ts.products.mdlreader.data.DrivingLicence;
import com.ul.ts.products.mdlreader.data.ReadFileCallback;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

class TimeMeasurement {
    double timestamp;
    int progress;
    String description;

    public TimeMeasurement(long nanoTime, int progress, String description) {
        this.timestamp = nanoTime / 1e9;
        this.progress = progress;
        this.description = description;
    }

    public String logRow(TimeMeasurement first, TimeMeasurement previous) {
        double totalDelta = (first == null) ? 0 : timestamp - first.timestamp;
        double previousDelta = (previous == null) ? 0 : timestamp - previous.timestamp;

        return String.format(
                "%.3f (+%.3f): %d %s",
                totalDelta, previousDelta,
                progress, description
        );
    }
}

abstract class LoadLicenceTask extends AsyncTask<Void, Integer, DrivingLicence> {
    private final String TAG = getClass().getName();

    private List<TimeMeasurement> steps = new ArrayList<> ();
    protected final APDUInterface apduInterface;
    protected final ProgressDialog progressDialog;
    protected final AbstractLicenseActivity activity;

    protected class PPC implements ReadFileCallback {
        private int expectedSize;
        private int statusCode;
        private int startPercentage;
        private int endPercentage;

        public PPC(int statusCode, int expectedSize, int startPercentage, int endPercentage) {
            this.expectedSize = expectedSize;
            this.statusCode = statusCode;
            this.startPercentage = startPercentage;
            this.endPercentage = endPercentage;
        }

        @Override
        public void afterReceive(int bytesRead) {
            int current = startPercentage + ((endPercentage - startPercentage) * bytesRead / expectedSize);
            if (current < endPercentage) {
                publishProgress(statusCode, current);
            } else {
                publishProgress(statusCode, endPercentage);
            }
        }
    }

    public LoadLicenceTask(AbstractLicenseActivity activity, APDUInterface apduInterface) {
        this.activity = activity;
        this.apduInterface = apduInterface;

        progressDialog = new ProgressDialog(activity);
    }

    public String getString(int resId) {
        return activity.getString(resId);
    }

    public int PROGRESS_CONNECTING = 0;
    public int PROGRESS_EFSOD = 1;
    public int PROGRESS_DG1 = 20;
    public int PROGRESS_DG6 = 40;
    public int PROGRESS_DG11 = 60;
    public int PROGRESS_DG12 = 60;
    public int PROGRESS_DG13 = 80;
    public int PROGRESS_DONE = 90;

    @Override
    protected void onPreExecute()
    {
        progressDialog.setTitle(getString(R.string.license_progressTitle));
        progressDialog.setMessage(getString(R.string.license_establish));

        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                activity.fail(getString(R.string.transfer_cancelled));
            }
        });

        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);

        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        //set the current progress of the progress progressDialog
        if (values.length == 1) {
            progressDialog.setProgress(values[0]);
        } else {
            progressDialog.setProgress(values[1]);
        }
        String message = getString(R.string.license_establish);
        if (values[0] >= PROGRESS_EFSOD)
            message = getString(R.string.license_efsod);
        if (values[0] >= PROGRESS_DG1)
            message = getString(R.string.license_dg1);
        if (values[0] >= PROGRESS_DG6)
            message = getString(R.string.license_dg6);
        if (values[0] >= PROGRESS_DG11)
            message = getString(R.string.license_dg11);
        if (values[0] >= PROGRESS_DG12)
            message = getString(R.string.license_dg12);
        if (values[0] >= PROGRESS_DG13)
            message = getString(R.string.license_dg13);
        if (values[0] >= PROGRESS_DONE)
            message = getString(R.string.license_progressTitle);
        steps.add(new TimeMeasurement(System.nanoTime(), values[0], message));
        progressDialog.setMessage(message);
    }

    @Override
    protected void onPostExecute(DrivingLicence licence)
    {
        steps.add(new TimeMeasurement(System.nanoTime(), 100, "onPostExecute"));

        // close the progress progressDialog
        // todo: the catch was specifcally added to deal with the only case, where no apduinterface is present. This should be fixed.
        try {
            apduInterface.close();
        } catch (Exception e) {
            Log.e(TAG, "Error occurred:", e);
        }

        progressDialog.dismiss();
        if (activity.connection != null) activity.connection.shutdown();

        Log.d(TAG+ ".timing", "TIMING DATA");
        TimeMeasurement first = null;
        TimeMeasurement previous = null;
        for (TimeMeasurement t: steps
             ) {
            if (first == null) first = t;
            if (previous == null) previous = t;

            Log.d(TAG + ".timing", t.logRow(first, previous));
            previous = t;
        }
        Log.d(TAG+ ".timing", "TIMING DATA END");

        activity.isDoneLoading = true;
    }
}
