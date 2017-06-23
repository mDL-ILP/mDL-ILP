package com.ul.ts.products.mdlreader.security;

import java.util.HashMap;
import java.util.Map;

public class PACEOIDs {

    private static final Map<String, String> oidMap;

    static {
        oidMap = new HashMap<>();

        oidMap.put("0.4.0.127.0.7.2.2.4",     "id-PACE");
        oidMap.put("0.4.0.127.0.7.2.2.4.1",   "id-PACE-DH-GM");
        oidMap.put("0.4.0.127.0.7.2.2.4.1.1", "id-PACE-DH-GM-3DES-CBC-CBC");
        oidMap.put("0.4.0.127.0.7.2.2.4.1.2", "id-PACE-DH-GM-AES-CBC-CMAC-128");
        oidMap.put("0.4.0.127.0.7.2.2.4.1.3", "id-PACE-DH-GM-AES-CBC-CMAC-192");
        oidMap.put("0.4.0.127.0.7.2.2.4.1.4", "id-PACE-DH-GM-AES-CBC-CMAC-256");

        oidMap.put("0.4.0.127.0.7.2.2.4.2",   "id-PACE-ECDH-GM");
        oidMap.put("0.4.0.127.0.7.2.2.4.2.1", "id-PACE-ECDH-GM-3DES-CBC-CBC");
        oidMap.put("0.4.0.127.0.7.2.2.4.2.2", "id-PACE-ECDH-GM-AES-CBC-CMAC-128");
        oidMap.put("0.4.0.127.0.7.2.2.4.2.3", "id-PACE-ECDH-GM-AES-CBC-CMAC-192");
        oidMap.put("0.4.0.127.0.7.2.2.4.2.4", "id-PACE-ECDH-GM-AES-CBC-CMAC-256");

        oidMap.put("0.4.0.127.0.7.2.2.4.3",   "id-PACE-DH-IM");
        oidMap.put("0.4.0.127.0.7.2.2.4.3.1", "id-PACE-DH-IM-3DES-CBC-CBC");
        oidMap.put("0.4.0.127.0.7.2.2.4.3.2", "id-PACE-DH-IM-AES-CBC-CMAC-128");
        oidMap.put("0.4.0.127.0.7.2.2.4.3.3", "id-PACE-DH-IM-AES-CBC-CMAC-192");
        oidMap.put("0.4.0.127.0.7.2.2.4.3.4", "id-PACE-DH-IM-AES-CBC-CMAC-256");

        oidMap.put("0.4.0.127.0.7.2.2.4.4",   "id-PACE-ECDH-IM");
        oidMap.put("0.4.0.127.0.7.2.2.4.4.1", "id-PACE-ECDH-IM-3DES-CBC-CBC");
        oidMap.put("0.4.0.127.0.7.2.2.4.4.2", "id-PACE-ECDH-IM-AES-CBC-CMAC-128");
        oidMap.put("0.4.0.127.0.7.2.2.4.4.3", "id-PACE-ECDH-IM-AES-CBC-CMAC-192");
        oidMap.put("0.4.0.127.0.7.2.2.4.4.4", "id-PACE-ECDH-IM-AES-CBC-CMAC-256");
    }

}
