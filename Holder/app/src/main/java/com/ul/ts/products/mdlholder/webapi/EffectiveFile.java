package com.ul.ts.products.mdlholder.webapi;

public class EffectiveFile {

    private String id;
    private String name;
    private byte[] value;

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getValue() {
        return value;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}
