package com.ul.ts.products.mdlreader.connection.descriptor;

import com.ul.ts.products.mdlreader.connection.bluetooth.BLEConnection;

import org.junit.Test;

import java.text.ParseException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class ConnectionDescriptorTest {
    @Test
    public void testParseBLEConnectionInfo() throws Exception {
        String ciString = "BLE:abcd";
        ConnectionInfo ci = ConnectionInfo.getConnectionInfo(ciString);
        assertThat(ci, instanceOf(BLEConnectionInfo.class));
        assertEquals(ConnectionDescriptor.buildConnectionInfoString(ci), ciString);
    }

    @Test(expected=ParseException.class)
    public void testThrowOnIllegalBLEConnectionInfo() throws Exception {
        ConnectionInfo ci = ConnectionInfo.getConnectionInfo("BLE:$#@$#@!");
    }

    @Test
    public void testParseWDConnectionInfo() throws Exception {
        String ciString = "aa:bb:cc:dd:ee:ff";
        ConnectionInfo ci = ConnectionInfo.getConnectionInfo(ciString);
        assertThat(ci, instanceOf(WDConnectionInfo.class));
        assertEquals(ConnectionDescriptor.buildConnectionInfoString(ci), ciString);
    }

    @Test(expected=ParseException.class)
    public void testThrowOnIllegalMAC_1() throws Exception {
        ConnectionInfo ci = ConnectionInfo.getConnectionInfo("aa:bb:cc:dd:ee");
    }

    @Test(expected=ParseException.class)
    public void testThrowOnIllegalMAC_2() throws Exception {
        ConnectionInfo ci = ConnectionInfo.getConnectionInfo("aa:bb:cc:dd:ee:ff:aa");
    }

    @Test(expected=ParseException.class)
    public void testThrowOnIllegalMAC_3() throws Exception {
        ConnectionInfo ci = ConnectionInfo.getConnectionInfo("aa:bb:cc:dd:ee:qq");
    }

    @Test(expected=ParseException.class)
    public void testThrowOnNonsense() throws Exception {
        ConnectionInfo ci = ConnectionInfo.getConnectionInfo("fdsafdsfasfsafdsaf");
    }

    @Test
    public void testParseTransferInfo() throws Exception {
        String tiString;
        TransferInfo ti;

        tiString = "0";
        ti = ConnectionDescriptor.parseTransferInfo(tiString);
        assertEquals(ti, TransferInfo.fullLicense());
        assertEquals(ConnectionDescriptor.buildTransferInfoString(ti), tiString);

        tiString = "18";
        ti = ConnectionDescriptor.parseTransferInfo(tiString);
        assertEquals(ti, TransferInfo.ageDetails(18));
        assertEquals(ConnectionDescriptor.buildTransferInfoString(ti), tiString);

        tiString = "nonsense";
        try {
            ConnectionDescriptor.parseTransferInfo(tiString);
            fail("Should throw ParseException on incorrect TransferInfo");
        } catch (ParseException e) {
            /* pass */
        }
    }

    @Test
    public void testSetupAndToString() throws Exception {
        ConnectionDescriptor cc = new ConnectionDescriptor("BLE:abcd;pacepass;0");
        assertEquals(cc.toDataString(), "BLE:abcd;pacepass;0");
        assertEquals(cc.connectionInfo, new BLEConnectionInfo("abcd"));
        assertEquals(cc.pacePassword, "pacepass");
        assertEquals(cc.transferInfo, TransferInfo.fullLicense());
    }
}