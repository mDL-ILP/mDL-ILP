package com.ul.ts.products.mdllibrary.connection;

import java.io.IOException;

public class DataMinimizationParameter implements ToDER {
    final int age;

    public DataMinimizationParameter(TLVData value) throws IOException {
        age = Utils.intFromByte(value.data[0]);
    }

    public DataMinimizationParameter(final int age) {
        this.age = age;
    }

    public boolean ageLimited() {
        return (age != 0);
    }

    public int getAgeLimit() {
        return age;
    }


    @Override
    public byte[] toDER() {
        return new byte[] {(byte) age};
    }

    @Override
    public String toString() {
        return "DataMinimizationParameter{" +
                "age=" + age +
                '}';
    }
}
