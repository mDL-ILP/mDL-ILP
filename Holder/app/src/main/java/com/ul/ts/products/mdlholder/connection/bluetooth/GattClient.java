package com.ul.ts.products.mdlholder.connection.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.ul.ts.products.mdlholder.utils.HexStrings;

import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

public class GattClient extends BluetoothGattCallback {
    private final String TAG = getClass().getName() + "@" + Integer.toHexString(this.hashCode());
    public final boolean VDBG = true;


    /* internal state handling */
    static int MTU_UNKNOWN = -1;

    static int MESSAGE_SERVICES_DISCOVERED = 1;
    static int MESSAGE_READ = 2;
    static int MESSAGE_WRITE = 3;
    static int MESSAGE_CHARACTERISTIC_CHANGED = 4;

    int lastMessage = -1;
    BluetoothGattCharacteristic contents;
    int status = -1;

    int connectionState = BluetoothProfile.STATE_DISCONNECTED;
    int mtu = MTU_UNKNOWN;

    /* interfaces */
    private Activity mActivity;
    private BluetoothGatt mBtGatt;
    private BluetoothGattCharacteristic mCharacteristic;

    public GattClient(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /* callbacks */

    @Override
    public synchronized void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, "onConnectionStateChange: " + newState);
        connectionState = newState;
        notifyAll();
    }

    @Override
    public synchronized void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        Log.d(TAG, "onMtuChanged: " + mtu);
        this.mtu = mtu;
        notifyAll();
    }

    @Override
    public synchronized void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onServicesDiscovered");
        this.lastMessage = MESSAGE_SERVICES_DISCOVERED;
        this.status = status;
        notifyAll();
    }

    @Override
    public synchronized void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicRead: " + characteristic.getUuid() + " -> [" + HexStrings.toHexString(characteristic.getValue()) + "] (status: " + status + ")");
        this.lastMessage = MESSAGE_READ;
        this.contents = characteristic;
        this.status = status;
        notifyAll();
    }

    @Override
    public synchronized void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicWrite: " + characteristic.getUuid());
        this.lastMessage = MESSAGE_WRITE;
        this.contents = characteristic;
        this.status = status;
        notifyAll();
    }

    @Override
    public synchronized void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged: " + characteristic.getUuid());
        this.lastMessage = MESSAGE_CHARACTERISTIC_CHANGED;
        this.contents = characteristic;
        notifyAll();
    }

    @Override
    public synchronized void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
    }

    /* synchronous methods */
    private synchronized void waitUntilConnected() throws InterruptedException {
        if (VDBG) { Log.d(TAG, "waitUntilConnected: [CMD]"); }
        while (connectionState != BluetoothProfile.STATE_CONNECTED) {
            wait();
        }
        if (VDBG) { Log.d(TAG, "waitUntilConnected: [DONE]"); }
    }

    private synchronized void setConnectionParameters() throws InterruptedException {
        if (VDBG) { Log.d(TAG, "setConnectionParameters: [CMD]"); }
        mBtGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        mBtGatt.requestMtu(517);
        while (mtu == MTU_UNKNOWN) {
            wait();
        }
        if (VDBG) { Log.d(TAG, "setConnectionParameters: [DONE]"); }
    }

    private synchronized void discoverServices() throws InterruptedException {
        if (VDBG) { Log.d(TAG, "discoverServices: [CMD]"); }
        while (lastMessage != MESSAGE_SERVICES_DISCOVERED) {
            mBtGatt.discoverServices();
            wait(1000);
        }
        if (VDBG) {
            Log.d(TAG, "discoverServices: [DONE] ");
            for (BluetoothGattService s : mBtGatt.getServices()) {
                Log.d(TAG, "discoverServices: found " + s.getUuid());
                for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                    Log.d(TAG, "--> characteristic: " + c.getUuid() + ":" + String.format("%x", c.getInstanceId()));
                }
            }
        }
        lastMessage = -1;
    }

    private synchronized void waitUntilCharacteristicChanged() throws InterruptedException {
        if (VDBG) { Log.d(TAG, "waitUntilCharacteristicChanged: [CMD]"); }
        while (lastMessage != MESSAGE_CHARACTERISTIC_CHANGED) {
            wait();
        }
        if (VDBG) { Log.d(TAG, "waitUntilCharacteristicChanged: [DONE]"); }
        lastMessage = -1;
    }


    private synchronized void readCharacteristic(BluetoothGattCharacteristic characteristic) throws InterruptedException, GattException {
        if (VDBG) { Log.d(TAG, "readCharacteristic: [CMD]"); }
        if (!mBtGatt.readCharacteristic(characteristic)) {
            throw new GattException("Could not read characteristic.");
        };
        while (lastMessage != MESSAGE_READ) {
            wait();
        }
        if (VDBG) { Log.d(TAG, "readCharacteristic: [DONE] " + HexStrings.toHexString(characteristic.getValue())); }
        lastMessage = -1;
    }



    private synchronized void writeCharacteristic(BluetoothGattCharacteristic c, byte[] data) throws InterruptedException {
        if (VDBG) { Log.d(TAG, "writeCharacteristic: [CMD] " + HexStrings.toHexString(c.getValue())); }
        c.setValue(data);
        mBtGatt.writeCharacteristic(c);
        while (lastMessage != MESSAGE_WRITE) {
            wait();
        }
        if (VDBG) { Log.d(TAG, "writeCharacteristic: [DONE]"); }
        lastMessage = -1;
    }

    /* public interface, only calls the synchronized methods above */

    /**
     * Synchronously connect to the given bluetooth device, set connection parameters (MTU)
     * and discover the mDL SERVICE_UUID and APDU_UUID.
     *
     * @param device The device to connect to. The device must support the mDL service and characteristic.
     * @throws InterruptedException When the thread is interrupted while waiting for a BT response.
     */
    public void connect(BluetoothDevice device) throws InterruptedException, GattException {
        if (VDBG) { Log.d(TAG, "connect: [CMD] " + device.getAddress()); }
        this.mBtGatt = device.connectGatt(this.mActivity, false, this);
        waitUntilConnected();
        setConnectionParameters();
        discoverServices();

        BluetoothGattService service = mBtGatt.getService(Constants.SERVICE_UUID);

        if (service == null) {
            Log.d(TAG, "SERVICES FOUND");
            for (BluetoothGattService s: mBtGatt.getServices()) {
                Log.d(TAG, "Service found: " + s.getUuid());
            }
            Log.d(TAG, "END OF LIST");
            throw new GattException("Could not find service " + Constants.SERVICE_UUID);
        }
        mCharacteristic = service.getCharacteristic(Constants.APDU_UUID);
        if (mCharacteristic == null) {
            throw new GattException("Could not find characteristic " + Constants.APDU_UUID);
        }

        if (VDBG) { Log.d(TAG, "connect: [DONE] "); }
    }

    /**
     * Read a message from the connection (i.e., read the current value of the wrapped characteristic).
     *
     * @param waitUntilChanged Wait until a CharacteristicChanged message is sent before reading.
     * @return Message (value of the characteristic)
     * @throws InterruptedException When the thread is interrupted while waiting for a BT response.
     * @throws GattException When the BT response is an error.
     */
    public byte[] read(boolean waitUntilChanged) throws InterruptedException, GattException {
        if (VDBG) { Log.d(TAG, "read: [CMD] " + (waitUntilChanged ? "waitUntilChanged" : "")); }
        if (waitUntilChanged) {
            waitUntilCharacteristicChanged();
        }
        readCharacteristic(mCharacteristic);

        if (status != GATT_SUCCESS) {
            throw new GattException(status);
        }

        if (VDBG) { Log.d(TAG, "read: [DONE] "); }
        return mCharacteristic.getValue();

    }

    public byte[] read() throws InterruptedException, GattException {
        return read(true);
    }

    /**
     * Write a message to the connection (i.e., write the current value of the wrapped characteristic).
     *
     * @param data Message (value of the characteristic)
     * @throws InterruptedException When the thread is interrupted while waiting for a BT response.
     * @throws GattException When the BT response is an error.
     */
    public void write(byte[] data) throws InterruptedException, GattException {
        if (VDBG) { Log.d(TAG, "write: [CMD] " + HexStrings.toHexString(data)); }
        this.writeCharacteristic(
                this.mCharacteristic,
                data
        );

        if (status != GATT_SUCCESS) {
            throw new GattException(status);
        }

        if (VDBG) { Log.d(TAG, "write: [DONE] "); }
    }

}
