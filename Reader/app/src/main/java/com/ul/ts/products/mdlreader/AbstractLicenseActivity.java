package com.ul.ts.products.mdlreader;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ul.ts.products.mdllibrary.connection.DataMinimizationParameter;
import com.ul.ts.products.mdllibrary.connection.DeviceEngagement;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfile;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileBLE;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileNFC;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileOnline;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileWD;
import com.ul.ts.products.mdllibrary.connection.TLVData;
import com.ul.ts.products.mdlreader.connection.RemoteConnection;
import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.connection.bluetooth.BLEConnection;
import com.ul.ts.products.mdlreader.connection.hce.NFCLicenseTransferConnection;
import com.ul.ts.products.mdlreader.connection.online.OnlineConnection;
import com.ul.ts.products.mdlreader.connection.wifi.WiFiDirectConnection;
import com.ul.ts.products.mdlreader.data.APDUInterface;
import com.ul.ts.products.mdlreader.data.DrivingLicence;

import java.io.IOException;

public abstract class AbstractLicenseActivity extends AppCompatActivity {
    private static final String TAG = "AbstractLicenseActivity";
    protected boolean isDoneLoading = false;
    protected ProgressDialog progressDialog;
    protected DeviceEngagement deviceEngagement;
    protected DataMinimizationParameter dataMinimizationParameter;
    protected RemoteConnection connection;
    protected DrivingLicence licence;

    public abstract void loadLicense(APDUInterface apduInterface);


    @Override
    protected void onResume() {
        Log.v(TAG, this.toString() + "::onResume");
        super.onResume();
        if (!isDoneLoading) {
            resumeConnection();
        }
    }

    protected void resumeConnection() {
        try {
            connection.runSetupSteps();
            connection.resume();
            connection.findPeers();
        } catch (RemoteConnectionException e) {
            Log.d(TAG, "Could not setup connection", e);
            if (e.retry) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                fail(e.getMessage());
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        byte[] deviceEngagementData = intent.getByteArrayExtra("deviceEngagement");
        try {
            deviceEngagement = new DeviceEngagement(new TLVData(deviceEngagementData));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final InterchangeProfile transferInterchange = deviceEngagement.getTransferInterchangeProfile();
        dataMinimizationParameter = deviceEngagement.getInterfaceIndependentProfile().dataMinimizationParameter;

        progressDialog = createTransferProgressDialog(transferInterchange);
        progressDialog.show();

        if (transferInterchange instanceof InterchangeProfileBLE) {
            connection = new BLEConnection(this, ((InterchangeProfileBLE) transferInterchange).antiCollisionIdentifier);
        } else if (transferInterchange instanceof InterchangeProfileNFC) {
            connection = new NFCLicenseTransferConnection(this);
        } else if (transferInterchange instanceof InterchangeProfileWD) {
            connection = new WiFiDirectConnection(this, ((InterchangeProfileWD) transferInterchange).MAC);
        } else if (transferInterchange instanceof InterchangeProfileOnline) {
            connection = new OnlineConnection(this, ((InterchangeProfileOnline) transferInterchange).URL);
        } else {
            throw new UnsupportedOperationException("transferInterchange not supported");
        }
    }

    @NonNull
    protected ProgressDialog createTransferProgressDialog(InterchangeProfile connectionInfo) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Trying to connect..");
        progressDialog.setMessage("Connecting to " + connectionInfo);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onBackPressed();
            }
        });
        return progressDialog;
    }

    public void fail(String reason) {
        progressDialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Failed to get License");
        builder.setMessage(reason);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onBackPressed();
            }
        });
        builder.create().show();
    }

    public void setLicence(DrivingLicence licence) {
        this.licence = licence;
    }
}
