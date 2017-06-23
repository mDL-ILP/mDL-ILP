package com.ul.ts.products.mdlreader.connection;

public abstract class RemoteConnection {
    /**
     * After the RemoteConnection object has been instantiated, the caller should call runSetupSteps();
     * runSetupSteps() returns null (on success) or an RemoteConnectionException on failure. The Exception
     * indicates whether whether the exception is
     * it should initialize the
     * communication channel, and should start listening to events related to the
     * communication channel.
     *
     * pause() and resume() should pause and resume listening (to be used when the activity is
     * hidden/shown)
     *
     * shutdown() should fully shut down the listener.
     */
    public boolean setupCompleted = false;
    public abstract void runSetupSteps() throws RemoteConnectionException;

    public abstract void pause();
    public abstract void resume();
    public abstract void shutdown();

    /**
     * Start Peer discovery. Once the peer has been found,
     * activity.loadLicense(apduInterface) should be called with an applicable interface;
     */
    public abstract void findPeers();
}
