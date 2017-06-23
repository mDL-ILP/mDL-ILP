package com.ul.ts.products.mdlholder.cardsim;

import com.ul.ts.products.mdlholder.utils.HexStrings;

public class ErrorApduInterface implements APDUInterface {
    @Override
    public byte[] send(final byte[] command) {
        return HexStrings.fromHexString("69 82"); // Security status not satisfied
    }

    @Override
    public void setMaxDataLength(final int maxDataLength) {

    }
}
