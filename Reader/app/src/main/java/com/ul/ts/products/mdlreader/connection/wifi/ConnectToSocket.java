package com.ul.ts.products.mdlreader.connection.wifi;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectToSocket extends AsyncTask<Void, Void, Socket> {

    private static final String TAG = "ConnectToSocket";

    private ProgressDialog progressDialog;
    private final String host;
    private final int port;
    private final static int TIMEOUT = 5000;

    public ConnectToSocket(Context context, String host, int port) {
        progressDialog = new ProgressDialog(context);
        this.host = host;
        this.port = port;
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setTitle("Communicating to holder app...");
        progressDialog.setMessage("Connecting to "+host+":"+port);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    @Override
    protected Socket doInBackground(Void... voids) {

        try {
            final Socket socket = new Socket();
            Log.d(TAG, "Attempting connection to "+ host +":"+ port);
            socket.connect(new InetSocketAddress(host, port), TIMEOUT);
            Log.d(TAG, "Connection made");

            if(socket.isConnected()) {
                return socket;
            } else {
                Log.e(TAG, "Socket not connected");
            }
        } catch (IOException e) {
            Log.e(TAG, "Socket failed", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Socket socket) {
        progressDialog.dismiss();
    }
}

