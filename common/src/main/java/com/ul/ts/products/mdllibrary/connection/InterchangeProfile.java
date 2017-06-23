package com.ul.ts.products.mdllibrary.connection;

import java.io.IOException;

abstract public class InterchangeProfile extends TLVObject {
    public InterchangeProfile(TLVData value) throws IOException {
        super(value);
    }
}
