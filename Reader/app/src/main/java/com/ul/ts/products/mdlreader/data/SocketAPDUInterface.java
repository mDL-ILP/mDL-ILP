package com.ul.ts.products.mdlreader.data;

import android.util.Log;

import com.ul.ts.products.mdlreader.utils.Bytes;
import com.ul.ts.products.mdlreader.utils.Preconditions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class SocketAPDUInterface implements APDUInterface {
    // https://docs.oracle.com/javacard/3.0.5/prognotes/extended_apdu_format.htm#JCPCL169
    // maximum length should be 1 + 1 + 2 + 3 + 65542. Round up to nearest 100.
    private final int APDU_BUFFER_SIZE = 65600;
    private final InputStream in;
    private final OutputStream out;
    private final Socket socket;

    /**
     * @param socket an OPEN socket that we can use the input and output streams from
     */
    public SocketAPDUInterface(Socket socket) {
        this.socket = socket;
        Preconditions.check(socket.isConnected(), "Socket needs to be connected");

        try {
            in = socket.getInputStream();
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot use streams of socket.", e);
        }
    }

    @Override
    public byte[] send(byte[] command) {

        Log.d(getClass().getName(), "Sending: "+ Bytes.hexString(command));

        try {
            // send the command
            out.write(command);
            out.flush();

            // get the response
            byte[] apduBuffer = new byte[APDU_BUFFER_SIZE];
            int apduCmdLength;
            apduCmdLength = in.read(apduBuffer); // blocks until something is received

            // copy the command to a new buffer with the correct length
            byte[] response = Arrays.copyOf(apduBuffer, apduCmdLength);

            Log.d(getClass().getName(), "Received: "+ Bytes.hexString(response));

            return response;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(getClass().getName(), "Failed to send command to server", e);
        }

        return null;
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            // socket is already closed
        }
    }
}
