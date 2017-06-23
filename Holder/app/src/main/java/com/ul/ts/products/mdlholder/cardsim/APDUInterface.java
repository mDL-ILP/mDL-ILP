package com.ul.ts.products.mdlholder.cardsim;

public interface APDUInterface {

    /**
     * Sends an ISO7816 APDU command and gets a response
     *
     * @param command the command to send to the interface
     * @return the response data to the command
     */
    byte[] send(byte[] command);
    void setMaxDataLength(int maxDataLength);
}
