package com.ul.ts.products.mdlreader.connection.online;


import android.support.annotation.RequiresPermission;

import com.ul.ts.products.mdlreader.AbstractLicenseActivity;
import com.ul.ts.products.mdlreader.ReadOnlineActivity;
import com.ul.ts.products.mdlreader.connection.RemoteConnection;
import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.data.APDUInterface;

import java.io.IOException;

public class OnlineConnection extends RemoteConnection {

    private final AbstractLicenseActivity activity;

    private String URL;

    public OnlineConnection(AbstractLicenseActivity activity, String URL) {
        this.activity = activity;
        this.URL = URL;
    }

    public void runSetupSteps() throws RemoteConnectionException {
        activity.loadLicense(new APDUInterface() {
            @Override
            public byte[] send(byte[] command) throws IOException {
                return new byte[0];
            }

            @Override
            public void close() {

            }
        });
        setupCompleted = true;
    }

    public String getURL() {
        return URL;
    }

    public void pause() {};
    public void resume() {};
    public void shutdown() {};

    /**
     * Start Peer discovery. Once the peer has been found,
     * activity.loadLicense(apduInterface) should be called with an applicable interface;
     */
    public void findPeers() {};
}
