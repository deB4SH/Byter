package de.b4sh.byter.utils.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Subclass for JmxEntities for behavior among all entites.
 */
public class JmxEntity {
    private static final Logger log = Logger.getLogger(JmxEntity.class.getName());
    private ObjectName objectName;

    /**
     * public constructor with base initialisation.
     * init-style= packageName:type=type
     * @param packageName desired packageName
     * @param type desired type of mbean
     */
    public JmxEntity(final String packageName, final String type) {
        try{
            this.objectName = new ObjectName( packageName+":type="+type);
        } catch (MalformedObjectNameException e) {
            log.log(Level.WARNING, "ObjectName is malformed!");
        }
    }

    /**
     * Get the current set object name.
     * @return ObjectName
     */
    public ObjectName getObjectName() {
        return objectName;
    }
}
