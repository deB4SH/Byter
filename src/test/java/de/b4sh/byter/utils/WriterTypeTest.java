/* ----------------------------------------------------------------------------
 * file: WriterTypeTest
 * project: Byter
 * by: Steve Golling
 *
 * Copyright (c) 2017, All rights reserved.
 * Max-Planck-Institut für Plasmaphysik. W7-X CoDaC group.
 *----------------------------------------------------------------------------
 */
 /* First version date: 2017-09-12*/
package de.b4sh.byter.utils;

import org.junit.Assert;
import org.junit.Test;

import de.b4sh.byter.utils.writer.WriterType;

public class WriterTypeTest {

    @Test
    public void testIfImplementationIsCorrect(){
        Assert.assertTrue(WriterType.isImplementationAvailable("buff"));
        Assert.assertFalse(WriterType.isImplementationAvailable("false"));
    }

}
