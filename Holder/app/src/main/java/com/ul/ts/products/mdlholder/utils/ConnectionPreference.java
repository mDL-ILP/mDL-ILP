package com.ul.ts.products.mdlholder.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.ul.ts.products.mdlholder.AbstractTransferActivity;
import com.ul.ts.products.mdlholder.R;
import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.connection.TransferInterface;
import com.ul.ts.products.mdlholder.connection.bluetooth.BTTransfer;
import com.ul.ts.products.mdlholder.connection.descriptor.TransferInfo;
import com.ul.ts.products.mdlholder.connection.hce.NFCTransfer;
import com.ul.ts.products.mdlholder.connection.wifi.WiFiTransfer;

public class ConnectionPreference {
    public static final String BLUETOOTH = "Bluetooth";
    public static final String WIFI_DIRECT = "WiFiDirect";
    public static final String NFC = "NFC";
    public static final String CONNECTION_PREFERENCE_KEY = "connection_preference";

    public static String getCurrentConnectionPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(CONNECTION_PREFERENCE_KEY, context.getString(R.string.connectionOptionDefault));
    }

    public static void setCurrentConnectionPreference(Context context, String value) {
        if (checkPrerequisites(context, value)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(CONNECTION_PREFERENCE_KEY, value).apply();
        }
    }

    public static TransferInterface getTransfer(AbstractTransferActivity activity, TransferInfo transferInfo, APDUInterface mdlSim) {
        String method = getCurrentConnectionPreference(activity);
        switch (method) {
            case BLUETOOTH:
                return new BTTransfer(activity, transferInfo, mdlSim);
            case WIFI_DIRECT:
                return new WiFiTransfer(activity, transferInfo, mdlSim);
            case NFC:
                return new NFCTransfer(activity, transferInfo, mdlSim);
            default:
                throw new RuntimeException("Transfer method " + method + " is not supported.");
        }
    }

    public static String toggle(Context context) {
        String currentMethod = getCurrentConnectionPreference(context);
        String newMethod = WIFI_DIRECT; // fallback

        switch (currentMethod) {
            case WIFI_DIRECT: newMethod = BLUETOOTH; break;
            case BLUETOOTH: newMethod = NFC; break;
            case NFC: newMethod = WIFI_DIRECT; break;
        }

        setCurrentConnectionPreference(context, newMethod);

        return newMethod;
    }

    public static boolean checkPrerequisites(Context context, String value) {
        if (value == WIFI_DIRECT) {
            /* maybe check if wifi is available? */
            return true;
        } else if (value == BLUETOOTH) {
            return checkBluetoothPrerequisites(context);
        } else if (value == NFC) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkBluetoothPrerequisites(Context context) {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (!(context instanceof Activity)) {
                return false;
            }
            Activity activity = (Activity) context;

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Toast.makeText(context, R.string.connection_need_coarse_location, Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
            return false;
        }
        return true;
    }
}
