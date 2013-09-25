package broadwick.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Useful methods for acting on arrays.
 */
public final class ArrayUtils {

    /**
     * Hidden Utility constructor.
     */
    private ArrayUtils() {
    }

    /**
     * Get the maximum element in an array of longs.
     * @param longs the array to search.
     * @return the maximum value in the array.
     */
    public static long max(final long[] longs) {
        long max = 0l;
        for (int j = 0; j < longs.length; j++) {
            max = (longs[j] > max) ? longs[j] : max;
        }
        return max;
    }

    /**
     * Get the sum of an array of doubles.
     * @param doubles the array to sum.
     * @return the sum of the array
     */
    public static Double sum(final Double[] doubles) {
        Double sum = 0.0;
        for (int j = 0; j < doubles.length; j++) {
            sum += doubles[j];
        }
        return sum;
    }


    /**
     * Get the sum of an array of integers.
     * @param integers the array to sum.
     * @return the sum of the array
     */
    public static Integer sum(final Integer[] integers) {
        Integer sum = 0;
        for (int j = 0; j < integers.length; j++) {
            sum += integers[j];
        }
        return sum;
    }

    /**
     * Modify an array by multiplying each element by a constant scaling factor.
     * @param longs the array to scale.
     * @param scale the scaling factor
     */
    public static void scale(long[] longs, final double scale) {
        for (int j = 0; j < longs.length; j++) {
            longs[j] *= scale;
        }
    }

    /**
     * Given an array containing double values as string convert them to Doubles.
     * @param arr the String[] to be converted.
     * @return the converted Double array.
     */
    public static Double[] toDoubleArray(final String[] arr) {
        Double[] doubles = new Double[arr.length];
        for (int i = 0; i < arr.length; ++i) {
            doubles[i] = Double.valueOf(arr[i]);
        }
        return doubles;
    }

    /**
     * Given a comma separated string containing double values as strings convert them to an
     * array of Doubles.
     * @param str the string containing comma separated double values.
     * @return the converted Double array.
     */
    public static Double[] toDoubleArray(final String str) {
        return toDoubleArray(toStringArray(str));
    }

    /**
     * Create a  new ArrayList from a comma separated list of ints encoded as string values.
     * @param str a comma separated list of double values
     * @return an ArrayList of the strings converted to ints.
     */
    public static List<Double> toArrayListDouble(final String str) {
        return new ArrayList<>(java.util.Arrays.asList(toDoubleArray(str)));
    }

    /**
     * Given an array containing integer values as string convert them to integers.
     * @param arr the String[] to be converted.
     * @return the converted integer array.
     */
    public static Integer[] toIntegerArray(final String[] arr) {
        Integer[] ints = new Integer[arr.length];
        for (int i = 0; i < arr.length; ++i) {
            ints[i] = Integer.valueOf(arr[i]);
        }
        return ints;
    }

    /**
     * Given a comma separated string containing integer values as strings convert them to an
     * array of integers.
     * @param str the string containing comma separated double values.
     * @return the converted integer array.
     */
    public static Integer[] toIntegerArray(final String str) {
        return toIntegerArray(toStringArray(str));
    }

    /**
     * Create a  new ArrayList from a comma separated list of integers encoded as string values.
     * @param str a comma separated list of integer values
     * @return an ArrayList of the strings converted to integers.
     */
    public static List<Integer> toArrayListInteger(final String str) {
        return new ArrayList<>(java.util.Arrays.asList(toIntegerArray(str)));
    }

    /**
     * Split a string into an array strings,ignoring whitespace around each string.
     * @param str the string to split
     * @param token the (char) token used to split the string.
     * @return an array of strings.
     */
    public static String[] toStringArray(final String str, final Pattern token) {
        return Iterables.toArray(Splitter.on(token).trimResults().split(str), String.class);
    }

       /**
     * Split a string into an array strings,ignoring whitespace around each string.
     * @param str the string to split
     * @param token the (char) token used to split the string.
     * @return an array of strings.
     */
    @SuppressWarnings("all")
    public static String[] toStringArray(String str, char token) {
        if (str.indexOf(token) > 0) {
            return Iterables.toArray(Splitter.on(token).trimResults().split(str), String.class);
        } else {
            String[] arr = {str};
            return arr;
        }
    }

    /**
     * Split a string into an array strings, ignoring whitespace around each string. The token used to split is the comma.
     * @param str the string to split
     * @return an array of strings.
     */
    public static String[] toStringArray(final String str) {
        return toStringArray(str, ',');
    }

    /**
     * Split a string into a collection of strings, ignoring whitespace around each string. The token used to split is the comma.
     * @param str the string to split
     * @return a collection of strings.
     */
    public static Collection<String> toCollectionStrings(final String str) {
        if (str.isEmpty()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.asList(toStringArray(str));
    }

    /**
     * Create a csv string for an array of Objects.
     * @param objects the array of objects.
     * @return a csv string of the array.
     */
    public static String toString(final Object[] objects) {
        final StringBuilder sb = new StringBuilder();
        for (Object o : objects) {
            sb.append(o.toString());
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Create a csv string for an array of Objects.
     * @param objects the array of objects.
     * @param fmt the formatter to beapplied to each element.
     * @return a csv string of the array.
     */
    public static String toString(final Object[] objects, final Format fmt) {
        final StringBuilder sb = new StringBuilder();
        for (Object o : objects) {
            try {
                sb.append(fmt.format(o));
            } catch (IllegalArgumentException iae) {
                sb.append(o.toString());
            }
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
