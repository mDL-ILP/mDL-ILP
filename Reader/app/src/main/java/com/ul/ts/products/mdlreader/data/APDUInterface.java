package com.ul.ts.products.mdlreader.data;

import java.io.IOException;

public interface APDUInterface {

    /**
     * Sends an ISO7816 APDU command and gets a response
     *
     * @param command the command to send to the interface
     * @return the response data to the command
     */
    byte[] send(byte[] command) throws IOException;
    void close();
}
