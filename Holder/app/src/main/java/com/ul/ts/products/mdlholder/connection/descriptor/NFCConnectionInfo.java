package com.ul.ts.products.mdlholder.connection.descriptor;

import android.support.annotation.NonNull;

import com.ul.ts.products.mdlholder.connection.descriptor.compat.AbstractLicenseActivity;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.NFCConnection;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.RemoteConnection;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfile;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileNFC;

import java.io.IOException;


public class NFCConnectionInfo extends ConnectionInfo {
    @NonNull
    @Override
    public String getConnectionType() {
        return "Near-Field Communication";
    }

    @NonNull
    @Override
    public String getConnectionDetails() {
        return "";
    }

    @NonNull
    @Override
    public String getConnectionString() {
        return "NFC";
    }

    @NonNull
    @Override
    public InterchangeProfile getInterchangeProfile() throws IOException {
        return new InterchangeProfileNFC(255);
    }

    @NonNull
    @Override
    public RemoteConnection getRemoteConnection(AbstractLicenseActivity activity) {
        return new NFCConnection(activity);
    }

    @Override
    public boolean usesNFC() {
        return true;
    }
}
