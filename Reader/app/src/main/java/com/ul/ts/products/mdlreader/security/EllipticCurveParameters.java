package com.ul.ts.products.mdlreader.security;

import com.ul.ts.products.mdlreader.utils.Bytes;
import com.ul.ts.products.mdlreader.utils.Preconditions;

import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Arrays;

/**
 * Create instances of Elliptic Curve containing the required parameters.
 */
public class EllipticCurveParameters
{
    /**
     * Standard curve brainpoolP256r1
     */
    public static EllipticCurveParameters brainpoolP192r1;

    /**
     * Standard curve brainpoolP256r1
     */
    public static EllipticCurveParameters brainpoolP256r1;

    /**
     * Standard curve brainpoolP320r1
     */
    public static EllipticCurveParameters brainpoolP320r1;

    static
    {
        brainpoolP192r1 = specifyCurve(
                Bytes.bytes(  // Curve parameter A
                        "6A 91 17 40 76 B1 E0 E1 9C 39 C0 31 FE 86 85 C1 CA E0 40 E5 C6 9A 28 EF"),

                Bytes.bytes(  // Curve parameter B
                        "46 9A 28 EF 7C 28 CC A3 DC 72 1D 04 4F 44 96 BC CA 7E F4 14 6F BF 25 C9"),

                Bytes.bytes( // Fixed point G X coordinate
                        "C0 A0 64 7E AA B6 A4 87 53 B0 33 C5 6C B0 F0 90 0A 2F 5C 48 53 37 5F D6"),

                Bytes.bytes( // Fixed point G Y coordinate
                        "14 B6 90 86 6A BD 5B B8 8B 5F 48 28 C1 49 00 02 E6 77 3F A2 FA 29 9B 8F"),

                0x01, // Cofactor H

                Bytes.bytes( // Order N
                        "C3 02 F4 1D 93 2A 36 CD A7 A3 46 2F 9E 9E 91 6B 5B E8 F1 02 9A C4 AC C1"),

                Bytes.bytes( // Field P
                        "C3 02 F4 1D 93 2A 36 CD A7 A3 46 30 93 D1 8D B7 8F CE 47 6D E1 A8 62 97"),

                192); // curve size

        brainpoolP256r1 = specifyCurve(
                Bytes.bytes(  // Curve parameter A
                        "7D 5A 09 75 FC 2C 30 57 EE F6 75 30 41 7A FF E7 FB 80 55 C1 26 DC 5C 6C E9 4A 4B 44 F3 30 B5 D9"),

                Bytes.bytes(  // Curve parameter B
                        "26 DC 5C 6C E9 4A 4B 44 F3 30 B5 D9 BB D7 7C BF 95 84 16 29 5C F7 E1 CE 6B CC DC 18 FF 8C 07 B6"),

                Bytes.bytes( // Fixed point G X coordinate
                        "8B D2 AE B9 CB 7E 57 CB 2C 4B 48 2F FC 81 B7 AF B9 DE 27 E1 E3 BD 23 C2 3A 44 53 BD 9A CE 32 62"),

                Bytes.bytes( // Fixed point G Y coordinate
                        "54 7E F8 35 C3 DA C4 FD 97 F8 46 1A 14 61 1D C9 C2 77 45 13 2D ED 8E 54 5C 1D 54 C7 2F 04 69 97"),

                0x01, // Cofactor H

                Bytes.bytes( // Order N
                        "A9 FB 57 DB A1 EE A9 BC 3E 66 0A 90 9D 83 8D 71 8C 39 7A A3 B5 61 A6 F7 90 1E 0E 82 97 48 56 A7"),

                Bytes.bytes( // Field P
                        "A9 FB 57 DB A1 EE A9 BC 3E 66 0A 90 9D 83 8D 72 6E 3B F6 23 D5 26 20 28 20 13 48 1D 1F 6E 53 77"),

                256); // curve size

        brainpoolP320r1 = specifyCurve(
                Bytes.bytes(  // Curve parameter A
                        "3E E3 0B 56 8F BA B0 F8 83 CC EB D4 6D 3F 3B B8 A2 A7 35 13 F5 EB 79 DA 66 19 0E B0 85 FF A9 F4 92 F3 75 A9 7D 86 0E B4"),

                Bytes.bytes(  // Curve parameter B
                        "52 08 83 94 9D FD BC 42 D3 AD 19 86 40 68 8A 6F E1 3F 41 34 95 54 B4 9A CC 31 DC CD 88 45 39 81 6F 5E B4 AC 8F B1 F1 A6"),

                Bytes.bytes( // Fixed point G X coordinate
                        "43 BD 7E 9A FB 53 D8 B8 52 89 BC C4 8E E5 BF E6 F2 01 37 D1 0A 08 7E B6 E7 87 1E 2A 10 A5 99 C7 10 AF 8D 0D 39 E2 06 11"),

                Bytes.bytes( // Fixed point G Y coordinate
                        "14 FD D0 55 45 EC 1C C8 AB 40 93 24 7F 77 27 5E 07 43 FF ED 11 71 82 EA A9 C7 78 77 AA AC 6A C7 D3 52 45 D1 69 2E 8E E1"),

                0x01, // Cofactor H

                Bytes.bytes( // Order N
                        "D3 5E 47 20 36 BC 4F B7 E1 3C 78 5E D2 01 E0 65 F9 8F CF A5 B6 8F 12 A3 2D 48 2E C7 EE 86 58 E9 86 91 55 5B 44 C5 93 11"),

                Bytes.bytes( // Field P
                        "D3 5E 47 20 36 BC 4F B7 E1 3C 78 5E D2 01 E0 65 F9 8F CF A6 F6 F4 0D EF 4F 92 B9 EC 78 93 EC 28 FC D4 12 B1 F1 B3 2E 27"),

                320); // curve size
    }

