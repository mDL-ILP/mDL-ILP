package com.ul.ts.products.mdlholder.connection.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.ul.ts.products.mdlholder.R;
import com.ul.ts.products.mdlholder.connection.RemoteConnection;

public class WiFiDirectConnection implements RemoteConnection {

    private final Activity activity;
    private final WiFiTransfer transfer;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;

    private IntentFilter intentFilter = new IntentFilter();

    private WifiP2pManager.ChannelListener channelListener;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    private final String TAG = getClass().getName();

    public WiFiDirectConnection(Activity activity, WiFiTransfer transfer) {
        this.activity = activity;
        this.transfer = transfer;

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(activity.getApplicationContext(), activity.getMainLooper(), channelListener);

        channelListener = new ChannelListener();
        connectionInfoListener = new ConnectionInfoListener();
    }

    @Override
    public void pause() {
        activity.unregisterReceiver(receiver);
    }

    @Override
    public void resume() {
        receiver = new WiFiDirectBroadcastReceiver();
        activity.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void shutdown() {
        manager.cancelConnect(channel, null);
        manager.stopPeerDiscovery(channel, null);
        manager.removeGroup(channel, null);
        Log.d(TAG, "Shutdown Wifi Direct");
    }

    @Override
    public void findPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Discovery Initiated");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Discovery Failed : " + reasonCode);
            }
        });
    }

    private class ConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            // After the group negotiation, we assign the group owner as the file
            // server. The file server is single threaded, single connection server
            // socket.
            Log.d(TAG, "Info available: "+info.toString());

            if (info.groupFormed && info.isGroupOwner) {
                Log.d(TAG, "I AM THE SERVER");
                transfer.startServer();
            } else if (info.groupFormed) {
                Log.d(TAG, "I AM THE CLIENT");
            }
        }
    }

    private class ChannelListener implements WifiP2pManager.ChannelListener {

        @Override
        public void onChannelDisconnected() {
            // Show an error dialog to warn the user of a critical failure and ask how to proceed.

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.connection_problem_title);
            builder.setMessage(R.string.connection_problem_wifi_fail);
            builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e(TAG, "Channel lost. Trying again");
                    // kill everything first
                    pause();
                    shutdown();
                    // start from the start again
                    resume();
                    findPeers();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e(TAG, "Channel is probably lost permanently. Try Disable/Re-Enable P2P.");
                    pause();
                    shutdown();
                    transfer.stopServer();
                }
            });
            builder.create().show();
        }
    }

    /**
     * A BroadcastReceiver that notifies of important wifi p2p events.
     */
    private class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        public WiFiDirectBroadcastReceiver() {
            super();
        }

        /*
         * (non-Javadoc)
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

                // UI update to indicate wifi p2p status.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi Direct mode is enabled
                    //activity.setIsWifiP2pEnabled(true);
                } else {
                    //activity.setIsWifiP2pEnabled(false);
                }
                Log.d(TAG, "P2P state changed");
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "P2P peers changed");
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.d(getClass().getName(), "P2P connection changed");

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection
                    // info to find group owner IP
                    manager.requestConnectionInfo(channel, connectionInfoListener);
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // NOTE(JS): Getting the hardware id of this device and passing it to the activity so it can be shown
                WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

                transfer.setConnectionSpecificInfo(thisDevice.deviceAddress);
                Log.d(TAG, "This device changed");
            }
        }
    }
}
