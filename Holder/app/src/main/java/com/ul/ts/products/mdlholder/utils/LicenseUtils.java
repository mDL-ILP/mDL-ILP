package com.ul.ts.products.mdlholder.utils;

import android.content.Context;
import android.util.Log;

import com.ul.ts.products.mdlholder.cardsim.MDLSim;
import com.ul.ts.products.mdlholder.data.CSCACertificates;
import com.ul.ts.products.mdlholder.data.DrivingLicence;
import com.ul.ts.products.mdlholder.data.ReadDataScript;

import java.io.IOException;
import java.util.Random;

public class LicenseUtils {

    public static DrivingLicence getLicense(Context context) throws IOException {

        ReadDataScript script = new ReadDataScript(new MDLSim(context, true, "doesn't matter", 255));

        byte[] EFSOd = script.readFile(0x0001D);
        Log.d("EFSOd", ByteUtils.bytesToHex(EFSOd));
//        byte[] EFCOM = script.readFile(0x0001E);
//        Log.d("EFCOM", ByteUtils.bytesToHex(EFCOM));
        byte[] DG1 = script.readFile(0x0001);
        Log.d("DG1", ByteUtils.bytesToHex(DG1));
        byte[] DG6 = script.readFile(0x0006);
        Log.d("DG6", ByteUtils.bytesToHex(DG6));
        byte[] DG10 = script.readFile(0x000A);
        Log.d("DG10", ByteUtils.bytesToHex(DG10));
        byte[] DG11 = script.readFile(0x000B);
        Log.d("DG11", ByteUtils.bytesToHex(DG11));
        byte[] DG13 = script.readFile(0x000D);
        Log.d("DG13", ByteUtils.bytesToHex(DG13));
        byte[] DG15 = script.readFile(0x000F);
        Log.d("DG15", ByteUtils.bytesToHex(DG15));
        byte[] DG16 = script.readFile(0x0010);
        Log.d("DG16", ByteUtils.bytesToHex(DG16));
        byte[] rand = new byte[8];
        new Random().nextBytes(rand);
        Log.d("Random", ByteUtils.bytesToHex(rand));
        byte[] responseToAuth = script.internalAuthenticate(rand);
        Log.d("AA response", ByteUtils.bytesToHex(responseToAuth));

        CSCACertificates cscaCertificates = new CSCACertificates(context);
        return new DrivingLicence(DG1, DG6, DG10, DG11, DG13, DG15, DG16, EFSOd, rand, responseToAuth, cscaCertificates);
    }
}
