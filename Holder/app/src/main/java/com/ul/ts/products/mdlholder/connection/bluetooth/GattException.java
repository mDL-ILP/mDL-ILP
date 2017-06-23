package com.ul.ts.products.mdlholder.connection.bluetooth;


import static android.bluetooth.BluetoothGatt.*;

public class GattException extends Exception {
    int gattStatusCode;
    String reason = "Unknown reason";

    public GattException(int gattStatusCode) {
        this.gattStatusCode = gattStatusCode;
        switch (gattStatusCode) {
            case GATT_SUCCESS: reason="Success"; break;
            case GATT_READ_NOT_PERMITTED: reason="Read not permitted"; break;
            case GATT_WRITE_NOT_PERMITTED: reason="Write not permitted"; break;
            case GATT_INSUFFICIENT_AUTHENTICATION: reason="Insufficient authentication"; break;
            case GATT_REQUEST_NOT_SUPPORTED: reason="Request not supported"; break;
            case GATT_INSUFFICIENT_ENCRYPTION: reason="Insufficient encryption"; break;
            case GATT_INVALID_OFFSET: reason="Invalid offset"; break;
            case GATT_INVALID_ATTRIBUTE_LENGTH: reason="Invalid attribute length"; break;
            case GATT_CONNECTION_CONGESTED: reason="Connection congested"; break;
            case GATT_FAILURE: reason="General failure"; break;
            case 133: reason="Disconnected"; break;
        }
    }

    public GattException(String reason) {
        this.reason = reason;
        this.gattStatusCode = -1;
    }

    @Override
    public String getMessage() {
        return reason + " (code " + gattStatusCode + ")";
    }
}
