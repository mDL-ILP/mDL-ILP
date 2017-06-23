package com.ul.ts.products.mdlholder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.orangegangsters.lollipin.lib.PinCompatActivity;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.AppLockImpl;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ul.ts.products.mdlholder.utils.NetUtils;
import com.ul.ts.products.mdlholder.utils.StorageUtils;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Provides the app entry point. This activity decides what to do and where to go when the
 * application is opened by the user.
 *
 * The application can be in 3 different start up states:
 *
 *  1) First launch where a PIN code isn't defined
 *  2) Pin code has been defined but the application isn't personalized
 *  3) Fully perso'd application.
 *
 * The app will do the following in the case of:
 *  1) Request a PIN is set up then go to step 2
 *  2) Ask user to enter pin, request a token from the server, download the perso data and on
 *     success go to step 3
 *  3) Ask user to enter pin, launch the main activity
 */
public class EntryPointActivity extends PinCompatActivity {

    private static final int LAUNCH_PERSO = 100;

    private static final int LAUNCH_MAIN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(getClass().getName(), "Firebase token: " + refreshedToken);

        NetUtils.setUpSSL(this);

        // User cannot get a 'hint' of what the PIN code is.
        AppLockImpl.getInstance(this, PinEntryActivity.class).setShouldShowForgot(false);
        // 20 sec timeout?
        AppLockImpl.getInstance(this, PinEntryActivity.class).setTimeout(20000);

        Intent intent = new Intent(this, PinEntryActivity.class);
        if (!AppLockImpl.getInstance(this, PinEntryActivity.class).isPasscodeSet()) {
            // state 1
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
            startActivityForResult(intent, LAUNCH_PERSO);

        } else if (StorageUtils.getBooleanPref(this, getString(R.string.perso_complete_key))) {
            // state 3
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
            startActivityForResult(intent, LAUNCH_MAIN);

        } else {
            // state 2
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
            startActivityForResult(intent, LAUNCH_PERSO);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LAUNCH_PERSO: {
                Intent intent = new Intent(this, PersoActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case LAUNCH_MAIN: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }
}
