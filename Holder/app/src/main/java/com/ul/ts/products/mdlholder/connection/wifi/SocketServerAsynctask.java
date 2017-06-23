package com.ul.ts.products.mdlholder.connection.wifi;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.cardsim.MDLSim;
import com.ul.ts.products.mdlholder.connection.CommunicationServerAsyncTask;
import com.ul.ts.products.mdlholder.utils.ByteUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class SocketServerAsynctask extends CommunicationServerAsyncTask {
    // https://docs.oracle.com/javacard/3.0.5/prognotes/extended_apdu_format.htm#JCPCL169
    // maximum length should be 1 + 1 + 2 + 3 + 65542. Round up to nearest 100.
    private final int APDU_BUFFER_SIZE = 65600;

    private final String TAG = getClass().getName();

    public SocketServerAsynctask(Activity activity, APDUInterface card) {
        super(activity, card);
    }

    @Override
    protected String doInBackground(Void... params) {
        try (ServerSocket serverSocket = new ServerSocket(1337)) {
            Log.d(TAG, "Server: Socket opened");

            try (Socket client = serverSocket.accept()) {
                Log.d(TAG, "Server: connected to "+client.getRemoteSocketAddress());
                publishProgress(PROGRESS_TRANSFERRING);

                InputStream in = client.getInputStream();
                OutputStream out = new DataOutputStream(client.getOutputStream());

                while (client.isConnected()) {
                    byte[] apduBuffer = new byte[APDU_BUFFER_SIZE];
                    int apduCmdLength;

                    apduCmdLength = in.read(apduBuffer); // blocks until something is received

                    if (apduCmdLength == -1) {
                        // stream ended, -1 returned from .read()
                        break;
                    }

                    // copy the command to a new buffer with the correct length
                    final byte[] apduCmd = Arrays.copyOf(apduBuffer, apduCmdLength);

                    Log.d(TAG, "Received: "+ ByteUtils.bytesToHex(apduCmd));

                    if (apduCmd.length > 0) {
                        final byte[] response = card.send(apduCmd);

                        Log.d(TAG, "Sending: "+ ByteUtils.bytesToHex(response));

                        out.write(response); // send response back
                        out.flush();
                    }
                }

                Log.d(TAG, "Server: connection closed");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return "Error: " + e.getMessage();
        }

        return RESULT_SUCCESS;
    }
}
