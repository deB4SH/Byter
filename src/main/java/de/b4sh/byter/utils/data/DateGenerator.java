/*
 * File: DateGenerator
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-29
 * Type: Class
 */
package de.b4sh.byter.utils.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generator Class for date specirfic functions.
 */
public final class DateGenerator {
    private static final Logger log = Logger.getLogger(DateGenerator.class.getName());
    private static final DateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
    private static final DateFormat dateFormatTime = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
    private static final DateFormat dateConstruct = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);

    private DateGenerator(){
        //nop
    }

    /**
     * Generated a yyyy-MM-dd String synchronized.
     * @return String with Date
     */
    public static synchronized String generateTodayString(){
        final Date today = new Date();
        return dateFormatDay.format(today);
    }

    /**
     * Generated a HH:mm:ss String synchronized.
     * @return String with Date
     */
    public static synchronized String generateTimeString(){
        final Date today = new Date();
        return dateFormatTime.format(today);
    }

    /**
     * Get the current stime as a string for file usage.
     * Replaces the : with an underline.
     * @return String that can be used in filenames
     */
    public static synchronized String generateTimeStringForFile(){
        return generateTimeString().replace(":","_");
    }

    /**
     * Generated a HH:mm:ss String synchronized.
     * @return String with Date
     */
    static synchronized String generateTimeStringForDisc(){
        final Date today = new Date();
        return dateFormatTime.format(today).replace(":","_");
    }

    /**
     * Generate Date from String.
     * @param input input String
     * @return Date | null (default)
     */
    public static synchronized Date generateDateFromTimeString(final String input){
        try {
            return dateFormatTime.parse(input);
        } catch (ParseException e) {
            log.log(Level.WARNING,"ParseException during input parse: " + input,e);
        }
        return null;
    }
}
