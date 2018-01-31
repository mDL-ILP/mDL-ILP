package com.ul.ts.products.mdlholder.connection.online;

import com.ul.ts.products.mdlholder.AbstractTransferActivity;
import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.connection.InterfaceAsyncTask;
import com.ul.ts.products.mdlholder.connection.TransferInterface;
import com.ul.ts.products.mdlholder.connection.bluetooth.GattAsyncTask;
import com.ul.ts.products.mdlholder.connection.descriptor.ConnectionDescriptor;
import com.ul.ts.products.mdlholder.connection.descriptor.ConnectionInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.OnlineConnectionInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.TransferInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.WDConnectionInfo;
import com.ul.ts.products.mdlholder.webapi.ReaderToken;
import com.ul.ts.products.mdlholder.webapi.WebAPI;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileOnline;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class OnlineTransfer implements TransferInterface {
    private final InterfaceAsyncTask lockUITask;
    private final AbstractTransferActivity mActivity;

    private final ExecutorService serviceOnline = Executors.newSingleThreadExecutor();

    public OnlineTransfer(AbstractTransferActivity activity, TransferInfo transferInfo, APDUInterface service) {
        mActivity = activity;

        lockUITask = new InterfaceAsyncTask(mActivity,
                "Setting up Online connection", "Retrieving token...");
        lockUITask.execute();


        // Retrieve token

        Callable<ReaderToken> registerTask = new WebAPI.RequestReaderTokenTask();
        Future<ReaderToken> f = serviceOnline.submit(registerTask);

        ReaderToken readerToken = null;
        try {
            readerToken = f.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }



        final ConnectionInfo connectionInfo = new OnlineConnectionInfo(readerToken.getToken());
        //final ConnectionInfo connectionInfo = new OnlineConnectionInfo("a830b0ab-f0c1-4886-a977-e4ba950c15ba");
        final ConnectionDescriptor descriptor = new ConnectionDescriptor(connectionInfo, transferInfo);
        mActivity.setupEngagement(descriptor, lockUITask);

    }

    public void startServer() {
    }

    @Override
    public void stopServer() {

        mActivity.finish();
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }




}
