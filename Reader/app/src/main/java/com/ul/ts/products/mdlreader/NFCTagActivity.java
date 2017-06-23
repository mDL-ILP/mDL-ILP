package com.ul.ts.products.mdlreader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.ul.ts.products.mdllibrary.connection.DeviceEngagement;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileInterfaceIndependent;
import com.ul.ts.products.mdllibrary.connection.TLVData;
import com.ul.ts.products.mdllibrary.connection.Utils;
import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.connection.hce.NFCEngagement;

import butterknife.BindView;
import butterknife.ButterKnife;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class NFCTagActivity extends EngagementActivity {
    private String TAG = getClass().getName();
    @BindView(R.id.barcode_view)  SurfaceView scanner;
    private NFCEngagement nfcEngagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous_capture);
        ButterKnife.bind(this);
        ContextCompat.checkSelfPermission(this, Manifest.permission.NFC);

        nfcEngagement = new NFCEngagement(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            nfcEngagement.runSetupSteps();
            nfcEngagement.resume();
            nfcEngagement.findPeers();
        } catch (RemoteConnectionException e) {
            Toast.makeText(this, "NFC engagement setup unsuccesful: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        codeFound = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcEngagement.pause();
    }

}
