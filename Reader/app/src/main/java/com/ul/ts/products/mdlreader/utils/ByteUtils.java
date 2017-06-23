package com.ul.ts.products.mdlreader.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides utility methods for working with bytes,
 */
public final class ByteUtils
{
    /**
     * An empty byte array that can be reused.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Mask used to identify bit 8.
     */
    public static final int B8_MASK = 0x80;

    /**
     * Mask used to identify bit 7.
     */
    public static final int B7_MASK = 0x40;

    /**
     * Mask used to identify bit 6.
     */
    public static final int B6_MASK = 0x20;

    /**
     * Mask used to identify bit 5.
     */
    public static final int B5_MASK = 0x10;

    /**
     * Mask used to identify bit 4.
     */
    public static final int B4_MASK = 0x08;

    /**
     * Mask used to identify bit 3.
     */
    public static final int B3_MASK = 0x04;

    /**
     * Mask used to identify bit 2.
     */
    public static final int B2_MASK = 0x02;

    /**
     * Mask used to identify bit 1.
     */
    public static final int B1_MASK = 0x01;

    private ByteUtils()
    {
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "[null array]";
        }
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static byte[] concatByteArrays(byte[] a, byte[] b) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write( a );
            outputStream.write( b );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return outputStream.toByteArray( );
    }

    /**
     * Splits the supplied value into two bytes.
     * @param value the value to be split.
     * @return the byte values.
     * @throws IllegalArgumentException if <code>value</code> is not in the range 0 - 0xFFFF.
     */
    public static final byte[] splitTwoBytes(int value)
    {
        Preconditions.checkRange("value", value, 0, 0xFFFF);
        byte[] split = new byte[2];
        split[0] = (byte) (0xFF & (value >> 8));
        split[1] = (byte) (0xFF & value);
        return split;
    }

    /**
     * Converts two bytes to an integer value.
     * @param msb the most signficant byte.
     * @param lsb the least significant byte.
     * @return the integer value.
     */
    public static final int twoBytesToInt(byte msb, byte lsb)
    {
        return (0xFF00 & (msb << 8)) + (0x00FF & lsb);
    }

    /**
     * Converts up to 8 bytes to a long value. Note that this method does not allow for signing bits so
     * care must be taken when using with 8 bytes.
     *
     * @param bytes the bytes (with most significant byte first).
     * @return the long value.
     *
     * @throws IllegalArgumentException if bytes is longer than 8 bytes.
     */
    public static final long bytesToLong(byte... bytes)
    {
        Preconditions.checkRange("bytes", "length", bytes.length, 0, 8);
        long result = 0;
        int shift = 0;
        for (int i = bytes.length - 1; i >= 0; i--)
        {
            long next = (long)(0xFF & bytes[i]) << shift;
            result += next;
            shift += 8;
        }
        return result;
    }

    /**
     * Converts up to 4 bytes to an int value. Note that this method does not allow for signing bits so
     * care must be taken when using with 4 bytes.
     *
     * @param bytes the bytes (with most significant byte first).
     * @return the long value.
     * @throws IllegalArgumentException if bytes is longer than 4 bytes.
     */
    public static final int bytesToInt(byte... bytes)
    {
        int result = 0;
        int shift = 0;
        for (int i = bytes.length - 1; i >= 0; i--)
        {
            int next = (0xFF & bytes[i]) << shift;
            result += next;
            shift += 8;
        }
        return result;
    }

    /**
     * Converts a segment of a byte array to an int value, using big-endian.
     * Note that this method does not allow for signing bits so care must be taken when using with 4 bytes.
     *
     * @param bytes the bytes (with most significant byte first).
     * @param offset the offset into the byte array at which to start.
     * @param count the number of bytes to be used; must be between 1 and 4.
     * @return the integer value.
     * @throws IllegalArgumentException if the offset or count are invalid as described above,
     * or don't fit correctly within the byte array.
     */
    public static final int bytesToInt(byte[] bytes, int offset, int count)
    {
        Preconditions.checkForNull("bytes", bytes);
        Preconditions.checkLowerRange("offset", offset, 0);
        Preconditions.checkRange("count", count, 1, 4);
        Preconditions.check(offset + count <= bytes.length,
                "offset (" + offset + ") and count (" + count + ") are beyond bytes.length (" + bytes.length + ").");

        int result = 0;
        int shift = 0;
        for (int i = offset + (count - 1); i >= offset; i--)
        {
            int next = (0xFF & bytes[i]) << shift;
            result += next;
            shift += 8;
        }
        return result;
    }

    /**
     * Returns the result of XORing 2 byte arrays. The arrays need not be the same length. In this case, the result
     * will have length set to the smaller of the 2 arrays.
     * @param a array 1
     * @param b array 2
     * @return the result.
     */
    public static byte[] xor(byte[] a, byte[] b)
    {
        int length = Math.min(a.length, b.length);
        byte [] xor = new byte[length];
        for (int i = 0; i < length; i++)
        {
            xor[i] = (byte)(a[i] ^ b[i]);
        }
        return xor;
    }

    /**
     * Applies a mask to the supplied byte array. The mask and the array must be the same length.
     * @param bytes the bytes.
     * @param mask the mask.
     * @return the masked byte array.
     */
    public static byte[] mask(byte[] bytes, byte[] mask)
    {
        Preconditions.check(bytes.length == mask.length, "Bytes and mask must have the same length");
        byte [] masked = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
        {
            masked[i] = (byte)(bytes[i] & mask[i]);
        }
        return masked;
    }

    /**
     * Converts a byte array of even length to an int array of unsigned integers, where each int is made up of 2 bytes
     * from the input array.
     * @throws IllegalArgumentException if data.length is not even.
     */
    public static int[] bytesToUnsigned16BitInts(byte[] byteArray)
    {
        Preconditions.checkForNull("data", byteArray);
        Preconditions.checkLowerRange("data.length", byteArray.length, 2);
        Preconditions.check(byteArray.length % 2 == 0, "data.length must be a factor of 2");

        int[] intArray = new int[byteArray.length/2];
        int intIndex = 0;
        for (int i = 0; i < byteArray.length; i += 2)
        {
            intArray[intIndex++] = twoBytesToInt(byteArray[i], byteArray[i+1]);
        }
        return intArray;
    }

    /**
     * Converts an integer array of unsigned 16 bit integers to a byte array, with each integer represented as 2 bytes,
     * most significant byte first.
     */
    public static byte[] unsigned16BitIntsToBytesMSB(int[] intArray)
    {
        Preconditions.checkForNull("intArray", intArray);

        byte[] result = new byte[intArray.length * 2];
        for (int i = 0; i < intArray.length; i++)
        {
            Preconditions.checkRange("intArray[i]", intArray[i], 0, 0xFFFF);
            System.arraycopy(intToBytes(intArray[i], 2, true), 0, result, (i*2), 2);
        }

        return result;
    }

    /**
     * Converts the integer value to a byte.
     * @param value the value to be converted.
     * @return the value as a byte.
     * @throws IllegalArgumentException if <code>value</code> is not in the range 0 - 0xFF.
     */
    public static final byte toByte(int value)
    {
        Preconditions.checkRange("value", value, 0, 0xFF);
        return (byte)(0xFF & value);
    }

    /**
     * Checks whether the bit in a value is set.
     * @param value the value to be checked.
     * @param bitMask a mask where the required bit is set to 1 and all other bits are set to 0.
     * @return <code>true</code> if the bit is set and <code>false</code> otherwise.
     */
    public static final boolean isBitSet(int value, int bitMask)
    {
        return ((value & bitMask) == bitMask);
    }

    /**
     * Set bits in an existing value.
     * @param value the value whose bits are to be set.
     * @param bitmask a mask defining the bits to be set.
     * @param bitValues the bit values.
     * @return the updated values.
     */
    public static final int setBits(int value, int bitmask, int bitValues)
    {
        int inverseMask = ~bitmask & 0xFF;
        return ((value & inverseMask) | bitValues);
    }

    /**
     * Convenience method which creates a <code>byte</code> array
     * from the specified <code>int</code> <code>elements</code>.
     * <p>
     * All <code>int</code>s are just cast to bytes, so it's up to the caller to ensure they're correctly supplied.
     *
     * @param elements the elements in the array.
     * @return the byte array.
     */
    public static byte[] bytes(int ... elements)
    {
        byte[] array = new byte[elements.length];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = (byte)elements[i];
        }
        return array;
    }

    /**
     * Convenience method to create an array of <code>integer</code>s from the supplied array of <code>byte</code>s.
     * @param bytes the <code>byte</code> array to convert. Clients should note that <code>null</code> is not permitted.
     * @return the corresponding array of <code>integer</code>s.
     */
    public static int[] ints(byte ... bytes)
    {
        Preconditions.checkForNull("bytes", bytes);

        // Create new int array
        int[] ints = new int[bytes.length];

        // Convert each byte to unsigned int
        for(int i = 0; i < bytes.length; i++)
        {
            ints[i] = ByteUtils.toInt(bytes[i]);
        }

        // Return ints
        return ints;
    }

    /**
     * Returns the most significant nibble from the supplied byte value.
     * @param value the byte value.
     * @return the most significant nibble.
     */
    public static final int getMSN(byte value)
    {
        int msn = 0xF0 & value;
        return 0x0F & (msn >> 4);
    }

    /**
     * Sets the most significant nibble of the supplied byte value.
     * @param value the value.
     * @param msn the new most significant nibble.
     * @return the modified value.
     *
     * @throws IllegalArgumentException if {@code value} is not in the range 0 - 0xFF or
     * {@code msn} is not in the range 0 - 0xF.
     */
    public static int setMSN(int value, int msn)
    {
        Preconditions.checkRange("value", value, 0, 0xFF);
        Preconditions.checkRange("msn", msn, 0, 0xF);
        int shifted = 0xFF & (msn << 4);
        return value | shifted;
    }

    /**
     * Returns the least significant nibble from the supplied byte value.
     * @param value the byte value.
     * @return the least significant nibble.
     */
    public static final int getLSN(byte value)
    {
        return 0x0F & value;
    }

    /**
     * Creates a byte array valued from <code>lower</code> to <code>upper</code>.
     * <p>
     * <code>lower</code> and <code>upper</code> must both be in the range from 0 to 255.
     * <p>
     * <code>lower</code> must be less than or equal to <code>upper</code>.
     *
     * @param lower the lower value.
     * @param upper the upper value.
     * @return the range.
     *
     * @throws IllegalArgumentException if the arguments are incorrect as described above.
     */
    public static byte[] range(int lower, int upper)
    {
        Preconditions.checkRange("lower", lower, 0, 255);
        Preconditions.checkRange("upper", upper, 0, 255);
        Preconditions.check(lower <= upper, "lower should be <= upper");

        byte[] bytes = new byte[upper - lower + 1];
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = (byte)(i + lower);
        }
        return bytes;
    }

    /**
     * Check whether the bit at the provided index in the provided byte is set.
     * @param value the value to be checked.
     * @param index the index of the bit to be checked. This uses the normal convention for bits e.g. the least
     * significant bit has index <code>1</code> and the most significant bit has index <code>8</code>.
     * @return <code>true</code> if the bit is set and <code>false</code> otherwise.
     */
    public static final boolean isIndexedBitSet(byte value, int index)
    {
        Preconditions.checkRange("index", index, 1, 8);

        return toBooleanArray(value)[index - 1];
    }

    /**
     * Converts the provided byte into an array of booleans.
     * @param value the value to convert.
     * @return an array of 8 booleans. <code>true</code> shall correspond to a bit set to <code>1</code>, and <code>false</code>
     * shall correspond to <code>0</code>. The array will have the standard ordering i.e. the boolean at the lowest
     * array index shall correspond to the least significant bit. Note that the array uses standard indexing (i.e. 0..7)
     */
    public static final boolean[] toBooleanArray(byte value)
    {
        boolean[] bitArray = new boolean[8];

        bitArray[0] = isBitSet((int)value, B1_MASK);
        bitArray[1] = isBitSet((int)value, B2_MASK);
        bitArray[2] = isBitSet((int)value, B3_MASK);
        bitArray[3] = isBitSet((int)value, B4_MASK);
        bitArray[4] = isBitSet((int)value, B5_MASK);
        bitArray[5] = isBitSet((int)value, B6_MASK);
        bitArray[6] = isBitSet((int)value, B7_MASK);
        bitArray[7] = isBitSet((int)value, B8_MASK);

        return bitArray;
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
    public static final byte[] repeated(int count, int value)
    {
        byte[] bytes = new byte[count];
        Arrays.fill(bytes, (byte)value);
        return bytes;
    }

    /**
     * Checks whether the byte sequence <code>array1</code> starts with the byte sequence defined in
     * <code>prefix</code>;
     * @param prefix the required start bytes.
     * @param array1 the byte array to check.
     * @return <code>true</code> if array1 starts with the byte sequence prefix and <code>false</code>
     * otherwise. If array1 is <code>null</code> or shorter than prefix then this method will return <code>false</code>.
     */
    public static final boolean startsWith(byte[] prefix, byte[] array1)
    {
        if (array1 == null)
        {
            return false;
        }
        if (array1.length < prefix.length)
        {
            return false;
        }
        for (int i = 0; i < prefix.length; i++)
        {
            if (array1[i] != prefix[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the byte sequence <code>array1</code> starts with the byte sequence defined in
     * <code>prefix</code> when the specified mask is applied;
     * @param prefix the required start bytes.
     * @param mask the mask
     * @param array1 the byte array to check.
     * @return <code>true</code> if array1 starts with the byte sequence prefix and <code>false</code>
     * otherwise. If array1 is <code>null</code> or shorter than prefix then this method will return <code>false</code>.
     * @throws IllegalArgumentException if prefix and mask have different lengths.
     */
    public static boolean startsWith(byte[] prefix, byte[] mask, byte[] array1)
    {
        if (mask.length != prefix.length)
        {
            throw new IllegalArgumentException("Mask and prefix must have the same length.");
        }
        if (array1 == null)
        {
            return false;
        }
        if (array1.length < prefix.length)
        {
            return false;
        }
        for (int i = 0; i < prefix.length; i++)
        {
            if ((array1[i] & mask[i]) != prefix[i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if <code>searchString</code> matches the bytes in <code>content</code> starting from
     * <code>offset</code> within <code>content</code>.
     * @param searchString the string of bytes to search for.
     * @param content the content in which to search.
     * @param offset the offset into <code>content</code> at which to start the search.
     */
    public static boolean startsWith(byte[] searchString, byte[] content, int offset)
    {
        if (searchString.length > content.length - offset)
        {
            return false;
        }

        for (int i = 0; i < searchString.length; i++)
        {
            if (searchString[i] != content[offset + i])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Searches for <code>searchString</code> within <code>content</code>, starting from <code>offset</code>.
     * Note that it is legal for <code>searchString</code> to be longer than <code>content.length - offset</code>,
     * although this will result in <code>false</code> being returned.
     * @param searchString the string of bytes to be searched for.
     * @param content the byte content within which to search.
     * @param offset the offset into <code>content</code> at which to start searching.
     * @return <code>true</code> if <code>searchString</code> is found within <code>content</code>, or <code>false</code>
     * otherwise.
     * @throws IllegalArgumentException if <code>offset</code> is outside the size limits of <code>content</code>.
     */
    public static boolean contains(byte[] searchString, byte[] content, int offset)
    {
        Preconditions.checkForNull("searchString", searchString);
        Preconditions.checkForNull("content", content);
        Preconditions.checkRange("offset", offset, 0, content.length - 1);

        // outer loop iterates over content from offset up to searchString.length from the end of content
        for (int i = offset; i < (content.length - (searchString.length - 1)); i++)
        {
            if (startsWith(searchString, content, i))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts the byte to an unsigned int if it less than zero.
     * @param b the byte to convert.
     * @return an unsigned int.
     */
    public static int toInt(byte b)
    {
        return b < 0 ? b & 0xFF : b;
    }

    /**
     * Converts the short to an unsigned int if it less than zero.
     * @param s the short to convert.
     * @return an unsigned int.
     */
    public static int toInt(short s)
    {
        return s < 0 ? s & 0xFFFF : s;
    }

    /**
     * Returns the supplied <code>array</code>, or an empty array if <code>null</code> is supplied.
     */
    public static byte[] allowForNull(byte[] array)
    {
        return ((array != null) ? array : ByteUtils.EMPTY_BYTE_ARRAY);
    }

    /**
     * Converts the provided byte array to a list of nibbles.
     * @param bytes the bytes to be converted.
     * @return an integer list containing the nibbles from the byte array. The nibbles will be in the order MSN first.
     * If the provided byte array had zero length, then the returned list will be empty.
     */
    public static List<Integer> toNibbleList(byte[] bytes)
    {
        Preconditions.checkForNull("bytes", bytes);

        List<Integer> nibbles = new ArrayList<>();
        for (byte nextByte : bytes)
        {
            nibbles.add(getMSN(nextByte));
            nibbles.add(getLSN(nextByte));
        }

        return nibbles;
    }

    /**
     * Converts the provided list of nibbles to a byte array.
     * @param nibbles the list of nibbles in the order MSN first. This list must contain have an even length, so any
     * padding must be added BEFORE the list is provided to this method. All the integers in the list must be in the
     * range 0-F (inclusive).
     * @return a byte array corresponding to the list of nibbles. If the provided list was empty, then a zero length
     * byte array will be returned.
     *
     * @throws IllegalArgumentException if the nibbles list does not have an even number of entries or if any of the
     * integers in the nibbles list are not in the range 0-F.
     */
    public static byte[] fromNibbleList(List<Integer> nibbles)
    {
        Preconditions.checkForNull("nibbles", nibbles);
        Preconditions.check(nibbles.size() % 2 == 0, "nibbles.size() is not an even number");

        byte[] output = new byte[nibbles.size() / 2];

        for(int i=0; i<output.length; i++)
        {
            int msn = nibbles.get(2*i);
            int lsn = nibbles.get(2*i+1);

            Preconditions.checkRange("msn", msn, 0x0, 0xF);
            Preconditions.checkRange("lsn", lsn, 0x0, 0xF);

            int nextByte = (msn << 4) | lsn;

            output[i] = toByte(nextByte);
        }

        return output;
    }

    /**
     * Converts a byte array of up to 4 bytes long into an unsigned integer. Note that this method will throw an
     * {@link IllegalArgumentException} if the unsigned version of MSB is greater than 127 (which would therefore
     * represent a value outside the range of an unsigned integer).
     * @param value the byte array to be converted.
     * @param msbFirst <code>true</code> if <code>value[0]</code> is the MSB, or <code>false</code> if
     * <code>value[0]</code> is the LSB.
     * @throws IllegalArgumentException if <code>value.length</code> > 4, or if the unsigned MSB is greater than 127.
     */
    public static int bytesToInt(byte[] value, boolean msbFirst)
    {
        Preconditions.checkForNull("value", value);
        Preconditions.checkRange("value.length", value.length, 0, 4);

        // MSB may not be greater than 127 in order to avoid signing issues
        Preconditions.check(value.length < 4 || (0xFF & value[msbFirst ? 0 : value.length - 1]) <= 127,
                "The MSB must be less than 128 for a 4-byte value");

        int integer = 0;
        for (int i = 0; i < value.length; i++)
        {
            integer = integer << 8;
            integer = integer | (0xFF & value[msbFirst ? i : value.length - 1 - i]);
        }
        return integer;
    }

    /**
     * Converts an unsigned integer into a fixed-length byte array. Note that this method will throw an
     * {@link IllegalArgumentException} under any of the following conditions:
     * <ul>
     * <li>if <code>length</code> is less than <code>0</code> or greater than <code>4</code>.
     * <li>if <code>value</code> is less than <code>0</code>.
     * <li> if <code>value</code> exceeds the maximum number that can be represented by the specified length.
     * </ul>
     * @param value the value to be converted.
     * @param length the length of the resulting byte array.
     * @param msbFirst <code>true</code> if the MSB should appear at index 0 of the returned array, or
     * <code>false</code> otherwise.
     */
    public static byte[] intToBytes(int value, int length, boolean msbFirst)
    {
        Preconditions.checkRange("length", length, 0, 4);
        Preconditions.checkLowerRange("value", value, 0);
        Preconditions.check(value <= Math.pow(2, length*8) - 1,
                "value too large to be represented by the specified length");

        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++)
        {
            bytes[msbFirst ? length - 1 - i : i] = (byte) (0xFF & value);
            value = value >> 8;
        }

        return bytes;
    }

    /**
     * Converts an integer into an unsigned byte array of the minimum length required. The returned byte array will
     * be MSB formatted, and will be at most 4 bytes long.
     * @throws IllegalArgumentException if <code>value</code> is less than zero.
     */
    public static byte[] intToBytes(int value)
    {
        Preconditions.checkLowerRange("value", value, 0);

        // handle 0 up front
        if (value == 0)
        {
            return Bytes.bytes(0);
        }

        // determine minimum length required by searching for the first non-zero byte from the MSB
        int length = 4;
        for (int i = 0; i < 4; i++)
        {
            int mask = (0xFF << (24 - (i * 8)));
            if ((value & mask) == 0)
            {
                length--;
            }
            else
            {
                break;
            }
        }

        return intToBytes(value, length, true);
    }

    /**
     * Left pads a byte array with a specified value to ensure a minimum size.
     * @param array the array to be padded.
     * @param minSize the minimum size. If <code>array</code> length is less than its size it will be left padded
     * (i.e. bytes will be inserted at index 0).
     * @param value the byte value to use for padding bytes.
     * @return a padded array. Note that if no padding is required the original array will be returned.
     */
    public static byte[] leftPad(byte[] array, int minSize, byte value)
    {
        int delta = minSize - array.length;
        if (delta < 1)
        {
            return array;
        }

        byte[] padded = new byte[minSize];
        System.arraycopy(array, 0, padded, delta, array.length);
        Arrays.fill(padded, 0, delta, value);
        return padded;
    }

    /**
     * Returns a copy of the supplied bytes allowing for {@code null} values.
     * @param bytes the bytes to be copied. This may be {@code null}.
     * @return a copy of the bytes or {@code null} if the input array was {@code null}.
     */
    public static byte[] safeCopy(byte[] bytes)
    {
        return bytes != null ? bytes.clone() : null;
    }

    /**
     * Takes a byte array and splits it into a list of smaller byte arrays of length {@code splitSize}.
     * If the length of the source byte array is not a multiple of {@code splitSize},
     * the last segment will be shorter than {@code splitSize}.
     * @param fullArray The array to be split.
     * @param splitSize The size of the smaller byte arrays.
     * @return A {@link List} of <code>byte[]</code>
     */
    public static List<byte[]> splitByteArray(byte[] fullArray, int splitSize)
    {
        Preconditions.checkForNull("fullArray", fullArray);
        Preconditions.checkLowerRange("splitSize", splitSize, 1);

        List<byte[]> splitByteArray = new ArrayList<>(fullArray.length / splitSize);
        for (int i = 0; i < fullArray.length; i += splitSize)
        {
            splitByteArray.add(Arrays.copyOfRange(fullArray, i, Math.min(i + splitSize, fullArray.length)));
        }
        return splitByteArray;
    }

    /**
     * Perform a bitwise left shift on the entire byte array.
     * @param ba the byte array to be shifted.
     * @param numBitsToShift the number of bits to shift the byte array.
     * @return a new byte array with the shifted value.
     */
    public static byte[] lshiftArray(byte[] ba, int numBitsToShift)
    {
        Preconditions.check(ba != null, "ba must not be null");

        if (ba.length == 0)
        {
            return ByteUtils.EMPTY_BYTE_ARRAY;
        }

        final byte[] sa = Bytes.concatenate(ba);

        int i;
        for (i = 0; i < (ba.length - 1); ++i)
        {
            sa[i] <<= numBitsToShift;
            sa[i] |= ((ba[i + 1] & (0xFF ^ (0xFF >> numBitsToShift))) >> (8 - numBitsToShift));
        }
        sa[i] <<= numBitsToShift;

        return sa;
    }

    /**
     * Create a copy of the provided byte array with the specified length. If the new length is greater than the original
     * byte array the the remaining space is padded with zero.
     *
     * Note that the value is assumed to be big endian and so any padding is prepended to the original value.
     *
     * @param ba the byte array to copy the value from.
     * @param newLength the length of the new byte array.
     * @return the new byte array.
     */
    public static byte[] padLength(byte[] ba, int newLength)
    {
        Preconditions.check(ba != null, "ba must not be null");
        Preconditions.check(newLength >= ba.length, "newLength must be greater than or equal to length of ba");

        if (newLength == 0)
        {
            return ByteUtils.EMPTY_BYTE_ARRAY;
        }

        final byte[] tmp = new byte[newLength];

        final int lengthToCopy = Math.min(newLength, ba.length);
        final int n = newLength - lengthToCopy;
        System.arraycopy(ba, 0, tmp, n, lengthToCopy);

        return tmp;
    }
}