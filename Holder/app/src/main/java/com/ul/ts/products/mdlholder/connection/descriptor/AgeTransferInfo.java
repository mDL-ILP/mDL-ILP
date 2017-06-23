package com.ul.ts.products.mdlholder.connection.descriptor;

import com.ul.ts.products.mdllibrary.connection.DataMinimizationParameter;

public class AgeTransferInfo extends TransferInfo {
    protected int age;

    public AgeTransferInfo(final String pacePassword, final int age) {
        super(pacePassword);
        this.age = age;
    }

    @Override
    public String buildTransferInfoString() {
        return String.valueOf(age);
    }


    @Override
    public DataMinimizationParameter getDataMinimizationParameter() {
        return new DataMinimizationParameter(age);
    }
}
