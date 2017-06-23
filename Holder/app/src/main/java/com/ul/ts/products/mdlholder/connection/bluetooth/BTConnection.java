package com.ul.ts.products.mdlholder.connection.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.ul.ts.products.mdlholder.connection.RemoteConnection;
import com.ul.ts.products.mdlholder.connection.RemoteConnectionException;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class BTConnection implements RemoteConnection {
    private static int REQUEST_ENABLE_BT = 1;

    private final Activity activity;
    private final BTTransfer transfer;
    private final String serviceData;

    private final String TAG = getClass().getName();
    private final List<ScanFilter> filters;
    private final ScanSettings settings;
    private final ScanCallback callback;

    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean setupCompleted = false;

    public BTConnection(Activity activity, BTTransfer transfer) {
        this.activity = activity;
        this.transfer = transfer;

        SecureRandom random = new SecureRandom();
        serviceData = new BigInteger(130, random).toString(32).substring(0,8);

        filters = getScanFilters(serviceData.getBytes());
        settings = getScanSettings();
        callback = getScanCallback(serviceData.getBytes());
    }

    public String getAntiCollisionIdentifier() {
        return serviceData;
    }

    public void runSetupSteps() throws RemoteConnectionException{
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                throw new RemoteConnectionException("Bluetooth is not supported!", false);
            }
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivity(enableBtIntent);

            throw new RemoteConnectionException("Bluetooth is not enabled", true);
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            throw new RemoteConnectionException("Sending over Bluetooth is not supported on this device.", false);
        }

        setupCompleted = true;
    }

    @NonNull
    private ScanCallback getScanCallback(final byte[] serviceData) {
        return new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    processResult(result);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult s: results) {
                        processResult(s);
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.d(TAG, "Scan failed: " + String.valueOf(errorCode));
                }

                public void processResult(ScanResult sr) {
                    Log.d(TAG, "Found device " + sr.getDevice().getName() + "(" + sr.getDevice().getAddress() + ")");

                    /* confirm correct connection key */
                    byte[] sd = sr.getScanRecord().getServiceData(Constants.SERVICE_pUUID);
                    if (sd != null) {
                        if (Arrays.equals(sd, serviceData)) {
                            BTConnection.this.stopScan();
                            transfer.startServer(sr.getDevice());
                        } else {
                            Log.d(TAG, "Incorrect service data: " + new String(sd) + " instead of " + new String(serviceData) + ". Continuing.");
                        }
                    } else {
                        Log.d(TAG, "No service data received -- continuing.");
                    }
                }
            };
    }

    private ScanSettings getScanSettings() {
        return new ScanSettings.Builder()
                    .setReportDelay(0)
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .build();
    }

    @NonNull
    private List<ScanFilter> getScanFilters(final byte[] serviceData) {
        return Arrays.asList(
                    new ScanFilter[]{
                            new ScanFilter.Builder()
                                    .setServiceData(Constants.SERVICE_pUUID, serviceData)
                                    .build()
                    });
    }

    private void startScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.startScan(filters, settings, callback);
            Log.d(TAG, "Started BLE scanner");
        }
    }

    private void stopScan() {
        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(callback);
            Log.d(TAG, "Stopped BLE scanner");
        }
    }

    @Override
    public void pause() {
        stopScan();
    }

    @Override
    public void resume() {
        try {
            runSetupSteps();
            startScan();
        } catch (RemoteConnectionException e) {
            Log.d(TAG, "Could not setup connection", e);
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();

            if (!e.retry) {
                activity.onBackPressed();
            }
        }
    }

    @Override
    public void shutdown() {
        stopScan();
        Log.d(TAG, "Shutdown Bluetooth");
    }

    @Override
    public void findPeers() {
    }
}
