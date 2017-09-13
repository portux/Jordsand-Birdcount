package de.jordsand.birdcensus.util;

/**
 * Collection of useful sanity checks
 * @author Rico Bergmann
 */
public class Assert {

    /**
     * @param obj the object to check
     * @throws IllegalArgumentException if {@code obj == null}
     */
    public static void notNull(Object obj) {
        if (obj == null) {
            reportFailure("Object is null");
        }
    }

    /**
     * @param arr the array to check
     * @throws IllegalArgumentException if any of the array's elements is {@code null}
     */
    public static void elemsNotNull(Object[] arr) {
        for (Object elem : arr) {
            notNull(elem);
        }
    }

    /**
     * @param params the variables to check
     * @throws IllegalArgumentException if any of the parameters is {@code null}
     */
    public static void nonNullParams(Object... params) {
        for (Object param : params) {
            notNull(param);
        }
    }

    /**
     * @param str the string to check
     * @throws IllegalArgumentException if the string is {@code null} or empty
     */
    public static void hasText(String str) {
        if (str == null || str.isEmpty()) {
            reportFailure("String is empty or null");
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} followed by a generic and a specific description
     * @param message the specific part of the exception message
     */
    private static void reportFailure(String message) {
        throw new IllegalArgumentException("Assertion failed: " + message);
    }

}
