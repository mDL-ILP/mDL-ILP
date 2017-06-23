package com.ul.ts.products.mdlholder.connection.hce;

import android.app.Service;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Messenger;
import android.support.annotation.IntDef;
import android.util.Log;

import com.ul.ts.products.mdlholder.cardsim.APDUInterface;
import com.ul.ts.products.mdlholder.cardsim.EngagementApduInterface;
import com.ul.ts.products.mdlholder.cardsim.ErrorApduInterface;
import com.ul.ts.products.mdlholder.cardsim.MDLSim;
import com.ul.ts.products.mdlholder.utils.HexStrings;

import java.util.Arrays;

public class MdlApduService extends HostApduService implements SendResponseApduInterface {
    private final String TAG = getClass().getName();
    private APDUInterface mdlSim = new ErrorApduInterface();

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent.getStringExtra("type").equals("engagement")) {
            mdlSim = new EngagementApduInterface(intent.getByteArrayExtra("engagementData"), 255);
        } else {
            final Messenger messenger = intent.getParcelableExtra("messenger");
            mdlSim = new SendingHandlerApduInterface(messenger, this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mdlSim = new ErrorApduInterface();
        super.onDestroy();
    }

    @Override
    public byte[] processCommandApdu(final byte[] commandApdu, final Bundle extras) {
        Log.v(TAG, "Received APDU: " + HexStrings.toHexString(commandApdu));
        final byte[] response = mdlSim.send(commandApdu);
        Log.v(TAG, "Returning APDU: " + HexStrings.toHexString(response));
        return response;
    }


    @Override
    public void onDeactivated(final int reason) {

    }
}
