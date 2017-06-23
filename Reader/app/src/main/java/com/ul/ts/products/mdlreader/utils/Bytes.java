package com.ul.ts.products.mdlreader.utils;

import java.util.Arrays;

/**
 * A collection of convenience methods for generating <code>byte</code> arrays from different sources.
 */
public class Bytes
{
    /**
     * Class should never be instantiated.
     */
    private Bytes()
    {

    }

    /**
     * Create a new <byte> array from a series of <code>int<code>s. Each <code>int<code> should contain one
     * byte value.
     * @param byteData a series of <code>int</code>s to be converted into a <code>byte</code> array.
     * @return a new <code>byte</code> array containing the input data.
     */
    public static byte[] bytes(int ... byteData)
    {
        return ByteUtils.bytes(byteData);
    }

    /**
     * Create a new <code>byte</code> array from a string containing hexadecimal byte values.
     * For example "00 FF 4B 7D" or "00FF4B7D".
     * @param byteString a space separated hexadecimal byte string.
     * @return a new <code>byte</code> array containing the input data.
     */
    public static byte[] bytes(String byteString)
    {
        return HexStrings.fromMixedFormatHexString(byteString);
    }

    /**
     * Concatenates the supplied byte arrays into a single byte array.
     * <p>
     * If no byte arrays are supplied, a byte array of length zero is returned.<br>
     * If any of the component arrays are <code>null</code>, they are treated as arrays of length zero.
     * <p>
     * <code>null</code> will never be returned.
     * An array of length zero will be returned if there are no array elements in the result.
     * @param arrays the array of byte arrays to be concatenated.
     * @return the concatenated byte array.
     */
    public static byte [] concatenate(byte[]... arrays)
    {
        // check we've got something
        if (arrays == null)
        {
            return ByteUtils.EMPTY_BYTE_ARRAY;
        }

        // get the total length of the byte arrays
        int length = 0;
        for (int i = 0; i < arrays.length; i++)
        {
            // ensure we have a proper byte array
            if (arrays[i] == null)
            {
                arrays[i] = ByteUtils.EMPTY_BYTE_ARRAY;
            }
            // add on its length
            length += arrays[i].length;
        }

        // make the return array
        byte [] concatenation = new byte[length];

        // copy the contents of the source arrays into the destination
        int count = 0;
        for (byte[] array : arrays)
        {
            System.arraycopy(array, 0, concatenation, count, array.length);
            count += array.length;
        }

        return concatenation;
    }

