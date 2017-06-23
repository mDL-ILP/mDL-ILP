package com.ul.ts.products.mdlholder.connection.hce;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.utils.HexStrings;

import java.util.concurrent.ArrayBlockingQueue;


public class SendingHandlerApduInterface extends Handler implements APDUInterface {
    final private String TAG = this.getClass().getName();

    private final ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(1);
    private final Messenger rcptTo;
    private final Messenger messageFrom;
    private final SendResponseApduInterface forwardTo;

    public SendingHandlerApduInterface(final Messenger rcptTo, final SendResponseApduInterface forwardTo) {
        this.rcptTo = rcptTo;
        this.messageFrom = new Messenger(this);
        this.forwardTo = forwardTo;
    }

    @Override
    public void handleMessage(final Message msg) {
        final byte[] response = msg.getData().getByteArray("response");
        Log.d(TAG, "Received: " + HexStrings.toHexString(response));
        this.forwardTo.sendResponseApdu(response);
    }

    @Override
    public byte[] send(final byte[] command) {
        byte[] result = null;

        final Bundle b = new Bundle();
        b.putByteArray("command", command);
        final Message m = new Message();
        m.replyTo = messageFrom;
        m.setData(b);

        try {
            Log.d(TAG, "Sending: " + m);
            rcptTo.send(m);
            Log.d(TAG, "Sent; waiting for response!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void setMaxDataLength(final int maxDataLength) {

    }
}
