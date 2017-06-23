package com.ul.ts.products.mdlreader.connection.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.ul.ts.products.mdlreader.AbstractLicenseActivity;
import com.ul.ts.products.mdlreader.connection.RemoteConnection;
import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.data.SocketAPDUInterface;

import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class WiFiDirectConnection extends RemoteConnection {

    private final AbstractLicenseActivity activity;
    private final String target;

    private IntentFilter intentFilter;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private PeerListener peerListener;
    private ConnectionListener connectionListener;

    private final static int PORT = 1337;

    private final String TAG = getClass().getName();

    public WiFiDirectConnection(AbstractLicenseActivity activity, String target) {
        this.activity = activity;
        this.target = target;
    }

    public void runSetupSteps() throws RemoteConnectionException {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(activity.getApplicationContext(), activity.getMainLooper(), null);

        peerListener = new PeerListener();
        connectionListener = new ConnectionListener();

        setupCompleted = true;
    }

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

    public void pause() {
        activity.unregisterReceiver(receiver);
    }

    public void resume() {
        receiver = new WiFiDirectBroadcastReceiver();
        activity.registerReceiver(receiver, intentFilter);
    }

    public void shutdown() {
        manager.cancelConnect(channel, null);
        manager.stopPeerDiscovery(channel, null);
        manager.removeGroup(channel, null);
        Log.d(TAG, "Wifi Direct shutdown");
    }

    public WifiP2pManager.ConnectionInfoListener getConnectionListener() {
        return connectionListener;
    }

    public WifiP2pManager.PeerListListener getPeerListener() {
        return peerListener;
    }

    private class ConnectionListener implements WifiP2pManager.ConnectionInfoListener {

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {

            Log.d(TAG, "Info available: "+info.toString()+". Stopping peer discovery.");
            manager.stopPeerDiscovery(channel, null);

            if (info.groupFormed && info.isGroupOwner) {
                Log.d(TAG, "I AM THE SERVER");
            } else if (info.groupFormed) {
                Log.d(TAG, "I AM THE CLIENT");
            }

            String address = info.groupOwnerAddress.getHostAddress();
            final ConnectToSocket connect = new ConnectToSocket(activity, address, PORT);

            // Delay for a second to make sure the server side is set up before we try to connect.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    connect.execute();

                    Socket s = null;
                    try {
                        s = connect.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(TAG, "Couldn't get socket connection", e);
                    }

                    if (s != null && s.isConnected()) {
                        activity.loadLicense(new SocketAPDUInterface(s));
                    } else {
                        // TODO(JS): error
                        activity.fail("Something went wrong");
                    }

                }
            }, 1000);
        }
    }

    private class PeerListener implements WifiP2pManager.PeerListListener {

        private final AtomicBoolean connectionAttemptInProgress = new AtomicBoolean(false);
        private final AtomicBoolean connected = new AtomicBoolean(false);

        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

            if (!connectionAttemptInProgress.compareAndSet(false, true)) {
                return;
            }

            if (connected.get()) {
                return;
            }

            for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                if (device.deviceAddress.equals(target)) {
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;
                    config.wps.setup = WpsInfo.PBC;
                    config.groupOwnerIntent = 0; // I want the other device to be the group owner (or 'server')

                    Log.d(TAG, "Trying to connect to "+device.deviceAddress+" "+device.deviceName+" Owner: "+device.isGroupOwner());

                    connect(config);
                    break;
                }
            }
        }

        public void connectionSuccess() {
            connected.set(true);
            connectionAttemptInProgress.set(false);
            manager.stopPeerDiscovery(channel, null);
        }

        public void connectionFailure() {
            connected.set(false);
            connectionAttemptInProgress.set(false);
        }
    }


    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                peerListener.connectionSuccess();
            }

            @Override
            public void onFailure(int reason) {
                peerListener.connectionFailure();
                Toast.makeText(activity, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * A BroadcastReceiver that notifies of important wifi p2p events.
     */
    private class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        public WiFiDirectBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // UI update to indicate wifi p2p status.

                Log.d(TAG, "P2P state changed.");
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling connection is notified with a
                // callback on PeerListListener.onPeersAvailable()
                manager.requestPeers(channel, getPeerListener());
                Log.d(TAG, "P2P peers changed.");
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "P2P connection changed.");

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    // we are connected with the other device, request connection
                    // info to find group owner IP
                    Log.d(TAG, "We are connected, yay! "+networkInfo.getState());
                    manager.requestConnectionInfo(channel, getConnectionListener());
                }

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "This device changed.");
            }
        }
    }
}
