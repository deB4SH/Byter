package de.b4sh.byter.utils.data;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Random String Generator class.
 * Usage for tests and other random generation purposes ;-)
 */
public final class StringGenerator {
    private static final SecureRandom random = new SecureRandom();

    private StringGenerator(){
        //nop
    }

    /**
     * Function to create a new random String with a requested length.
     * @param count how many symbols
     * @return String
     */
    public static String nextRandomString(final int count) {
        return new BigInteger(130, random).toString(32).substring(0,count);
    }
}
