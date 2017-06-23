package com.ul.ts.products.mdlreader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
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

public class ContinuousCaptureActivity extends EngagementActivity {
    private String TAG = getClass().getName();
    @BindView(R.id.barcode_view)  SurfaceView scanner;
    QREader reader;
    private boolean codeFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous_capture);
        ButterKnife.bind(this);
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        reader = new QREader.Builder(this, scanner, new BarcodeRead(this))
                .facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(scanner.getHeight())
                .width(scanner.getWidth())
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reader.initAndStart(scanner);

        codeFound = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        reader.releaseAndCleanup();
    }

    private class BarcodeRead implements QRDataListener {
        private Activity activity;

        public BarcodeRead(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onDetected(final String data) {
            DeviceEngagement deviceEngagement;

            Log.d(TAG, data + " scanned");

            processEngagement(Utils.readQR(data));
        }
    }
}
