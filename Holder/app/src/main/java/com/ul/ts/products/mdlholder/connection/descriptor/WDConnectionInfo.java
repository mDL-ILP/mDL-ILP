package com.ul.ts.products.mdlholder.connection.descriptor;

import android.support.annotation.NonNull;

import com.ul.ts.products.mdlholder.connection.descriptor.compat.AbstractLicenseActivity;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.RemoteConnection;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.WiFiDirectConnection;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfile;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileWD;

import java.io.IOException;

public class WDConnectionInfo extends ConnectionInfo {
    public final String MAC;

    public WDConnectionInfo(String MAC) {
        this.MAC = MAC;
    }

    @NonNull
    @Override
    public String getConnectionType() {
        return "Wi-Fi Direct";
    }

    @NonNull
    @Override
    public String getConnectionDetails() {
        return MAC;
    }

    @NonNull
    @Override
    public String getConnectionString() {
        return getConnectionDetails();
    }

    @NonNull
    @Override
    public InterchangeProfile getInterchangeProfile() throws IOException {
        return new InterchangeProfileWD(MAC);
    }

    @NonNull
    @Override
    public RemoteConnection getRemoteConnection(AbstractLicenseActivity activity) {
        return new WiFiDirectConnection(activity, MAC);
    }

    @Override
    public boolean usesNFC() {
        return false;
    }
}
