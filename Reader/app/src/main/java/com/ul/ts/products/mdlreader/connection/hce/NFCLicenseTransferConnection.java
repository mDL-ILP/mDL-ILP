package com.ul.ts.products.mdlreader.connection.hce;

import android.content.Context;

import com.ul.ts.products.mdlreader.AbstractLicenseActivity;

public class NFCLicenseTransferConnection extends NFCConnection {
    private final AbstractLicenseActivity licenseActivity;

    public NFCLicenseTransferConnection(final AbstractLicenseActivity licenseActivity) {
        super(licenseActivity);
        this.licenseActivity = licenseActivity;
    }

    @Override
    protected void dispatchLoadTask(final IsoDepApduInterface apduInterface) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                licenseActivity.loadLicense(apduInterface);
            }
        });
    }

    @Override
    protected void fail(final String message) {
        licenseActivity.fail(message);
    }
}
