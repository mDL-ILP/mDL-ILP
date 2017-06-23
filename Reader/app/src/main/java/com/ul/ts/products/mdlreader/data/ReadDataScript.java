package com.ul.ts.products.mdlreader.data;


import android.util.Log;

import com.ul.ts.products.mdlreader.utils.ByteUtils;
import com.ul.ts.products.mdlreader.utils.Bytes;
import com.ul.ts.products.mdlreader.utils.CardIOUtils;
import com.ul.ts.products.mdlreader.utils.HexStrings;

import java.io.IOException;
import java.util.Arrays;

class EmptyCallback implements ReadFileCallback {
    @Override
    public void afterReceive(int bytesRead) {    };
}

public class ReadDataScript {
    public static byte[] AID_MDL = HexStrings.fromHexString("A0 00 00 02 48 02 00");
    public static byte[] AID_MDL_ENGAGEMENT = HexStrings.fromHexString("A0 00 00 02 48 02 01");

    public static final int SUCCESS = 0x9000;
    private final byte[] AID;
    private final String TAG = this.getClass().getName();

    private final APDUInterface connection;

    public ReadDataScript(APDUInterface connection, byte[] AID) throws IOException {
        this.connection = connection;
        this.AID = AID;
        connect();
    }

    private void connect() throws IOException {
        byte[] selectCmd = Bytes.concatenate(
                new byte[] {0x00, (byte) 0xA4, 0x04, 0x00, (byte) this.AID.length},
                this.AID,
                new byte[] {0x00}
        );
        final byte[] selectResponse = connection.send(selectCmd);
        if (!Arrays.equals(selectResponse, HexStrings.fromHexString("90 00"))) {
            Log.w(TAG, "Select application failed, received " + HexStrings.toHexString(selectResponse) + "; ignoring for compatibility");
        }
    }

    public byte[] readFile(int id) throws IOException {
        return readFile(id, new EmptyCallback());
    }

    public byte[] readFile(int id, ReadFileCallback callback) throws IOException {
        //read
        byte p1 = (byte)(0x80 | id);
        byte p2 = (byte)(0x00);
        byte[] resp = connection.send(new byte[]{(byte)0x00, (byte)0xb0, p1, p2, (byte)0x00});
        if (getStatusWord(resp) == SUCCESS) {
            byte[] results = new byte[0];
            int offset;

            results = Bytes.concatenate(results, CardIOUtils.stripSW(resp));
            callback.afterReceive(results.length);

            while (getStatusWord(resp) == SUCCESS && resp.length > 2) {
                offset = results.length;
                p1 = (byte)(offset / 256);
                p2 = (byte)(offset % 256);
                resp = connection.send(new byte[]{(byte)0x00, (byte)0xb0, p1, p2, (byte)0x00});
                results = Bytes.concatenate(results, CardIOUtils.stripSW(resp));
                callback.afterReceive(results.length);
            }
            return results;
        }
        else {
            return null;
        }
    }

    public byte[] internalAuthenticate(byte[] random) throws IOException {
        byte[] command = Bytes.concatenate(Bytes.bytes("00 88 00 00 08"), random, Bytes.bytes("00"));
        return CardIOUtils.stripSW(connection.send(command));
    }

    public static int getStatusWord(byte[] bytes) {
        return ByteUtils.bytesToInt(Bytes.tail(bytes, 2), 0, 2);
    }
}
