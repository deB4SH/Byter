/*
 * File: OsUtilsTest
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-09
 * Type: Class
 */
package de.b4sh.byter.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import de.b4sh.byter.utils.data.OsUtils;

/**
 * This is more a detection test. not a real function test
 */
public class OsUtilsTest {

    private static final Logger log = Logger.getLogger(OsUtilsTest.class.getName());

    @Test
    public void testUtilsUnderWindows(){
        if(OsUtils.isWindows()){
            log.log(Level.INFO, "OS-Type is: " + OsUtils.getOsName());
            Assert.assertTrue(OsUtils.isWindows());
        }else{
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testUtilsUnderLinux(){
        if(OsUtils.isUnix()){
            log.log(Level.INFO, "OS-Type is: " + OsUtils.getOsName());
            Assert.assertTrue(OsUtils.isUnix());
        }else{
            Assert.assertTrue(true);
        }
    }

}
