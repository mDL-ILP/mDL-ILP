package com.ul.ts.products.mdlreader.utils;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

public class BTUtils {
    @NonNull
    public static String deviceString(BluetoothDevice device) {
        return device.getAddress() + " => " + device.getName();
    }
}
