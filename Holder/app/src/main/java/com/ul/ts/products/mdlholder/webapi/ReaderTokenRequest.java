package com.ul.ts.products.mdlholder.webapi;

import java.util.List;

public class ReaderTokenRequest {

    private String deviceToken;
    private List<String> permittedDatagroups;

    public String getDeviceToken() {
        return deviceToken;
    }

    public List<String> getPermittedDatagroups() {
        return permittedDatagroups;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setPermittedDatagroups(List<String> permittedDatagroups) {
        this.permittedDatagroups = permittedDatagroups;
    }
}
