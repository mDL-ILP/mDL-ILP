package com.ul.ts.products.mdlholder.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.ul.ts.products.mdlholder.utils.StorageUtils;

public class HolderFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String PREF_DEVICE_TOKEN = "firebase_device_token";

    @Override
    public void onTokenRefresh() {

        // Get the saved token from the shared preferences
        final String oldToken = StorageUtils.getStringPref(this, PREF_DEVICE_TOKEN);
        Log.d(getClass().getName(), "Old token: " + oldToken);

        // Get updated InstanceID token
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(getClass().getName(), "Refreshed token: " + refreshedToken);

        if (oldToken.isEmpty()) {
            handleNewToken(refreshedToken);
        } else {
            handleTokenUpdate(oldToken, refreshedToken);
        }
    }

    private void handleNewToken(String refreshedToken) {
        // save the token to the shared preferences
        StorageUtils.setStringPref(this, PREF_DEVICE_TOKEN, refreshedToken);
    }

    private void handleTokenUpdate(String oldToken, String refreshedToken) {
        // save the token to the shared preferences
        StorageUtils.setStringPref(this, PREF_DEVICE_TOKEN, refreshedToken);
        // TODO(JS): Notify the server that our FCM token has changed?
    }
}
