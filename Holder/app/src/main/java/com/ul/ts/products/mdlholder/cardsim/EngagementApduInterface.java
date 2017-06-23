package com.ul.ts.products.mdlholder.cardsim;


import com.ul.ts.products.mdlholder.utils.HexStrings;

import java.util.HashMap;

public class EngagementApduInterface extends BasicCard {
    public EngagementApduInterface(byte[] engagementData, final int maxDataLength) {
        super(maxDataLength);
        fileContent = new HashMap<>();
        fileContent.put((byte) 0x00, engagementData);
    }

    @Override
    protected byte[] getAID() {
        return HexStrings.fromHexString("A0 00 00 02 48 02 01");
    }

    @Override
    boolean mayReadFile(final byte currentFile) {
        return true;
    }
}
