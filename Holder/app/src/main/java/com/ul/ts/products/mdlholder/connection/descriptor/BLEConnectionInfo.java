package com.ul.ts.products.mdlholder.connection.descriptor;

import android.support.annotation.NonNull;

import com.ul.ts.products.mdlholder.connection.descriptor.compat.AbstractLicenseActivity;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.BLEConnection;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.RemoteConnection;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfile;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileBLE;

import java.io.IOException;

public class BLEConnectionInfo extends ConnectionInfo {
    public final String antiCollisionIdentifier;

    public BLEConnectionInfo(String antiCollisionIdentifier) {
        this.antiCollisionIdentifier = antiCollisionIdentifier;
    }

    @NonNull
    @Override
    public String getConnectionType() {
        return "Bluetooth Low Energy";
    }

    @NonNull
    @Override
    public String getConnectionDetails() {
        return antiCollisionIdentifier;
    }

    @NonNull
    @Override
    public String getConnectionString() {
        return "BLE:" + antiCollisionIdentifier;
    }

    @NonNull
    @Override
    public InterchangeProfile getInterchangeProfile() throws IOException {
        return new InterchangeProfileBLE(
            "",
            antiCollisionIdentifier.getBytes(),
            "Unknown",
            600
        );
    }

    @NonNull
    @Override
    public RemoteConnection getRemoteConnection(AbstractLicenseActivity activity) {
        return new BLEConnection(activity, this.antiCollisionIdentifier);
    }

    @Override
    public boolean usesNFC() {
        return false;
    }
}
