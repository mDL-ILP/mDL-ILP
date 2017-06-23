package com.ul.ts.products.mdlholder.connection.hce;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ul.ts.products.mdlholder.cardsim.APDUInterface;

import java.util.concurrent.ArrayBlockingQueue;


public class ReceivingHandler extends Handler {
    final private String TAG = this.getClass().getName();
    final private APDUInterface wrappedInterface;

    public ReceivingHandler(final APDUInterface wrappedInterface) {
        this.wrappedInterface = wrappedInterface;
    }

    @Override
    public void handleMessage(final Message msg) {
        Log.d(TAG, "handleMessage: " + msg);
        final byte[] command = msg.getData().getByteArray("command");
        final byte[] response = wrappedInterface.send(command);

        final Bundle b = new Bundle();
        b.putByteArray("response", response);
        final Message m = new Message();
        m.setData(b);

        try {
            Log.d(TAG, "Returning: " + m);
            msg.replyTo.send(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
