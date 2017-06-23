package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

public class UnsupportedTagException extends RuntimeException {
    final byte tag;
    final byte[] value;

    public UnsupportedTagException(byte tag, byte[] value) {
        this.tag = tag;
        this.value = value;
    }

    @Override
    public String toString() {
        return "UnsupportedTagException{tag=" + HexStrings.toHexString(this.tag) + ", value=" + HexStrings.toHexString(this.value) + "}";
    }
}
