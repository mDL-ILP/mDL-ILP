package com.ul.ts.products.mdlholder.data;

import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.utils.ByteUtils;
import com.ul.ts.products.mdlholder.utils.Bytes;
import com.ul.ts.products.mdlholder.utils.CardIOUtils;

import java.io.IOException;

public class ReadDataScript {

    public static final int SUCCESS = 0x9000;

    private final APDUInterface connection;

    public ReadDataScript(APDUInterface connection) {
        this.connection = connection;
    }

    public byte[] readFile(int id) throws IOException {
        //read
        byte p1 = (byte)(0x80 | id);
        byte p2 = (byte)(0x00);
        byte[] resp = connection.send(new byte[]{(byte)0x00, (byte)0xb0, p1, p2, (byte)0x00});
        if (getStatusWord(resp) == SUCCESS) {
            byte[] results = new byte[0];
            int offset;

            results = Bytes.concatenate(results, CardIOUtils.stripSW(resp));

            while (getStatusWord(resp) == SUCCESS && resp.length > 2)
            {
                offset = results.length;
                p1 = (byte)(offset / 256);
                p2 = (byte)(offset % 256);
                resp = connection.send(new byte[]{(byte)0x00, (byte)0xb0, p1, p2, (byte)0x00});
                results = Bytes.concatenate(results, CardIOUtils.stripSW(resp));
            }
            return results;
        }
        else {
            return null;
        }
    }

    public byte[] internalAuthenticate(byte[] random) {
        byte[] command = Bytes.concatenate(Bytes.bytes("00 88 00 00 08"), random, Bytes.bytes("00"));
        return  CardIOUtils.stripSW(connection.send(command));
    }

    public static int getStatusWord(byte[] bytes) {
        return ByteUtils.bytesToInt(Bytes.tail(bytes, 2), 0, 2);
    }
}
