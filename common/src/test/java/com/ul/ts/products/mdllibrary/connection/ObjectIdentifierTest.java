package com.ul.ts.products.mdllibrary.connection;

import com.ul.ts.products.mdllibrary.HexStrings;

import static org.junit.Assert.*;

public class ObjectIdentifierTest {
    @org.junit.Test
    public void toByteArray() throws Exception {
        ObjectIdentifier oid;


        oid = new ObjectIdentifier("0.4.0.127.0.7.2.2.4");
        assertArrayEquals(HexStrings.fromHexString("04 00 7F 00 07 02 02 04"), oid.toByteArray());

        oid = new ObjectIdentifier("1.3.6.1.5.5.7.48.1");
        assertArrayEquals(HexStrings.fromHexString("2b 06 01 05 05 07 30 01"), oid.toByteArray());
    }

    @org.junit.Test
    public void toOidString() throws Exception {
        ObjectIdentifier oid;

        oid = new ObjectIdentifier("0.4.0.127.0.7.2.2.4");
        assertEquals("0.4.0.127.0.7.2.2.4", oid.toOidString());

        oid = new ObjectIdentifier("1.3.6.1.5.5.7.48.1");
        assertEquals("1.3.6.1.5.5.7.48.1", oid.toOidString());
    }

    @org.junit.Test
    public void fromByteArray() throws Exception {
        ObjectIdentifier oid;

        oid = new ObjectIdentifier(HexStrings.fromHexString("04 00 7F 00 07 02 02 04"));
        assertEquals("0.4.0.127.0.7.2.2.4", oid.toOidString());

        oid = new ObjectIdentifier(HexStrings.fromHexString("2b 06 01 05 05 07 30 01"));
        assertEquals("1.3.6.1.5.5.7.48.1", oid.toOidString());
    }

    @org.junit.Test
    public void testEquals() throws Exception {
        assertEquals(new ObjectIdentifier(HexStrings.fromHexString("04 00 7F 00 07 02 02 04")),
                new ObjectIdentifier("0.4.0.127.0.7.2.2.4"));
    }

}