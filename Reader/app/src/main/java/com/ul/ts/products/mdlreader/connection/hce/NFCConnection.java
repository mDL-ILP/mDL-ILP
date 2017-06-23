package com.ul.ts.products.mdlreader.connection.hce;

import android.app.Activity;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import com.ul.ts.products.mdlreader.AbstractLicenseActivity;
import com.ul.ts.products.mdlreader.connection.RemoteConnection;
import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.utils.HexStrings;

import java.io.IOException;
import java.util.Arrays;

abstract class NFCConnection extends RemoteConnection implements NfcAdapter.ReaderCallback {
    protected final String TAG = this.getClass().getName();
    protected final Activity activity;
    protected NfcAdapter adapter;


    public NFCConnection(final Activity activity) {
        this.activity = activity;
    }

    @Override
    public void runSetupSteps() throws RemoteConnectionException {
        adapter = NfcAdapter.getDefaultAdapter(activity);

        if (adapter == null) {
            throw new RemoteConnectionException("NFC is not supported on this device.", false);
        }

        setupCompleted = true;
    }

    @Override
    public void resume() {}

    @Override
    public void findPeers() {
        adapter.enableReaderMode(
                activity,
                this,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null
        );
        Log.d(TAG, "enabled Reader Mode");
    }

    @Override
    public void pause() {
        adapter.disableReaderMode(activity);
        Log.d(TAG, "disabled Reader Mode");
    }

    @Override
    public void shutdown() {
        pause();
    }

    @Override
    public void onTagDiscovered(final Tag tag) {
        Log.d(TAG, "Tag found: " + tag.toString());
        Log.d(TAG, "Id: " + HexStrings.toHexString(tag.getId()));
        for (String tech: tag.getTechList()) {
            Log.d(TAG, "Tech: " + tech);
        }

        if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.IsoDep")) {
            IsoDepApduInterface apduInterface;
            try {
                apduInterface = new IsoDepApduInterface(IsoDep.get(tag));
            } catch (IOException e) {
                fail(e.getMessage());
                e.printStackTrace();
                return;
            }

            dispatchLoadTask(apduInterface);
        }
    }



    protected abstract void fail(String message);

    protected abstract void dispatchLoadTask(IsoDepApduInterface apduInterface);
}
