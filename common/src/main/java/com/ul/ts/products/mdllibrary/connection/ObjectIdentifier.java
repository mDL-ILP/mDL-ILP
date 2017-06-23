package com.ul.ts.products.mdllibrary.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectIdentifier {
    final List<Integer> parts;

    public ObjectIdentifier(final String oid) {
        parts = new ArrayList<>();
        for (String element : oid.split("\\.")) {
            parts.add(Integer.parseInt(element));
        }

        if (parts.size() < 2) {
            throw new RuntimeException("Illegal oid: " + oid);
        }
    }

    public ObjectIdentifier(final byte[] value) {
        parts = new ArrayList<>();
        /*
        According to http://luca.ntop.org/Teaching/Appunti/asn1.html:
        The first octet has value 40 * value1 + value2. (This is unambiguous, since value1 is
        limited to values 0, 1, and 2; value2 is limited to the range 0 to 39 when value1 is 0
        or 1; and, according to X.208, n is always at least 2.)

        The following octets, if any, encode value3, ..., valuen. Each value is encoded base 128,
        most significant digit first, with as few digits as possible, and the most significant bit
        of each octet except the last in the value's encoding set to "1."
        */

        int ctr = 0;
        for (byte b: value) {
            int i = Utils.intFromByte(b);
            if (ctr == 0) {
                parts.add(i / 40);
                parts.add(i % 40);
            } else {
                parts.add(i);
            }
            ctr += 1;
        }

        if (parts.size() < 2) {
            throw new RuntimeException("Illegal oid: " + Arrays.toString(value));
        }
    }

    public byte[] toByteArray() {
        byte[] ba = new byte[parts.size() - 1];

        int ctr = 0;
        for (int i : parts) {
            if (ctr == 0) {
                ba[0] = (byte) (i * 40);
            } else if (ctr == 1) {
                ba[0] += (byte) i;
            } else {
                ba[ctr-1] = (byte) i;
            }

            ctr += 1;
        }

        return ba;
    }

    public String toOidString() {
        StringBuilder sb = new StringBuilder();
        for (Integer i : parts) {
            sb.append(i); sb.append(".");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ObjectIdentifier that = (ObjectIdentifier) o;

        return parts.equals(that.parts);

    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }

    @Override
    public String toString() {
        return "ObjectIdentifier{" + toOidString() + "}";
    }
}
