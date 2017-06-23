package com.ul.ts.products.mdlholder.connection.descriptor;

import com.ul.ts.products.mdllibrary.connection.DataMinimizationParameter;

public class FullLicenseTransferInfo extends TransferInfo {
    public FullLicenseTransferInfo(final String pacePassword) {
        super(pacePassword);
    }

    @Override
    public String buildTransferInfoString() {
        return "0";
    }

    @Override
    public DataMinimizationParameter getDataMinimizationParameter() {
        return new DataMinimizationParameter(0);
    }
}
