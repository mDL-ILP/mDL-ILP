package com.ul.ts.products.mdlholder.connection.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.ul.ts.products.mdlholder.AbstractTransferActivity;
import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.connection.InterfaceAsyncTask;
import com.ul.ts.products.mdlholder.connection.TransferInterface;
import com.ul.ts.products.mdlholder.connection.descriptor.BLEConnectionInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.ConnectionDescriptor;
import com.ul.ts.products.mdlholder.connection.descriptor.ConnectionInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.TransferInfo;


public class BTTransfer implements TransferInterface {
    private final InterfaceAsyncTask lockUITask;
    private final GattAsyncTask transferTask;
    private final AbstractTransferActivity mActivity;
    private final BTConnection connection;
    private boolean started = false;

    public BTTransfer(AbstractTransferActivity activity, TransferInfo transferInfo, APDUInterface service) {
        mActivity = activity;
        lockUITask = new InterfaceAsyncTask(mActivity,
                "Setting up Bluetooth", "Activating hardware...");
        lockUITask.execute();

        service.setMaxDataLength(500);

        connection = new BTConnection(mActivity, this);
        transferTask = new GattAsyncTask(mActivity, service);

        final ConnectionInfo connectionInfo = new BLEConnectionInfo(connection.getAntiCollisionIdentifier());
        final ConnectionDescriptor descriptor = new ConnectionDescriptor(connectionInfo, transferInfo);
        mActivity.setupEngagement(descriptor, lockUITask);
    }

    public synchronized void startServer(BluetoothDevice device) {
        if (! started ) {
            started = true;
            transferTask.setDevice(device);
            transferTask.execute();
        }
    }

    @Override
    public void stopServer() {
        lockUITask.dismiss();
        mActivity.finish();
    }

    @Override
    public void resume() {
        connection.resume();
        connection.findPeers();
    }

    @Override
    public void pause() {
        connection.pause();
        connection.shutdown();
    }
}
