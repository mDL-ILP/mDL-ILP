package com.ul.ts.products.mdlholder.utils;


/**
 * Contains a collection of utility methods for dealing with bits.
 * <p>
 * Bits are numbered from 1 to 8, with 8 being the most significant bit.
 */
public class BitUtils
{
    /**
     * Stores bit masks for bits 1 to 8, with the mask for bit 1 being first in the array.
     */
    static final int[] BIT_MASKS = { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80 };

    // private constructor, so this class can't be instantiated.
    private BitUtils()
    {
    }

    /**
     * Extracts a bit field from a byte value as an integer.
     * <p>
     * Bits are numbered from 1 to 8, with 8 being the most significant bit.
     * <p>
     * For example, given the structure and example value below:
     * <pre>
     *      | b8 | b7 | b6 | b5 | b4 | b3 | b2 | b1 |
     *      |    field1    | f2 |       field3      |
     *  x =   1    0    1    1    1    0    1    1    = 0xBB
     * </pre>
     * then:
     * <pre>
     *      extractBitField(x, 8, 6) == 0x05
     *      extractBitField(x, 5, 5) == 0x01
     *      extractBitField(x, 4, 1) == 0x0B.
     * </pre>
     * @param byteValue the value of the byte from which the bit field is to be extracted.
     * @param topBit the top bit of the field; <code>topBit</code> must be at least as large as <code>bottomBit</code>.
     * @param bottomBit the bottom bit of the field; <code>bottomBit</code> must be no larger than <code>topBit</code>.
     * @return the bit field as an integer.
     * @throws IllegalArgumentException if the byte value is not between 0 and 255,
     * or the bit arguments are not between 1 and 8, or <code>bottomBit</code> is larger than <code>topBit</code>.
     */
    public static final int extractBitField(int byteValue, int topBit, int bottomBit)
    {
        Preconditions.checkRange("byteValue", byteValue, 0, 255);
        checkBitNumberValid("topBit", topBit);
        checkBitNumberValid("bottomBit", bottomBit);
        // inline this check, to avoid creating the string if not necessary
        if (topBit < bottomBit)
        {
            throw new IllegalArgumentException("Bottom bit " + bottomBit + " is larger than top bit " + topBit + ".");
        }

        // construct the full mask from the individual masks
        int mask = 0;
        for (int i = bottomBit; i <= topBit; i++)
        {
            mask |= BIT_MASKS[i - 1];
        }

        // extract the value, and shift it
        // if the bottom bit is 1, don't shift at all, and so on
        return (byteValue & mask) >> (bottomBit - 1);
    }

    /**
     * Checks that the bit number is between 1 and 8.
     */
    private static void checkBitNumberValid(String bitNumberDescription, int bitNumber)
    {
        Preconditions.checkRange(bitNumberDescription, bitNumber, 1, 8);
    }

    /**
     * Creates an <code>int</code> from the specified bits.
     * <p>
     * This is intended to allow values based on bits to be created more explicitly,
     * and could be considered a replacement for the fact that Java does not have bit literals.
     * <p>
     * For example, if you want to encode the bits '0 1 1' in an integer, <code>bitField(0, 1, 1)</code> can be used
     * instead of coding the integer as <code>0x03</code>, which obscures the actual bit values being encoded
     * and is more open to errors.
     * @param bitValues the bit values; there should be between 1 and 31 elements supplied, each with value 0 or 1.
     * Note that at least 1 element must be supplied, as it's not clear what to do with zero elements;
     * and 32 elements isn't allowed, as the client's intention for signing isn't clear.
     * In any case, it is anticipated that the main usage of this method will be for 8 bit values or less.
     * @return the integer which has been constructed.
     * @throws IllegalArgumentException if the constraints on <code>bitValues</code> above aren't satisfied.
     */
    public static final int bitField(int ... bitValues)
    {
        Preconditions.checkRange("bitValues.length", bitValues.length, 1, 31);

        // start off at zero, then mask in each bit value
        int value = 0x00000000;
        int mask = 0x00000001;
        // start at the end of the array, which should be the lsb
        for (int i = bitValues.length - 1; i >= 0; i--)
        {
            int bitValue = bitValues[i];
            // the bit value should be 0 or 1
            Preconditions.check(bitValue == 0 || bitValue == 1, "All bit values should be 0 or 1.");
            // if it's 1, mask it in; if it's 0, then there's nothing to do
            if (bitValue == 1)
            {
                value |= mask;
            }
            // update the mask
            mask <<= 1;
        }
        return value;
    }

    /**
     * Returns {@code true} if the specified bits in {@code byteValue} (starting from {@code topBit})
     * are equal to {@code bitValues}.
     * <p>
     * For example, {@code areBitsSetTo(myByteValue, 7, 1, 1, 0)} would check whether b7 to b5 are equal to 110b
     * in {@code myByteValue}.
     * @return {@code true} if the bits are set as specified, {@code false} if the bits are not set as specified.
     */
    public static final boolean areBitsSetTo(int byteValue, int topBit, int... bitValues)
    {
        checkBitNumberValid("topBit", topBit);
        int bottomBit = topBit - bitValues.length + 1;
        checkBitNumberValid("bottomBit", bottomBit);

        int actualBitField = extractBitField(byteValue, topBit, bottomBit);
        int expectedBitField = bitField(bitValues);
        return actualBitField == expectedBitField;
    }

    /**
     * Returns {@code true} if the specified bit in {@code byteValue} is set to 1.
     * This is a convenience variance of {@link #areBitsSetTo(int, int, int...)}, for convenient use with a single bit.
     * @return {@code true} if the bit is set, {@code false} if the bit is not set.
     */
    public static boolean isBitSet(int byteValue, int bitNumber)
    {
        checkBitNumberValid("bitNumber", bitNumber);

        return BitUtils.extractBitField(byteValue, bitNumber, bitNumber) == 1;
    }

    /**
     * Creates a mask value from a bit string in the form "110xxx01".
     * @param binary the masked value. This should be a string containing only 0, 1 or x characters with a maximum of
     * 32 characters.
     * @return the mask value. For example if <code>value</code> is "110xxx01" the mask is 00011100 (0x1C).
     * @throws IllegalArgumentException if the supplied value contains more than 32 characters.
     * @throws NumberFormatException if the supplied value contains characters other than 1, 0, x or X.
     */
    public static final int getMaskFromWildCardBinary(String binary)
    {
        if (binary.length() > 32)
        {
            throw new IllegalArgumentException("value contains more than 32 digits: " + binary.length());
        }
        String mask = binary.replaceAll("1", "0");
        mask = mask.replaceAll("x|X", "1");
        return Integer.parseInt(mask, 2);
    }

    /**
     * Creates a mask value from a bit string in the form "110xxx01".
     *
     * @param binary the masked binary string. This should be a string containing only 0, 1 or x characters with a maximum of
     *              32 characters.
     * @return the mask value. For example if <code>value</code> is "110xxx01" the mask is 00011100 (0x1C).
     * @throws IllegalArgumentException if the supplied value contains more than 32 characters.
     * @throws NumberFormatException    if the supplied value contains characters other than 1, 0, x or X.
     */
    public static final int getValueFromWildCardBinary(String binary)
    {
        if (binary.length() > 32)
        {
            throw new IllegalArgumentException("value contains more than 32 digits: " + binary.length());
        }
        String value = binary.replaceAll("x|X", "0");
        return Integer.parseInt(value, 2);
    }
}
