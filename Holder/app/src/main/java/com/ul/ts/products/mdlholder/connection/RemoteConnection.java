package com.ul.ts.products.mdlholder.connection;

public interface RemoteConnection {
    /**
     * After the RemoteConnection object has been instantiated, it should initialize the
     * communication channel, and should start listening to events related to the
     * communication channel.
     *
     * pause() and resume() should pause and resume listening (to be used when the activity is
     * hidden/shown)
     *
     * shutdown() should fully shut down the listener.
     */
    void pause();
    void resume();
    void shutdown();

    /**
     * Start Peer discovery. Once the peer has been found, transfer.startServer()
     * should be called, which will set up the further connection.
     */
    void findPeers();
}
