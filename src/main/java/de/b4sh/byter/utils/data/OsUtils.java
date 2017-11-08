/*
 * File: OsUtils
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-09
 * Type: Class
 */
package de.b4sh.byter.utils.data;

/**
 * Class for checking the underlying operting system.
 */
public final class OsUtils {

    private static String currentOs = null;

    /**
     * Constructor
     */
    public OsUtils(){
        //NOP
    }

    /**
     * Get the current OS type as String.
     * @return String with the OS type.
     */
    public static String getOsName(){
        if(currentOs == null){
            currentOs = System.getProperty("os.name");
        }
        return currentOs;
    }

    /**
     * Checks if underlying os is Windows.
     * @return boolean
     */
    public static boolean isWindows(){
        return getOsName().startsWith("Windows");
    }

    /**
     * Checks if the underlying os is Linux.
     * @return boolean
     */
    public static boolean isUnix(){
        return getOsName().startsWith("Linux");
    }
}
