package de.b4sh.byter.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import de.b4sh.byter.utils.data.StringGenerator;

public final class StringGeneratorTest {

    private static final Logger log = Logger.getLogger(StringGenerator.class.getName());

    @Test
    public void testGenerator(){
        final String test = StringGenerator.nextRandomString(10);
        Assert.assertNotNull(test);
        log.log(Level.INFO,"Random generated String: " + test);
        Assert.assertEquals(10,test.length());
    }
}
