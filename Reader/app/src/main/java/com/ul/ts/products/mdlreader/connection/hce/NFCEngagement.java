package com.ul.ts.products.mdlreader.connection.hce;

import android.app.Activity;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.ul.ts.products.mdlreader.ContinuousCaptureActivity;
import com.ul.ts.products.mdlreader.NFCTagActivity;
import com.ul.ts.products.mdlreader.data.ReadDataScript;

import java.io.IOException;

public class NFCEngagement extends NFCConnection {
    private final String TAG = this.getClass().getName();
    private final NFCTagActivity ccActivity;

    public NFCEngagement(final NFCTagActivity activity) {
        super(activity);
        this.ccActivity = activity;
    }

    @Override
    protected void fail(final String message) {
        // not sure what should happen here...
    }

    @Override
    protected void dispatchLoadTask(final IsoDepApduInterface apduInterface) {
        try {
            final ReadDataScript script = new ReadDataScript(apduInterface, ReadDataScript.AID_MDL_ENGAGEMENT);
            byte[] engagementData = script.readFile(0);
            ccActivity.processEngagement(engagementData);
        } catch (IOException e) {
            Log.e(TAG, "Could not process NFC engagement: ", e);
        }
    }


}
