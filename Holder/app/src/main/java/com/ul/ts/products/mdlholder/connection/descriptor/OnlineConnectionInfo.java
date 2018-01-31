package com.ul.ts.products.mdlholder.connection.descriptor;

import android.support.annotation.NonNull;

import com.ul.ts.products.mdlholder.connection.descriptor.compat.AbstractLicenseActivity;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.RemoteConnection;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.WiFiDirectConnection;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfile;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileOnline;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileWD;

import java.io.IOException;


public class OnlineConnectionInfo extends ConnectionInfo {
    public final String URL;

    public OnlineConnectionInfo(String URL) {
        this.URL = URL;
    }

    @NonNull
    @Override
    public String getConnectionType() {
        return "Online";
    }

    @NonNull
    @Override
    public String getConnectionDetails() {
        return URL;
    }

    @NonNull
    @Override
    public String getConnectionString() {
        return getConnectionDetails();
    }

    @NonNull
    @Override
    public InterchangeProfile getInterchangeProfile() throws IOException {
        return new InterchangeProfileOnline(URL);
    }

    @NonNull
    @Override
    public RemoteConnection getRemoteConnection(AbstractLicenseActivity activity) {
        return null;
    }

    @Override
    public boolean usesNFC() {
        return false;
    }
}