    private byte[] a;
    private byte[] b;
    private byte[] x;
    private byte[] y;
    private int h;
    private byte[] n;
    private byte[] p;
    private int curveSize;

    private EllipticCurveParameters(byte[] a, byte[] b, byte[] x, byte[] y, int h, byte[] n, byte[] p, int size)
    {
        Preconditions.checkForNull("Curve parameter A", a);
        Preconditions.checkForNull("Curve parameter B", b);
        Preconditions.checkForNull("Fixed point G X coordinate", x);
        Preconditions.checkForNull("Fixed point G Y coordinate", y);
        // TODO(JS): is there a sensible check for cofactor h?
        Preconditions.checkForNull("Order N", n);
        Preconditions.checkForNull("Field prime P", p);
        // TODO(JS): is there a sensible check for curve size?

        this.a = Arrays.copyOf(a, a.length);
        this.b = Arrays.copyOf(b, b.length);
        this.x = Arrays.copyOf(x, x.length);
        this.y = Arrays.copyOf(y, y.length);
        this.h = h;
        this.n = Arrays.copyOf(n, n.length);
        this.p = Arrays.copyOf(p, p.length);
        this.curveSize = size;
    }

    /**
     * Create a new Elliptic Curve Parameter instance with the specified parameters.
     * @param a curve parameter A
     * @param b curve parameter B
     * @param x fixed point G X coordinate
     * @param y fixed point G Y coordinate
     * @param h cofactor H
     * @param n order N
     * @param p field prime P
     * @return the new Elliptic Curve Parameter instance.
     */
    public static EllipticCurveParameters specifyCurve(byte[] a, byte[] b, byte[] x, byte[] y, int h, byte[] n, byte[] p, int curveSize)
    {
        Preconditions.check(a.length == b.length, "Curve parameters are not the correct length");
        Preconditions.check(b.length == x.length, "Curve parameters are not the correct length");
        Preconditions.check(x.length == y.length, "Curve parameters are not the correct length");
        Preconditions.check(y.length == n.length, "Curve parameters are not the correct length");
        Preconditions.check(n.length == p.length, "Curve parameters are not the correct length");

        return new EllipticCurveParameters(a, b, x, y, h, n, p, curveSize);
    }

    /**
     * Returns the curve parameter A.
     * @return curve parameter A
     */
    public byte[] getA()
    {
        return Arrays.copyOf(a, a.length);
    }

    /**
     * Returns the curve parameter B.
     * @return curve parameter B
     */
    public byte[] getB()
    {
        return Arrays.copyOf(b, b.length);
    }

    /**
     * Returns the fixed point G X coordinate.
     * @return curve parameter X coordinate.
     */
    public byte[] getX()
    {
        return Arrays.copyOf(x, x.length);
    }

    /**
     * Returns the fixed point G Y coordinate.
     * @return curve parameter Y coordinate.
     */
    public byte[] getY()
    {
        return Arrays.copyOf(y, y.length);
    }

    /**
     * Returns the cofactor H.
     * @return cofactor H
     */
    public int getH()
    {
        return h;
    }

    /**
     * Returns the order N.
     * @return order N
     */
    public byte[] getN()
    {
        return Arrays.copyOf(n, n.length);
    }

    /**
     * Returns the field prime P.
     * @return field prime P
     */
    public byte[] getP()
    {
        return Arrays.copyOf(p, p.length);
    }

    public int getCurveSize() {
        return curveSize;
    }

    public static ECParameterSpec encodeECParameterSpec(EllipticCurveParameters params) {

        // Field
        final BigInteger pInt = new BigInteger(1, params.getP());
        final ECField field = new ECFieldFp(pInt);

        final BigInteger aInt = new BigInteger(1, params.getA());
        final BigInteger bInt = new BigInteger(1, params.getB());
        final EllipticCurve curve = new EllipticCurve(field, aInt, bInt);

        // Fixed Point G
        final BigInteger xInt = new BigInteger(1, params.getX());
        final BigInteger yInt = new BigInteger(1, params.getY());
        final ECPoint g = new ECPoint(xInt, yInt);

        // Order N
        final BigInteger nInt = new BigInteger(1, params.getN());

        return new ECParameterSpec(curve, g, nInt, params.getH());
    }
}
