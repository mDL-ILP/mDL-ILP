package com.ul.ts.products.mdlreader.utils;


import java.util.Arrays;
import java.util.Collection;


/**
 * Provides convenience methods to perform precondition checks on parameters in a method body. Each method will throw an
 * appropriate exception if the check fails. For example
 * <pre>
 * public void myMethod(String name, int value, int[] data)
 * {
 *     Preconditions.checkForNull("name", name);
 *     Preconditions.checkForNull("data", data);
 *     Preconditions.checkRange("data", "length", data.length, 0, 6);
 * <p/>
 *     // method body
 *     ...
 * }
 * </pre>
 */
public class Preconditions
{

    /**
     * Checks whether the supplied integer value is a valid byte value (i.e. in the range 0 - 0xFF.
     * @param name the name of the value.
     * @param value the value.
     */
    public static void checkByteValue(String name, int value)
    {
        if (value < 0 || value > 0xFF)
        {
            throw new IllegalArgumentException(name + " is not a valid byte value.");
        }
    }

    /**
     * Checks that the specifed class assignable from the class of the specified value. i.e. checks that the
     * value is an instance of the specified class.
     * @param name the name of the value being checked.
     * @param value the value to be checked.
     * @param requiredClass the required class.
     * @throws IllegalArgumentException if the class is not assignable from the class of the value.
     */
    public static final void checkAssignableFrom(String name, Object value, Class requiredClass)
    {
        if (!requiredClass.isAssignableFrom(value.getClass()))
        {
            throw new IllegalArgumentException(name + " is of type " + value.getClass().getName()
                    + " : expected " + requiredClass.getName());
        }
    }

    /**
     * Checks whether a parameter is <code>null</code>.
     *
     * @param name      the name of the parameter.
     * @param parameter the parameter to check.
     * @throws NullPointerException if the parameter is <code>null</code>.
     */
    public static final void checkForNull(String name, Object parameter)
    {
        if (parameter == null)
        {
            throw new NullPointerException(name + " parameter is null.");
        }
    }

    /**
     * Checks for <code>parameter</code> being <code>null</code>,
     * or any element in <code>parameter</code> being <code>null</code>.
     * @throws NullPointerException if <code>parameter</code> is <code>null</code>.
     * @throws IllegalArgumentException if any element is <code>null</code>.
     */
    public static final <T> void checkForNullElements(String parameterName, T[] parameter)
    {
        Preconditions.checkForNull(parameterName, parameter);
        for (T value : parameter)
        {
            if (value == null)
            {
                throw new IllegalArgumentException(
                        parameterName + " (" + Arrays.toString(parameter) + ") contains one or more null elements.");
            }
        }
    }

    /**
     * Checks for <code>parameter</code> being <code>null</code>,
     * or any element in <code>parameter</code> being <code>null</code>.
     * @throws NullPointerException if <code>parameter</code> is <code>null</code>.
     * @throws NullPointerException if <code>iterable</code> itself
     * @throws IllegalArgumentException if any element is <code>null</code>.
     */
    public static final <T> void checkForNullElements(String parameterName, Iterable<T> parameter)
    {
        Preconditions.checkForNull(parameterName, parameter);
        for (T value : parameter)
        {
            if (value == null)
            {
                throw new IllegalArgumentException(
                        parameterName + " (" + parameter + ") contains one or more null elements.");
            }
        }
    }

    /**
     * Checks that the value of a parameter is between specified limits.
     *
     * @param name        the parameter name.
     * @param valueString the name of the value being checked (e.g. "size" or "length").
     * @param value       the value of the parameter (e.g. the length of an array or size of a collection).
     * @param min         the minimum length.
     * @param max         the maximum length.
     * @throws IllegalArgumentException if the length is &lt; the min or &gt; the max lengths.
     */
    public static final void checkRange(String name, String valueString, int value, int min, int max)
    {
        if (value < min)
        {
            throw new IllegalArgumentException(name + " " + valueString + ": " + value + " is less than " + min);
        }
        if (value > max)
        {
            throw new IllegalArgumentException(name + " " + valueString + ": " + value + " is greater than " + max);
        }
    }

    /**
     * Checks that the value of a parameter is between specified limits.
     *
     * @param name  the parameter name.
     * @param value the value of the parameter.
     * @param min   the minimum value.
     * @param max   the maximum value.
     * @throws IllegalArgumentException if the value is &lt; the min or &gt; the max values.
     */
    public static final void checkRange(String name, int value, int min, int max)
    {
        if ((value < min) || (value > max))
        {
            throw new IllegalArgumentException(name + ": " + value + " is not between " + min + " and " + max);
        }
    }

    /**
     * Checks that the value of a parameter is between specified limits.
     *
     * @param name  the parameter name.
     * @param value the value of the parameter.
     * @param min   the minimum value.
     * @param max   the maximum value.
     * @throws IllegalArgumentException if the value is &lt; the min or &gt; the max values.
     */
    public static final void checkRange(String name, long value, long min, long max)
    {
        if ((value < min) || (value > max))
        {
            throw new IllegalArgumentException(name + ": " + value + " is not between " + min + " and " + max);
        }
    }

    /**
     * Checks that the value of a parameter is between specified limits.
     *
     * @param name  the parameter name.
     * @param value the value of the parameter.
     * @param min   the minimum value.
     * @param max   the maximum value.
     * @throws IllegalArgumentException if the value is &lt; the min or &gt; the max values.
     */
    public static final void checkRange(String name, double value, double min, double max)
    {
        if (value < min)
        {
            throw new IllegalArgumentException(name + ": " + value + " is less than " + min);
        }
        if (value > max)
        {
            throw new IllegalArgumentException(name + ": " + value + " is greater than " + max);
        }
    }