    /**
     * Compares two byte arrays for equality. Two byte arrays are equal if they are the same length and each
     * element at the same index within the arrays is of equal value.
     * @param b1 the first array to compare.
     * @param b2 the second array to compare.
     * @return <code>true</code> if both arrays are equal, <code>false</code> otherwise.
     */
    public static boolean isEqual(byte [] b1, byte[] b2)
    {
        if (b1 == null || b2 == null || b1.length != b2.length)
        {
            return false;
        }

        for (int i = 0; i < b1.length; ++i)
        {
            if (b1[i] != b2[i])
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates a new <code>string</code> containing a space separated hexadecimal list of bytes that match the
     * input array.
     * @param byteData the <code>byte</code> array to be converted to a string.
     * @return the hexadecimal encoding byte string.
     */
    public static String hexString(byte[] byteData)
    {
        return HexStrings.toHexString(byteData);
    }

    /**
     * Creates a new <code>string</code> containing a hexadecimal list of bytes that match the
     * input array with no spaces.
     * @param byteData the <code>byte</code> array to be converted to a string.
     * @return the hexadecimal encoding byte string.
     */
    public static String unspacedHexString(byte[] byteData)
    {
        return HexStrings.toUnspacedHexString(byteData);
    }

    /**
     * Returns a string containing a Hex representation of the provided integer. The string will show a whole number of
     * bytes i.e. F is displayed as 0F, A3B is displayed as 0A3B.
     * @param value the integer to be displayed as a hex string
     * @return the integer as a hex string
     */
    public static String toHexWord(int value)
    {
        return HexStrings.toHexWord(value);
    }

    /**
     * Returns a string containing the supplied value coded on a specified number of hex bytes.
     * <p/>
     * <p/>
     * <p/>
     * A hex byte will be coded on two digits, zero padding if necessary (e.g. '0F' rather than 'F').
     * <p/>
     * If the value can be represented on fewer bytes than specified, then padding bytes will be added.
     * <p/>
     * No spaces are added between the bytes.
     * <p/>
     * <p/>
     * <p/>
     * For example, a value of 0x3F00 with length 2 would return "3F00" whereas a value of 0x123 with
     * <p/>
     * length 3 would return "000123".
     *
     * @param value  the value to be converted.
     * @param length the number of bytes.
     * @return a string containing the hex bytes.
     * @throws IllegalArgumentException
     *          if the value cannot be represented by the specified number of bytes.
     */
    public static String toHexWord(int value, int length)
    {
        return HexStrings.toHexWord(value, length);
    }

    /**
     * Creates a hex string from the supplied bytes, wrapped into lines of the specified length.
     * @param value the value to be converted.
     * @param lineLength the number of bytes in each line.
     * @return a string representation of the bytes.
     */
    public static String toWrappedHexString(byte[] value, int lineLength)
    {
        return HexStrings.toWrappedHexString(value, lineLength);
    }

    /**
     * Return the first N bytes of the supplied data. If N is greater than the length of the data return the complete
     * data.
     * @param byteData the data to retrieve the first N bytes of
     * @param length the number of bytes to extract from the input data
     * @return the first N bytes of the data or the complete data
     */
    public static byte[] head(byte[] byteData, int length)
    {
        if (byteData.length < length)
        {
            return byteData;
        }
        else
        {
            return Arrays.copyOf(byteData, length);
        }
    }

    /**
     * Return the last N bytes of the supplied data. If N is greater than the length of the data return the complete
     * data.
     * @param byteData the data to retrieve the last N bytes of
     * @param length the number of bytes to extract from the input data
     * @return the last N bytes of the data or the complete data
     */
    public static byte[] tail(byte[] byteData, int length)
    {
        if (byteData.length <= length)
        {
            return byteData;
        }
        else
        {
            int toIndex = byteData.length;
            int fromIndex = toIndex - length;
            return Arrays.copyOfRange(byteData, fromIndex, toIndex);
        }
    }

    /**
     * Extract a sub array from provided input data. The length of the sub array will be truncated if the input data
     * is not long enough.
     * @param byteData the data to extract the sub array from
     * @param index the index into the array to start the sub array at
     * @param length the length of the sub array
     * @return the sub array
     */
    public static byte[] sub(byte[] byteData, int index, int length)
    {
        // Check if we need to truncate the length.
        Preconditions.check(index < byteData.length, "index greater than byte array size");
        Preconditions.check(index >= 0, "index must be greater than 0");

        if (length <= 0)
        {
            return ByteUtils.EMPTY_BYTE_ARRAY;
        }

        if ((index + length) > (byteData.length))
        {
            length = byteData.length - index;
        }

        return Arrays.copyOfRange(byteData, index, index + length);
    }

    /**
     * Returns all but the last N bytes of the supplied data. If N is greater than the length of the data return an
     * empty array.
     * @param byteData the data to retrieve the bytes of
     * @param length the number of bytes not to be extracted from the input data
     * @return all but the last N bytes of the data
     */
    public static byte[] allButLast(byte[] byteData, int length)
    {
        return Bytes.head(byteData, byteData.length >= length ? byteData.length - length : 0);
    }

    /**
     * Returns all but the first N bytes of the supplied data. If N is greater than the length of the data return an
     * empty array.
     * @param byteData the data to retrieve the bytes of
     * @param length the number of bytes not to be extracted from the input data
     * @return all but the first N bytes of the data
     */
    public static byte[] allButFirst(byte[] byteData, int length)
    {
        return tail(byteData, byteData.length >= length ? byteData.length - length : 0);
    }

    /**
     * Create a new byte array with the specified length filled with '00' = 'FF' repeating.
     * @param length the length of the new byte array
     * @return the new byte array
     */
    public static byte[] data(int length)
    {
        if (length <= 0)
        {
            return ByteUtils.EMPTY_BYTE_ARRAY;
        }

        byte[] tmp = new byte[length];

        byte val = 0;
        for (int i = 0; i < length; ++i)
        {
            tmp[i] = val++; // Wraps around to repeat the pattern.
        }

        return tmp;
    }

    /**
     * Create a new byte array with the specified length filled with provided value.
     * @param length the length of the new byte array
     * @param value the byte value (0 - 255) to set all elements of the new array to
     * @return the new byte array
     */
    public static byte[] data(int length, int value)
    {
        if (length <= 0)
        {
            return ByteUtils.EMPTY_BYTE_ARRAY;
        }

        byte[] tmp = new byte[length];

        for (int i = 0; i < length; ++i)
        {
            tmp[i] = (byte)value;
        }

        return tmp;
    }

    /**
     * Creates a new byte array that is a reverse of the input data.
     * @param byteData array to be reversed
     * @return new array which is the reverse of the input data
     */
    public static byte[] reverse(byte[] byteData)
    {
        if (byteData == null)
        {
            return ByteUtils.EMPTY_BYTE_ARRAY;
        }

        byte[] reversingArray = byteData.clone();
        int startPointer = 0;
        int endPointer = reversingArray.length - 1;

        while (endPointer > startPointer)
        {
            byte tempValue = reversingArray[endPointer];
            reversingArray[endPointer] = reversingArray[startPointer];
            reversingArray[startPointer] = tempValue;
            endPointer--;
            startPointer++;
        }

        return reversingArray;
    }

    /**
     * Splits the supplied value into two bytes.
     * @param value the value to be split.
     * @return the byte values.
     * @throws IllegalArgumentException if <code>value</code> is not in the range 0 - 0xFFFF.
     */
    public static byte[] intAsTwoBytes(int value)
    {
        return ByteUtils.splitTwoBytes(value);
    }

    /**
     * Converts up to 4 bytes to an int value. Note that this method does not allow for signing bits so
     * care must be taken when using with 4 bytes.
     *
     * @param bytes the bytes (with most significant byte first).
     * @return the long value.
     * @throws IllegalArgumentException if bytes is longer than 4 bytes.
     */
    public static int bytesToInt(byte... bytes)
    {
        return ByteUtils.bytesToInt(bytes);
    }

    /**
     * Converts the integer value to a byte.
     * @param value the value to be converted.
     * @return the value as a byte.
     * @throws IllegalArgumentException if <code>value</code> is not in the range 0 - 0xFF.
     */
    public static byte toByte(int value)
    {
        return ByteUtils.toByte(value);
    }

    /**
     * Converts the byte to an unsigned integer.
     * @param b the byte to convert.
     * @return an unsigned int.
     */
    public static int toInt(byte b)
    {
        return ByteUtils.toInt(b);
    }

    /**
     * Returns the most significant nibble from the supplied byte value.
     * @param value the byte value.
     * @return the most significant nibble.
     */
    public static int getMSN(byte value)
    {
        return ByteUtils.getMSN(value);
    }

    /**
     * Returns the least significant nibble from the supplied byte value.
     * @param value the byte value.
     * @return the least significant nibble.
     */
    public static int getLSN(byte value)
    {
        return ByteUtils.getLSN(value);
    }

    /**
     * Checks whether the byte sequence <code>array1</code> starts with the byte sequence defined in
     * <code>prefix</code>;
     *
     * @param array the byte array to check.
     * @param prefix the required start bytes.
     * @return <code>true</code> if array1 starts with the byte sequence prefix and <code>false</code>
     * otherwise. If array1 is <code>null</code> or shorter than prefix then this method will return <code>false</code>.
     */
    public static boolean startsWith(byte[] array, byte[] prefix)
    {
        // note order of parameters is reversed!
        return ByteUtils.startsWith(prefix, array);
    }

    /**
     * Creates a byte array which contains <code>value</code> repeated <code>count</code> times.
     * <br>
     * This is equivalent to:
     * <PRE>
     * byte[] bytes = new byte[count];
     * Arrays.fill(bytes, (byte)value);
     * return bytes;
     * </PRE>
     */
    public static byte[] repeated(int count, int value)
    {
        return ByteUtils.repeated(count, value);
    }
}

