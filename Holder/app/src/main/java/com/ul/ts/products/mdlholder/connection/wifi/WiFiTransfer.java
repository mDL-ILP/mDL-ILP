package com.ul.ts.products.mdlholder.connection.wifi;

import com.ul.ts.products.mdlholder.AbstractTransferActivity;
import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.connection.InterfaceAsyncTask;
import com.ul.ts.products.mdlholder.connection.TransferInterface;
import com.ul.ts.products.mdlholder.connection.descriptor.ConnectionDescriptor;
import com.ul.ts.products.mdlholder.connection.descriptor.ConnectionInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.TransferInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.WDConnectionInfo;


public class WiFiTransfer implements TransferInterface {
    private final InterfaceAsyncTask lockUITask;
    private final SocketServerAsynctask transferTask;
    private final AbstractTransferActivity mActivity;
    private final WiFiDirectConnection connection;
    private final TransferInfo transferInfo;

    public WiFiTransfer(AbstractTransferActivity activity, TransferInfo transferInfo, APDUInterface service) {
        mActivity = activity;
        lockUITask = new InterfaceAsyncTask(mActivity,
                "Setting up Wi-Fi Direct", "Activating hardware...");
        lockUITask.execute();

        this.transferInfo = transferInfo;

        service.setMaxDataLength(16384);

        connection = new WiFiDirectConnection(mActivity, this);
        transferTask = new SocketServerAsynctask(mActivity, service);
    }

    public void startServer() {
        transferTask.execute();
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

    public void setConnectionSpecificInfo(String deviceAddress) {
        final ConnectionInfo connectionInfo = new WDConnectionInfo(deviceAddress);
        final ConnectionDescriptor descriptor = new ConnectionDescriptor(connectionInfo, transferInfo);
        mActivity.setupEngagement(descriptor, lockUITask);
    }


}
