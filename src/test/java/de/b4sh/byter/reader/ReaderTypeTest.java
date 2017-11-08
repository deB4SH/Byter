/*
 * File: ReaderTypeTest
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-09-27
 * Type: Class
 */
package de.b4sh.byter.reader;

import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import de.b4sh.byter.utils.reader.ReaderType;

public class ReaderTypeTest {

    private static final Logger log = Logger.getLogger(ReaderTypeTest.class.getName());

    /**
     * True expected
     */
    @Test
    public void testCheckRegisteredType(){
        Assert.assertTrue(ReaderType.isTypeRegistered("rafr"));
    }

    /**
     * False expected
     */
    @Test
    public void testCheckRegisteredTypeWrong(){
        Assert.assertFalse(ReaderType.isTypeRegistered("NOPE"));
    }

}
