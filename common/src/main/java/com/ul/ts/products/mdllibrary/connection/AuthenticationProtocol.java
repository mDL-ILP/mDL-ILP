package com.ul.ts.products.mdllibrary.connection;

import java.io.IOException;

abstract public class AuthenticationProtocol extends TLVObject {
    public AuthenticationProtocol(TLVData value) throws IOException {
        super(value);
    }
}
