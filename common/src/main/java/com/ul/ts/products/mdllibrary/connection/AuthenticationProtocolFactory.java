package com.ul.ts.products.mdllibrary.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationProtocolFactory {
    private final Map<ObjectIdentifier, Class<? extends AuthenticationProtocol>> oidMap = new HashMap<>();
    private static AuthenticationProtocolFactory instance = null;

    private static AuthenticationProtocolFactory getInstance() {
        if (instance == null) instance = new AuthenticationProtocolFactory();
        return instance;
    }

    private AuthenticationProtocolFactory() {
        oidMap.put(AuthenticationProtocolPACE.oid, AuthenticationProtocolPACE.class);
    }

    public static AuthenticationProtocol build(TLVData value) throws IOException {
        ObjectIdentifier oid = new ObjectIdentifier(Utils.getValue(value, (byte) 0x06));

        Class<? extends AuthenticationProtocol> cls = getInstance().oidMap.get(oid);

        AuthenticationProtocol ip;
        try {
            ip = cls.getDeclaredConstructor(TLVData.class).newInstance(value);
        } catch (Exception e) {
            throw new IOException(e);
        }

        return ip;
    }
}
