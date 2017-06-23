package com.ul.ts.products.mdllibrary.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InterchangeProfileFactory {
    private final Map<UUID, Class<? extends InterchangeProfile>> uuidMap = new HashMap<>();
    private static InterchangeProfileFactory instance = null;

    private static InterchangeProfileFactory getInstance() {
        if (instance == null) instance = new InterchangeProfileFactory();
        return instance;
    }

    private InterchangeProfileFactory() {
        uuidMap.put(InterchangeProfileInterfaceIndependent.profileUUID, InterchangeProfileInterfaceIndependent.class);
        uuidMap.put(InterchangeProfileBLE.profileUUID, InterchangeProfileBLE.class);
        uuidMap.put(InterchangeProfileWD.profileUUID, InterchangeProfileWD.class);
        uuidMap.put(InterchangeProfileNFC.profileUUID, InterchangeProfileNFC.class);
    }

    public static InterchangeProfile build(TLVData tlv) throws IOException {
        final UUID uuid = Utils.getUuid(Utils.getValue(tlv, (byte) 0x34));

        Class<? extends InterchangeProfile> cls = getInstance().uuidMap.get(uuid);

        InterchangeProfile ip;
        try {
            ip = cls.getDeclaredConstructor(TLVData.class).newInstance(tlv);
        } catch (Exception e) {
            throw new IOException(e);
        }

        return ip;
    }

}
