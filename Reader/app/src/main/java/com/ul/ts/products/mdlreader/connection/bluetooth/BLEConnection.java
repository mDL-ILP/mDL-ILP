package com.ul.ts.products.mdlreader.connection.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ul.ts.products.mdlreader.AbstractLicenseActivity;
import com.ul.ts.products.mdlreader.connection.RemoteConnection;
import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.data.APDUInterface;

public class BLEConnection extends RemoteConnection {
    private static int REQUEST_ENABLE_BT = 1;
    private final String TAG = getClass().getName();
    private final AbstractLicenseActivity activity;
    private final byte[] serviceData;

    private final AdvertiseCallback advCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                /* This is probably OK -- we accidentally called startAdvertising while we were
                   already doing so.
                 */
                return;
            }
            activity.fail("Advertisement failed with error code " + errorCode);
        }
    };

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mAdvertiser;
    private GattService gattService;

    public BLEConnection(AbstractLicenseActivity activity, byte[] serviceData) {
        this.activity = activity;
        this.serviceData = serviceData;
        /* at some point, call activity.loadLicense(new SocketAPDUInterface(s)); */
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


        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mAdvertiser == null) {
            throw new RemoteConnectionException("Reading over Bluetooth is not supported on this device.", false);
        }

        if (gattService == null) {
            gattService = new GattService(activity, mBluetoothManager);
        }
        setupCompleted = true;
    }

    private void startAdvertising() {
        AdvertiseSettings advSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();

        AdvertiseData advData = new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(Constants.SERVICE_pUUID)
                .addServiceData(Constants.SERVICE_pUUID, serviceData)
                .build();

        AdvertiseData advScanResponse = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build();

        if (mAdvertiser != null) {
            gattService.startAdvertising();
            mAdvertiser.startAdvertising(advSettings, advData, advScanResponse, advCallback);
            Log.d(TAG, "Started advertisement of mDL service with data " + new String(serviceData));
        } else {
            Log.e(TAG, "mAdvertiser not available!");
        }
    }

    private void stopAdvertising() {
        if (mAdvertiser != null) mAdvertiser.stopAdvertising(advCallback);
        if (gattService != null) gattService.stopAdvertising();
        Log.d(TAG, "Stopped advertisement of mDL service");
    }

    public void findPeers() {
        startAdvertising();
    }

    public void pause() {
        stopAdvertising();
    }

    public void resume() {
        startAdvertising();
    }

    public void shutdown() {
        stopAdvertising();
        if (gattService != null) gattService.close();
        Log.d(TAG, "Cleaned up BLE");
    }
}
