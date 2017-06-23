package com.ul.ts.products.mdlholder.connection.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.connection.CommunicationServerAsyncTask;
import com.ul.ts.products.mdlholder.utils.ByteUtils;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class GattAsyncTask extends CommunicationServerAsyncTask {
    private final String TAG = getClass().getName();
    private BluetoothDevice device;

    public GattAsyncTask(Activity activity, APDUInterface card) {
        super(activity, card);
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    protected String doInBackground(Void... params) {
        publishProgress("Connecting to " + device.getAddress());
        GattClient gatt = new GattClient(activity);

        try {
            Log.d(TAG, "Connecting to " + device.getAddress() + "...");
            gatt.connect(device);
            Log.d(TAG, "Server: Connected to " + device.getAddress());
            publishProgress("Connected");

            byte[] apduCmd = gatt.read(false);
            while (apduCmd.length > 0) {
                publishProgress(PROGRESS_TRANSFERRING + "\n" + ByteUtils.bytesToHex(apduCmd));
                final byte[] response = card.send(apduCmd);
                Log.d(TAG, "Sending: " + ByteUtils.bytesToHex(response));
                gatt.write(response);

                apduCmd = gatt.read(false);
                Log.d(TAG, "Received: " + ByteUtils.bytesToHex(apduCmd));
            }
            gatt.write(apduCmd); // acknowledge closing connection
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } catch (GattException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

        return RESULT_SUCCESS;
    }
}
