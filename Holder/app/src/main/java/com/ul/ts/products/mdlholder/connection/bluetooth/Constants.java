package com.ul.ts.products.mdlholder.connection.bluetooth;

import android.os.ParcelUuid;

import java.util.UUID;

public final class Constants {
    public static final UUID SERVICE_UUID = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid SERVICE_pUUID = new ParcelUuid(SERVICE_UUID);

    public static final UUID APDU_UUID = UUID.fromString("00002315-0000-1000-8000-00805f9b34fb");
    public static final ParcelUuid APDU_pUUID = new ParcelUuid(APDU_UUID);

}
