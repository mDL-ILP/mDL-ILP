package com.ul.ts.products.mdlreader.utils;


import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.StringTokenizer;


/**
 * Provides methods for generating strings from hex data.
 */
public final class HexStrings
{
    // private constructor, so this class can't be instantiated
    private HexStrings()
    {
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

    public static final String toHexWord(int value, int length)
    {
        String conversion = Integer.toHexString(value).toUpperCase();

        // check that the conversion has an even number of characters (i.e. is coded on a whole number of bytes).

        if (conversion.length() % 2 != 0)
        {
            conversion = '0' + conversion;
        }

        int padding = length - (conversion.length() / 2);

        if (padding < 0)
        {
            throw new IllegalArgumentException("Cannot represent " + value + " as a string containing "

                    + length + " bytes");
        }
        if (padding > 0)
        {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < padding; i++)
            {
                buffer.append("00");
            }

            buffer.append(conversion);
            conversion = buffer.toString();
        }
        return conversion;

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
    public static final String longToHexWord(long value, int length)
    {
        String conversion = Long.toHexString(value).toUpperCase();

        // check that the conversion has an even number of characters (i.e. is coded on a whole number of bytes).

        if (conversion.length() % 2 != 0)
        {
            conversion = '0' + conversion;
        }

        int padding = length - (conversion.length() / 2);

        if (padding < 0)
        {
            throw new IllegalArgumentException("Cannot represent " + value + " as a string containing "

                    + length + " bytes");
        }
        if (padding > 0)
        {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < padding; i++)
            {
                buffer.append("00");
            }

            buffer.append(conversion);
            conversion = buffer.toString();
        }
        return conversion;

    }

    /**
     * Returns a string containing a Hex representation of the provided integer. The string will show a whole number of
     * bytes i.e. F is displayed as 0F, A3B is displayed as 0A3B.
     * @param value the integer to be displayed as a hex string
     * @return the integer as a hex string
     */
    public static final String toHexWord(int value)
    {
        String conversion = Integer.toHexString(value).toUpperCase();

        // check that the conversion has an even number of characters (i.e. is coded on a whole number of bytes).

        if (conversion.length() % 2 != 0)
        {
            conversion = '0' + conversion;
        }

        return conversion;
    }


    /**
     * Returns a string containing hex values from the supplied byte array. The string contains space separated byte
     * values. Each byte will be represented by 2 digits.
     *
     * @param value the byte array to be converted.
     * @return a string representation of the byte array.
     */

    public static final String toHexString(byte[] value)
    {
        if (value == null) {
            return "[null array]";
        }
        StringBuffer buffer = new StringBuffer(value.length * 3);
        for (int i = 0; i < value.length; i++)
        {
            buffer.append(HexStrings.toHexWord((0xFF & value[i]), 1));
            if (i < value.length - 1)
            {
                buffer.append(' ');
            }
        }
        return buffer.toString();
    }

    /**
     * Returns a string containing the hex value for the supplied byte, represented by 2 digits.
     * @param value the byte to be converted.
     * @return a string representation of the byte.
     */
    public static final String toHexString(byte value)
    {
        return HexStrings.toHexWord((0xFF & value), 1);
    }

    /**
     * Creates a hex string from the supplied bytes, wrapped into lines of the specified length.
     * @param value the value to be converted.
     * @param lineLength the number of bytes in each line.
     * @return a string representation of the bytes.
     */
    public static final String toWrappedHexString(byte[] value, int lineLength)
    {
        int count = 0;
        StringBuffer buffer = new StringBuffer(value.length * 3);
        for (int i = 0; i < value.length; i++)
        {
            buffer.append(HexStrings.toHexWord((0xFF & value[i]), 1));
            if (i < value.length - 1)
            {
                buffer.append(' ');
            }
            count++;
            if (count == lineLength)
            {
                buffer.append('\n');
                count = 0;
            }
        }
        return buffer.toString();
    }

