package com.ul.ts.products.mdlholder;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;
import com.ul.ts.products.mdlholder.webapi.WebAPI;

import java.util.concurrent.Executors;

public class PinEntryActivity extends AppLockActivity {

    @Override
    public void showForgotDialog() {
        // NOTE(JS): Forgot is disabled
    }

    @Override
    public void onPinFailure(int attempts) {
        if (attempts == 3) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.pin_failure_title);
            builder.setMessage(R.string.pin_failure_body);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Executors.newSingleThreadExecutor().submit(new WebAPI.RevokeTask());
                    SettingsFragment.deleteAllData(getApplicationContext());
                    finish();
                }
            });
            builder.create().show();
        }
    }

    @Override
    public void onPinSuccess(int attempts) {
        // NOTE(JS): Normal behaviour, no need to do anything
    }
}