    /**
     * Checks that an int parameter value is equal to an expected value.
     * @param paramName the name of the parameter being checked.
     * @param value the value of the parameter.
     * @param expected the expected value.
     */
    public static final void checkEquals(String paramName, int value, int expected)
    {
        if (value != expected)
        {
            throw new IllegalArgumentException(paramName + " value: " + value + " does not equal " + expected + ".");
        }
    }

    /**
     * Checks that two int parameters have the same value.
     * @param param1Name the name of the first parameter.
     * @param value1 the first parameter value.
     * @param param2Name the name of the second parameter.
     * @param value2 the second parameter value.
     */
    public static final void checkEquals(String param1Name, int value1, String param2Name, int value2)
    {
        if (value1 != value2)
        {
            throw new IllegalArgumentException(param1Name + " value: " + value1 + " does not equal " + param2Name
                    + " value: " + value2);
        }
    }

    /**
     * Checks that <code>test</code> is <code>true</code>. If it is not <code>true</code>, an
     * <code>IllegalArgumentException</code> will be thrown.
     *
     * @param test         the test to be done.
     * @param errorMessage the error message for when <code>test</code> is <code>false</code>.
     * @throws IllegalArgumentException if <code>test</code> is <code>false</code>.
     */
    public static final void check(boolean test, String errorMessage)
    {
        if (!test)
        {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Checks that an index is within specified bounds.
     * @param name the name of the index parameter.
     * @param index the index value.
     * @param min the minimum allowed value.
     * @param max the maximum allowed value.
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is not in the specified bounds.
     * @throws IllegalArgumentException if <code>min</code> is &gt; <code>max</code>.
     */
    public static final void checkIndexBounds(String name, int index, int min, int max)
    {
        Preconditions.check(min <= max, "min: " + min + " is greater than max: " + max);
        if (index < min)
        {
            throw new IndexOutOfBoundsException(
                    name + ": " + index + " is less than the minimum allowed value: " + min);
        }
        if (index > max)
        {
            throw new IndexOutOfBoundsException(
                    name + ": " + index + " is greater than the maximum allowed value: " + max);
        }
    }

    /**
     * Checks that the length or size of a parameter is at least <code>min</code>.
     *
     * @param name  the parameter name.
     * @param value the value of the parameter (e.g. the length of an array or size of a collection).
     * @param min   the minimum length.
     * @throws IllegalArgumentException if <code>value &lt; min</code>.
     */
    public static final void checkLowerRange(String name, int value, int min)
    {
        if (value < min)
        {
            throw new IllegalArgumentException(name + ": " + value + " is less than " + min);
        }
    }

    /**
     * Checks that the length or size of a parameter is at least <code>min</code>.
     *
     * @param name  the parameter name.
     * @param value the value of the parameter (e.g. the length of an array or size of a collection).
     * @param min   the minimum length.
     * @throws IllegalArgumentException if <code>value &lt; min</code>.
     */
    public static final void checkLowerRange(String name, long value, long min)
    {
        if (value < min)
        {
            throw new IllegalArgumentException(name + ": " + value + " is less than " + min);
        }
    }

    /**
     * Checks that the length or size of a parameter is at most <code>max</code>.
     *
     * @param name  the parameter name.
     * @param value the value of the parameter (e.g. the length of an array or size of a collection).
     * @param max   the maximum length.
     * @throws IllegalArgumentException if <code>value ;gt; max</code>.
     */
    public static final void checkUpperRange(String name, int value, int max)
    {
        if (value > max)
        {
            throw new IllegalArgumentException(name + ": " + value + " is greater than " + max);
        }
    }

    /**
     * Checks that the length or size of a parameter is at most <code>max</code>.
     *
     * @param name  the parameter name.
     * @param value the value of the parameter (e.g. the length of an array or size of a collection).
     * @param max   the maximum length.
     * @throws IllegalArgumentException if <code>value ;gt; max</code>.
     */
    public static final void checkUpperRange(String name, long value, long max)
    {
        if (value > max)
        {
            throw new IllegalArgumentException(name + ": " + value + " is greater than " + max);
        }
    }

    /**
     * Checks that <code>test</code> is <code>true</code>.
     * <p/>
     * If it is not <code>true</code>, an <code>IllegalStateException</code> will be thrown.
     *
     * @param test         the test to be done.
     * @param errorMessage the error message for when <code>test</code> is <code>false</code>.
     * @throws IllegalStateException if <code>test</code> is <code>false</code>.
     */

    public static final void checkState(boolean test, String errorMessage)
    {
        if (!test)
        {
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * Checks that the lengths of all the arrays in <code>arrays</code> are between <code>minInclusive </code>
     * and <code>maxInclusive</code>.
     * @throws IllegalArgumentException if an array does not meet the above criteria.
     */
    public static final void checkArrayRanges(String collectionName, Collection<byte[]> arrays,
                                              int minInclusive, int maxInclusive)
    {
        Preconditions.checkForNull(collectionName, arrays);
        int index = 0;
        for (byte[] array : arrays)
        {
            Preconditions.checkForNull(collectionName + "[" + index + "]", array);
            Preconditions.checkRange(collectionName + "[" + index + "].length", array.length, minInclusive, maxInclusive);
            index++;
        }
    }

    /**
     * Checks that the length of the supplied byte array has the specified length.
     * @param name the name of the array.
     * @param array the array.
     * @param requiredLength the required length.
     * @throws IllegalArgumentException if the array does not have the specified length.
     */
    public static void checkByteArrayLength(String name, byte[] array, int requiredLength)
    {
        Preconditions.checkForNull(name, array);
        if (array.length != requiredLength)
        {
            throw new IllegalArgumentException(name + " does not contain " + requiredLength + " bytes");
        }
    }
}