    /**
     * Returns a string containing hex values from the supplied int array.
     * <p/>
     * The string contains space separated int values.
     * <p/>
     * Each int will be represented as described in {@link #toHexWord(int, int)}.
     *
     * @param value      the int array to be converted.
     * @param wordLength the number of bytes in each word.
     * @return a string representation of the int array.
     */

    public static final String toHexString(int[] value, int wordLength)
    {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < value.length; i++)
        {
            if (i > 0)
            {
                buffer.append(' ');
            }

            buffer.append(HexStrings.toHexWord(value[i], wordLength));
        }

        return buffer.toString();

    }


    /**
     * Generates a byte array by parsing byte values from the supplied string.
     * <p/>
     * The string should contain space separated hexidecimal values e.g. "FF FF FF FF".
     *
     * @param hexString the string containing the hex values.
     * @return a byte array containing the hex values.
     * @throws NumberFormatException if the string cannot be converted to a byte array.
     */
    public static final byte[] fromHexString(String hexString) throws NumberFormatException
    {
        StringTokenizer tokenizer = new StringTokenizer(hexString);

        byte[] value = new byte[tokenizer.countTokens()];

        int index = 0;

        while (tokenizer.hasMoreTokens())
        {
            int nextByte = Integer.parseInt(tokenizer.nextToken(), 16);
            if (nextByte < 0 || nextByte > 0xFF)
            {

                throw new NumberFormatException(nextByte + " is not a valid byte value");
            }

            value[index] = (byte) (0xFF & nextByte);
            index++;
        }

        return value;

    }

    /**
     * Generates a byte array by parsing byte values from the supplied string.
     * <p/>
     * The string should contain hexadecimal values without spaces e.g. "FFFFFFFF". The conversion is case insensitive,
     * and the input string will be trimmed using {@link String#trim()} before being used.
     *
     * @param unspacedHexString the string containing the hex values. Cannot be <code>null</code>.
     * @return a byte array containing the hex values.
     * @throws NumberFormatException if the string cannot be converted to a byte array. This may occur if the trimmed string
     * has an odd number of characters, or contains characters that are outside the permitted range for hex values,
     * or if the integer value of a two character pair is outside the permitted range for a byte value.
     */
    public static final byte[] fromUnspacedHexString(String unspacedHexString) throws NumberFormatException
    {
        Preconditions.checkForNull("unspacedHexString", unspacedHexString);

        unspacedHexString = unspacedHexString.trim();

        if(unspacedHexString.length() % 2 != 0)
        {
            throw new NumberFormatException("Trimmed unspacedHexString contains odd number of characters");
        }

        int size = unspacedHexString.length() / 2;

        byte[] value = new byte[size];

        for(int i=0; i<size; i++)
        {
            String nextByteString = unspacedHexString.substring(i * 2, (i + 1) * 2);
            int nextByte = Integer.parseInt(nextByteString, 16);

            if (nextByte < 0 || nextByte > 0xFF)
            {

                throw new NumberFormatException(nextByte + " is not a valid byte value");
            }

            value[i] = (byte)(0xFF & nextByte);
        }

        return value;
    }

    /**
     * Generates a byte array by parsing byte values from the supplied string.
     * <p/>
     * The string may contain hexadecimal values with or without spaces e.g. "FF FF FFFFFFFF FF".
     * The conversion is case insensitive and the input string will be trimmed using {@link String#trim()}
     * before being used. Whitespace (including line breaks) should be on byte boundaries. For example
     * <pre>
     * AABBCC
     * DDEEFF
     * </pre>
     * and not
     * <pre>
     * AABBCCD
     * DEEFF
     * </pre>
     *
     * @param hexString the string containing the hex values. Cannot be <code>null</code>.
     * @return a byte array containing the hex values.
     * @throws NumberFormatException if the string cannot be converted to a byte array. This may occur if the trimmed string
     * has an odd number of characters, or contains characters that are outside the permitted range for hex values,
     * or if the integer value of a two character pair is outside the permitted range for a byte value.
     */
    public static byte[] fromMixedFormatHexString(String hexString) throws NumberFormatException
    {
        Preconditions.checkForNull("hexString", hexString);
        hexString = hexString.trim();
        StringTokenizer tokenizer = new StringTokenizer(hexString);

        // the maximum number of bytes that can be in the string is half of the string length.
        ByteArrayOutputStream stream = new ByteArrayOutputStream(hexString.length()/2);
        while (tokenizer.hasMoreTokens())
        {
            String next = tokenizer.nextToken();
            if (next.length() > 2)
            {
                byte[] unspacedBytes = fromUnspacedHexString(next);
                for (byte unspacedByte : unspacedBytes)
                {
                    stream.write(unspacedByte);
                }
            }
            else if (next.length() == 2)
            {
                int nextByte = Integer.parseInt(next, 16);
                if (nextByte < 0 || nextByte > 0xFF)
                {
                    throw new NumberFormatException(nextByte + " is not a valid byte value");
                }
                stream.write((byte) (0xFF & nextByte));
            }
            else
            {
                throw new NumberFormatException(next + " is not a digit hex byte value.");
            }
        }
        return stream.toByteArray();
    }

    /**
     * Returns a string containing hex values from the supplied byte array. The string contains no spaces between byte
     * values. Each byte will be represented by 2 digits, with any letter characters (A-F) as uppercase.
     *
     * @param value the byte array to be converted.
     * @return a string representation of the byte array.
     */
    public static final String toUnspacedHexString(byte[] value)
    {
        StringBuilder builder = new StringBuilder(value.length * 2);
        for(byte nextByte:value)
        {
            builder.append(HexStrings.toHexWord((0xFF & nextByte), 1));
        }
        return builder.toString();
    }

    /**
     * Returns a hex string wrapped in single quotes. For example '00 1A 2B'.
     * @param value the byte values.
     * @return the string.
     */
    public static String toQuotedHexString(byte[] value)
    {
        StringBuilder builder = new StringBuilder("'");
        builder.append(toHexString(value));
        builder.append("'");
        return builder.toString();
    }

    /**
     * Returns a hex word wrapped in single quotes. For example '001A'.
     * @param value the byte values.
     * @return the string.
     */
    public static String toQuotedHexWord(byte[] value)
    {
        StringBuilder builder = new StringBuilder("'");
        builder.append(toUnspacedHexString(value));
        builder.append("'");
        return builder.toString();
    }

    /**
     * Returns a hex byte wrapped in single quotes. For example '1A'.
     * @param value the byte value.
     * @return the string.
     */
    public static String toQuotedHexByte(byte value)
    {
        StringBuilder builder = new StringBuilder("'");
        builder.append(toHexString(value));
        builder.append("'");
        return builder.toString();
    }

    /**
     * Returns a "summary" of the supplied bytes.
     * <br/>
     * If the length of the bytes is less than or equal to {@code maxLength}, {@link #toHexString(byte[])} will be returned.
     * <br/>
     * Otherwise (if the length is greater than {@code maxLength}), the first {@code maxLength} bytes will be returned,
     * followed by {@code "..."}.
     * <p>
     * For example, {@code HexStrings.toSummaryHexString(Bytes.bytes("01 02 03 04"), 6)} will return {@code "01 02 03 04"};
     * <br/>
     * while, {@code HexStrings.toSummaryHexString(Bytes.bytes("01 02 03 04"), 2)} will return {@code "01 02 ..."}.
     */
    public static String toSummaryHexString(byte[] bytes, int maxLength)
    {
        Preconditions.checkForNull("bytes", bytes);
        Preconditions.checkLowerRange("maxLength", maxLength, 1);
        if (bytes.length <= maxLength)
        {
            return HexStrings.toHexString(bytes);
        }
        else
        {
            return HexStrings.toHexString(Arrays.copyOf(bytes, maxLength)) + " ...";
        }
    }
}