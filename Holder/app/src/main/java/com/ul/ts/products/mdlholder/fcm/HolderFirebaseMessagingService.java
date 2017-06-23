package com.ul.ts.products.mdlholder.fcm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ul.ts.products.mdlholder.PersoActivity;
import com.ul.ts.products.mdlholder.SettingsFragment;
import com.ul.ts.products.mdlholder.utils.NetUtils;
import com.ul.ts.products.mdlholder.webapi.BooleanValue;
import com.ul.ts.products.mdlholder.webapi.WebAPI;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HolderFirebaseMessagingService extends FirebaseMessagingService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            System.out.println("getData() Key: " + entry.getKey() + " value: " + entry.getValue());

            if (entry.getKey().equalsIgnoreCase("message")) {
                NetUtils.setUpSSL(getApplicationContext());

                if (entry.getValue().equalsIgnoreCase("download")) {
                    Log.d(getClass().getName(), "Download received");

                    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                    Log.d(getClass().getName(), "Current component: "+cn.getClassName());

                    if (cn.getClassName().equals(PersoActivity.class.getName())) {
                        Log.d(getClass().getName(), "Sending broadcast DOWNLOAD_LICENSE;");
                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                        localBroadcastManager.sendBroadcast(new Intent("download_license"));
                    } else {
                        new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                public void run() {
                                    new WebAPI.DownloadLicenseDataTask(getApplicationContext()).execute();
                                }
                            }
                        );
                    }

                } else if (entry.getValue().equalsIgnoreCase("revoke")) {
                    Log.d(getClass().getName(), "Revoke received");

                    Future<BooleanValue> shouldRevokeFuture = executor.submit(new WebAPI.ShouldRevokeTask());

                    try {
                        BooleanValue shouldRevoke = shouldRevokeFuture.get();
                        if (shouldRevoke != null && shouldRevoke.isValue()) {
                            SettingsFragment.deleteAllData(getApplicationContext());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(getClass().getName(), e.getMessage(), e);
                    }
                }
            }
        }
    }
}
