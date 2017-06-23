package com.ul.ts.products.mdllibrary.connection;

public class TLVData {
    public static TLVData EMPTY = new TLVData(new byte[] {});

    public final byte[] data;

    public TLVData(final byte[] data) {
        this.data = data;
    }
}
