package com.ul.ts.products.mdlholder.connection.descriptor;

import com.ul.ts.products.mdllibrary.connection.AuthenticationProtocol;
import com.ul.ts.products.mdllibrary.connection.AuthenticationProtocolPACE;
import com.ul.ts.products.mdllibrary.connection.DataMinimizationParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class TransferInfo {
    protected String pacePassword;

    public TransferInfo(String pacePassword) {
        this.pacePassword = pacePassword;
    }

    public String getPacePassword() {
        return pacePassword;
    }

    public abstract DataMinimizationParameter getDataMinimizationParameter();

    public List<AuthenticationProtocol> getAuthenticationProtocols() throws IOException {
        List<AuthenticationProtocol> list = new ArrayList<>();
        list.add(new AuthenticationProtocolPACE(pacePassword.getBytes()));
        return list;
    }

    public abstract String buildTransferInfoString();
}

