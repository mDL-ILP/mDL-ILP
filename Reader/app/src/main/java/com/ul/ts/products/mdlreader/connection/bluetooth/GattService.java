package com.ul.ts.products.mdlreader.connection.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ul.ts.products.mdlreader.AbstractLicenseActivity;
import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.data.APDUInterface;
import com.ul.ts.products.mdlreader.utils.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.ul.ts.products.mdlreader.utils.BTUtils.deviceString;


public class GattService extends BluetoothGattServerCallback implements APDUInterface {
    private final String TAG = getClass().getName();

    private final AbstractLicenseActivity activity;
    private final BluetoothGattServer gattServer;
    private BluetoothGattService bluetoothGattService;
    private BluetoothDevice currentDevice;

    private boolean writeReceived = false;
    private boolean dataWaiting = false; /* if no data is waiting, ignore read requests */

    private ByteArrayOutputStream apduRecvStream;

    private static final BluetoothGattCharacteristic apduCharacteristic =
            new BluetoothGattCharacteristic(
                    Constants.APDU_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
            );


    public GattService(AbstractLicenseActivity activity, BluetoothManager bluetoothManager) throws RemoteConnectionException {
        Log.d(TAG, "Create " + this.toString() + " for activity " + activity.toString());
        this.activity = activity;

        gattServer = bluetoothManager.openGattServer(activity, this);
        if (gattServer == null) {
            throw new RemoteConnectionException("Could not create GattServer", false);
        }
        bluetoothGattService = new BluetoothGattService(
                Constants.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
        );
        bluetoothGattService.addCharacteristic(apduCharacteristic);
    }

    public void startAdvertising() {
        if (gattServer.getService(bluetoothGattService.getUuid()) == null) {
            Log.d(TAG, "started GattService " + bluetoothGattService.getUuid());
            gattServer.addService(bluetoothGattService);
        }
    }

    public void stopAdvertising() {
        if (gattServer.getService(bluetoothGattService.getUuid()) != null) {
            gattServer.removeService(bluetoothGattService);
            Log.d(TAG, "stopped GattService " + bluetoothGattService.getUuid());
        }
    }

    /* callbacks */

    /**
     * Connection State callback. This is called any time a device connects/disconnects, etc. We
     * only handle a single device at a time, and ignore any other connection requests. When a
     * connection is established, we immediately send the APDUInterface (i.e., this object) to the
     * waiting Activity.
     */
    @Override
    public synchronized void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        Log.v(TAG, "onConnectionStateChange: " + deviceString(device) + ", " + status + ", " + newState);
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
            /* new device */
            if (currentDevice == null) {
                currentDevice = device;
                Log.v(TAG, "Connected to device: " + deviceString(device));

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.loadLicense(GattService.this);
                    }
                } );

            } else {
                Log.v(TAG, "Already connected to: " + deviceString(currentDevice) + ", ignoring new device " + deviceString(device));
            }
        } else if (status != BluetoothGatt.GATT_SUCCESS || newState == BluetoothGatt.STATE_DISCONNECTED){
            if (!currentDevice.equals(device)) {
                Log.v(TAG, "Ignoring state change of device " + deviceString(device));
            } else {
                Log.v(TAG, "Disconnecting from " + deviceString(device));
                currentDevice = null;
                notifyAll();
            }
        }
    }

    /**
     * Read requests can be performed in several steps, with increasing offsets (e.g. when the MTU
     * is very small). We need to handle this manually.
     *
     * We only support reading from the apduCharacteristic; if any other characteristic is read,
     * we return an error.
     */
    @Override
    public synchronized void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattCharacteristic characteristic) {

        Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());

        if (characteristic.getUuid() != apduCharacteristic.getUuid()) {
            Log.d(TAG, "Not the APDU characteristic, returning error.");
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_READ_NOT_PERMITTED, offset, null);
            return;
        }

        while (! dataWaiting) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        byte[] value = apduCharacteristic.getValue();

        if (offset > value.length) {
            Log.d(TAG, "Offset " + String.valueOf(offset) + " is larger than length " + String.valueOf(value.length));
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset, null);
        } else {
            byte[] part = Arrays.copyOfRange(value, offset, value.length);
            Log.d(TAG, "Sending blob starting at offset " + String.valueOf(offset) + ": " + ByteUtils.bytesToHex(part));

            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, part);
        }
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        Log.d(TAG, "Received write to offset " + offset + " with value " + ByteUtils.bytesToHex(value));
        if (offset == 0) {
            apduRecvStream = new ByteArrayOutputStream();
        }

        int status = BluetoothGatt.GATT_SUCCESS;
        if (offset != apduRecvStream.size()) {
            Log.e(TAG, "Offset " + String.valueOf(offset) + " is not stream size " + String.valueOf(apduRecvStream.size()));
            status = BluetoothGatt.GATT_INVALID_OFFSET;
        } else {
            try {
                apduRecvStream.write(value);
            } catch (IOException e) {
                status = BluetoothGatt.GATT_FAILURE;
            }
        }

        if (responseNeeded) {
            Log.v(TAG, "Sent response: " + String.valueOf(status));
            gattServer.sendResponse(device, requestId, status, offset, value);
        }

        if (! preparedWrite) {
            onExecuteWrite(device, requestId, true);
        }
    }

    @Override
    public synchronized void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        dataWaiting = false;

        apduCharacteristic.setValue(apduRecvStream.toByteArray());
        apduRecvStream = null;

        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);

        writeReceived = true;
        notifyAll();
    }

    /* Actual public, synchronised, interface starts here */

    @Override
    public synchronized byte[] send(byte[] command) throws IOException {
        /* send a command:
               [server] set the characteristic value
               [server] send a characteristic changed notification
               [client] retrieve the command
               [client] process the command
               [client] sent the response as new characteristic value
               [server] return the new char. value to the caller
         */
        if (currentDevice == null) {
            throw new IOException("Device disconnected");
        }

        Log.d(TAG, "CMD: " + ByteUtils.bytesToHex(command));
        apduCharacteristic.setValue(command);
        dataWaiting = true;
        notifyAll();

        gattServer.notifyCharacteristicChanged(currentDevice, apduCharacteristic, false);
        Log.d(TAG, "[notified changed, waiting for response]");

        writeReceived = false;

        while(!writeReceived) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (currentDevice == null) { // disconnected
                throw new IOException("Device disconnected");
            }
        }

        /* wait for read and write */
        Log.d(TAG, "RES: " + ByteUtils.bytesToHex(apduCharacteristic.getValue()));
        return apduCharacteristic.getValue();
    }

    @Override
    public void close() {
        // send empty message to communicate end of conversation to client
        try {
            send(new byte[] {});
        } catch (IOException e) {
            // ignore -- the connection is already gone
            e.printStackTrace();
        }
        // then de-register the gatt server
        gattServer.clearServices();
        gattServer.close();

        currentDevice = null;

        Log.d(TAG, "Fully shut down " + this.toString());
    }
}
