package com.ul.ts.products.mdlreader.connection.hce;

import android.nfc.tech.IsoDep;
import android.util.Log;

import com.ul.ts.products.mdlreader.data.APDUInterface;
import com.ul.ts.products.mdlreader.utils.ByteUtils;

import java.io.IOException;


public class IsoDepApduInterface implements APDUInterface {
    private final String TAG = this.getClass().getName();
    private static final int TAG_TIMEOUT = 3000; // ms
    private final IsoDep tag;

    public IsoDepApduInterface(final IsoDep tag) throws IOException {
        this.tag = tag;

        tag.connect();
        tag.setTimeout(TAG_TIMEOUT);
    }

    @Override
    public byte[] send(final byte[] command) throws IOException {
        Log.d(TAG, "CMD: " + ByteUtils.bytesToHex(command));
        final byte[] response = tag.transceive(command);
        Log.d(TAG, "RES: " + ByteUtils.bytesToHex(response));
        return response;
    }

    @Override
    public void close() {
        try {
            tag.close();
        } catch (IOException e) {
            // ignore -- the connection is already gone
            e.printStackTrace();
        }
    }
}
