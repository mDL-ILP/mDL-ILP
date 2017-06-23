package com.ul.ts.products.mdlreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ul.ts.products.mdllibrary.connection.DeviceEngagement;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileInterfaceIndependent;
import com.ul.ts.products.mdllibrary.connection.TLVData;

abstract class EngagementActivity extends AppCompatActivity {
    protected boolean codeFound = false;
    private final String TAG = this.getClass().getName();

    public void processEngagement(final byte[] data) {
        final DeviceEngagement deviceEngagement;
        try {
            final TLVData tlvData = new TLVData(data);
            deviceEngagement = new DeviceEngagement(tlvData);
            Log.d(TAG, deviceEngagement.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error in parsing DeviceEngagement", e);
            return;
        }

        InterchangeProfileInterfaceIndependent ipii = deviceEngagement.getInterfaceIndependentProfile();
        Intent intent;

        if (ipii.dataMinimizationParameter.ageLimited()) {
            intent = new Intent(this, ReadAgeActivity.class);
        } else {
            intent = new Intent(this, ReadLicenseActivity.class);
        }

        intent.putExtra("deviceEngagement", deviceEngagement.toDER());

        // prevent intent from firing more than once (resulting in two ReadLicenseActivity
        // activities started for the same QR code)
        // we cannot simply call reader.releaseAndCleanup(); as this
        // hangs the activity.
        if (!codeFound) {
            codeFound = true;
            startActivity(intent);
        }
        finish();
    }
}
