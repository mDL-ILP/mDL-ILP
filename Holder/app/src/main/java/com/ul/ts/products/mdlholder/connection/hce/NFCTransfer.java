package com.ul.ts.products.mdlholder.connection.hce;

import android.content.Intent;
import android.os.Messenger;
import android.util.Log;

import com.ul.ts.products.mdlholder.AbstractTransferActivity;
import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.connection.InterfaceAsyncTask;
import com.ul.ts.products.mdlholder.connection.TransferInterface;
import com.ul.ts.products.mdlholder.connection.descriptor.ConnectionDescriptor;
import com.ul.ts.products.mdlholder.connection.descriptor.ConnectionInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.NFCConnectionInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.TransferInfo;

public class NFCTransfer implements TransferInterface {
    private final String TAG = this.getClass().getName();

    private final AbstractTransferActivity activity;
    private final Intent mdlService;

    public NFCTransfer(final AbstractTransferActivity activity, final TransferInfo transferInfo, final APDUInterface service) {
        this.activity = activity;
        this.mdlService = new Intent(activity, MdlApduService.class);
        mdlService.putExtra("type", "transfer");

        final InterfaceAsyncTask lockUITask = new InterfaceAsyncTask(activity,
                "Setting up Bluetooth", "Activating hardware...");
        lockUITask.execute();

        service.setMaxDataLength(255);


        final Messenger messenger = new Messenger(new ReceivingHandler(service));
        mdlService.putExtra("messenger", messenger);
        activity.startService(mdlService);

        final ConnectionInfo connectionInfo = new NFCConnectionInfo();
        final ConnectionDescriptor descriptor = new ConnectionDescriptor(connectionInfo, transferInfo);
        activity.setupEngagement(descriptor, lockUITask);
    }

    @Override
    public void stopServer() {
        Log.d(TAG, "stopServer");
        //activity.startService(mdlService);
        activity.stopService(mdlService);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}
