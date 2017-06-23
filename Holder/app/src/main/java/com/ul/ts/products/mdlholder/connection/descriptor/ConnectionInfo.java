package com.ul.ts.products.mdlholder.connection.descriptor;

import android.support.annotation.NonNull;

import com.ul.ts.products.mdlholder.connection.descriptor.compat.AbstractLicenseActivity;
import com.ul.ts.products.mdlholder.connection.descriptor.compat.RemoteConnection;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfile;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileBLE;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileNFC;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileWD;

import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ConnectionInfo {
    @NonNull
    public abstract String getConnectionType();

    @NonNull
    public abstract String getConnectionDetails();

    @NonNull
    public abstract String getConnectionString();

    @NonNull
    public abstract RemoteConnection getRemoteConnection(AbstractLicenseActivity activity);

    @NonNull
    public abstract InterchangeProfile getInterchangeProfile() throws IOException;

    @Override
    public String toString() {
        return getConnectionType() + ": " + getConnectionDetails();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionInfo that = (ConnectionInfo) o;

        if (!getConnectionType().equals(that.getConnectionType())) return false;
        return getConnectionDetails().equals(that.getConnectionDetails());

    }

    @Override
    public int hashCode() {
        int result = getConnectionType().hashCode();
        result = 31 * result + getConnectionDetails().hashCode();
        return result;
    }

    protected static ConnectionInfo getConnectionInfo(String result) throws ParseException {
        /* BLE:abcd */
        Matcher bleMatcher = Pattern.compile("BLE:(\\w+)").matcher(result);

        /* aa:bb:cc:dd:ee:ff */
        Matcher wdMatcher = Pattern.compile("((?:[\\da-f]{2}:){5}[\\da-f]{2})", Pattern.CASE_INSENSITIVE).matcher(result);

        if (result.startsWith("NFC")) {
            return new NFCConnectionInfo();
        }
        if (bleMatcher.matches()) {
            return new BLEConnectionInfo(bleMatcher.group(1));
        } else if (wdMatcher.matches()) {
            return new WDConnectionInfo(wdMatcher.group(1));
        } else {
            throw new ParseException("Invalid connection info; expected BLE:<code> or WifiDirect MAC address.", 0);
        }
    }

    protected static ConnectionInfo getConnectionInfo(InterchangeProfile ip) {
        if (ip instanceof InterchangeProfileBLE) {
            return new BLEConnectionInfo(new String(((InterchangeProfileBLE) ip).antiCollisionIdentifier));
        } else if (ip instanceof InterchangeProfileWD) {
            return new WDConnectionInfo(((InterchangeProfileWD) ip).MAC);
        } else if (ip instanceof InterchangeProfileNFC) {
            return new NFCConnectionInfo();
        } else {
            throw new RuntimeException("Unknown InterchangeProfile");
        }
    }

    public abstract boolean usesNFC();
}

