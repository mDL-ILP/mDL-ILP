package com.ul.ts.products.mdllibrary.connection;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DeviceEngagementBuilder {
    private DataMinimizationParameter dmp;
    private List<AuthenticationProtocol> authProtocols;
    private List<InterchangeProfile> ips;

    public DeviceEngagementBuilder() {
        ips = new LinkedList<>();
    }

    public DeviceEngagementBuilder setDataMinimizationParameter(DataMinimizationParameter dmp) {
        this.dmp = dmp;
        return this;
    }

    public DeviceEngagementBuilder setAuthenticationProtocols(List<AuthenticationProtocol> authProtocols) {
        this.authProtocols = authProtocols;
        return this;
    }

    public DeviceEngagementBuilder addInterchangeProfile(InterchangeProfile ip) {
        ips.add(ip);
        return this;
    }

    public DeviceEngagement build() throws IOException {
        if (dmp == null) {
            throw new RuntimeException("DataMinimizationParameter not set");
        }
        if (authProtocols.size() == 0) {
            throw new RuntimeException("No AuthenticationProtocols set");
        }

        if (ips.size() == 0) {
            throw new RuntimeException("No communication protocols set");
        }

        ips.add(0, new InterchangeProfileInterfaceIndependent(
                        new byte[]{},
                        new byte[]{},
                        dmp,
                        authProtocols,
                        Arrays.asList("en")
                )
        );

        return new DeviceEngagement(ips);
    }
}