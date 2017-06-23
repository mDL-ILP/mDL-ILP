package com.ul.ts.products.mdlholder.connection.descriptor;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.ul.ts.products.mdlholder.utils.QR.QRUtils;
import com.ul.ts.products.mdllibrary.connection.AuthenticationProtocol;
import com.ul.ts.products.mdllibrary.connection.AuthenticationProtocolPACE;
import com.ul.ts.products.mdllibrary.connection.DeviceEngagement;
import com.ul.ts.products.mdllibrary.connection.DeviceEngagementBuilder;
import com.ul.ts.products.mdllibrary.connection.InterchangeProfileInterfaceIndependent;
import com.ul.ts.products.mdllibrary.connection.TLVData;

import java.io.IOException;

public class ConnectionDescriptor implements Parcelable, Engagement {
    public ConnectionInfo connectionInfo;
    public TransferInfo transferInfo;

    public ConnectionDescriptor(byte[] connectionTLV) throws IOException {
        final DeviceEngagement de = new DeviceEngagement(new TLVData(connectionTLV));

        connectionInfo = ConnectionInfo.getConnectionInfo(de.getTransferInterchangeProfile());
        transferInfo = getTransferInfo(de);
    }

    public static TransferInfo getTransferInfo(DeviceEngagement de) {
        final InterchangeProfileInterfaceIndependent iiProfile = de.getInterfaceIndependentProfile();

        String pacePassword = null;
        for (AuthenticationProtocol ap : iiProfile.authenticationProtocols) {
            if (ap instanceof AuthenticationProtocolPACE) {
                pacePassword = new String(((AuthenticationProtocolPACE) ap).pacePassword);
            }
        }

        if (iiProfile.dataMinimizationParameter.ageLimited()) {
            return new FullLicenseTransferInfo(pacePassword);
        } else {
            return new AgeTransferInfo(pacePassword, iiProfile.dataMinimizationParameter.getAgeLimit());
        }
    }

    public ConnectionDescriptor(final ConnectionInfo connectionInfo, final TransferInfo transferInfo) {
        this.connectionInfo = connectionInfo;
        this.transferInfo = transferInfo;
    }

    public static final Creator<ConnectionDescriptor> CREATOR = new Creator<ConnectionDescriptor>() {
        @Override
        public ConnectionDescriptor createFromParcel(Parcel in) {
            try {
                return new ConnectionDescriptor(in.createByteArray());
              } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ConnectionDescriptor[] newArray(int size) {
            return new ConnectionDescriptor[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(getContents());
    }

    public byte[] getContents() {
        try {
            final DeviceEngagement de = new DeviceEngagementBuilder()
                    .setDataMinimizationParameter(transferInfo.getDataMinimizationParameter())
                    .setAuthenticationProtocols(transferInfo.getAuthenticationProtocols())
                    .addInterchangeProfile(connectionInfo.getInterchangeProfile())
                    .build();
            return de.toDER();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Bitmap getQr() {
        return QRUtils.getQR(getContents());
    }

    public Bitmap getQr(int size) {
        return QRUtils.getQR(getContents(), size);
    }

    @Override
    public boolean engageNFC() {
        return !(connectionInfo.usesNFC());
    }
}
