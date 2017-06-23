package com.ul.ts.products.mdllibrary.connection;

import net.sf.scuba.tlv.TLVInputStream;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

abstract class TLVObject implements ToDER {
    public TLVObject(TLVData value) throws IOException {
        init();
        this.setFromTLV(value);
    }

    protected void setFromTLV(TLVData source) throws IOException {
        final TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(source.data));

        try {
            //noinspection InfiniteLoopStatement
            while(true) {
                final byte tag = (byte) stream.readTag();
                stream.readLength();
                final byte[] content = stream.readValue();
                tlvSet(tag, content);
            }
        } catch (EOFException e) {
            // pass
        }
    }

    /*
    Perform initialization; is called by the constructor before setTLV() is called.
     */
    protected void init() {
    }

    abstract protected void tlvSet(byte tag, byte[] value) throws IOException;

    @Override
    abstract public int hashCode();

    @Override
    public boolean equals(Object obj) {
        return (obj.getClass() == getClass()) && (obj.hashCode() == hashCode());
    }
}
