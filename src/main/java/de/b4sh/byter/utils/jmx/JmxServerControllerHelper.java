/*
 * File: JmxServerControllerHelper
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-10
 * Type: Class
 */
package de.b4sh.byter.utils.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * JmxServerControllerHelper class.
 * UNUSED
 */
public class JmxServerControllerHelper extends JmxHelper {

    /**
     * Function to rebuild the evaluation folder.
     * UNUSED AND UNTESTS!
     * @param mbs mbean server
     * @param on jmx controller mbean
     */
    public void rebuildEvaluationFolder(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"RebuildEvaluationFolder");
    }

}
