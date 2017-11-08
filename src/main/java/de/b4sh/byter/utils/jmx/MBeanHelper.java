package de.b4sh.byter.utils.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.udojava.jmx.wrapper.JMXBeanWrapper;

/**
 * single point for registering und unregister elements from the mbean server.
 */
public class MBeanHelper {
    private static final Logger log = Logger.getLogger(MBeanHelper.class.getName());
    private final MBeanServer mbs;

    /**
     * private constructor that sets the MBeanServer.
     * @param mbs running mbean server instance
     */
    public MBeanHelper(final MBeanServer mbs) {
        this.mbs = mbs;
    }

    /**
     * Register a JMXWrapper based object to mbean server.
     * @param objectToRegister object you want to register
     * @param objectName under which object name it should be available
     */
    public void registerElement(final Object objectToRegister, final ObjectName objectName){
        try{
            final JMXBeanWrapper wrapper = new JMXBeanWrapper(objectToRegister);
            this.mbs.registerMBean(wrapper, objectName);
        } catch (final IntrospectionException | NotCompliantMBeanException | MBeanRegistrationException | InstanceAlreadyExistsException e) {
            log.log(Level.WARNING, "Error during initialisation or registration of Object to MBeanserver."
                    + " see Stracktrace for more Information", e);
        }
    }

    /**
     * Register a static build mbean to the mbean server.
     * @param objecToRegister object you want to register
     * @param objectName under which object name it should be available
     */
    public void registerStaticElement(final Object objecToRegister, final ObjectName objectName){
        try {
            this.mbs.registerMBean(objecToRegister,objectName);
        } catch (InstanceAlreadyExistsException | NotCompliantMBeanException | MBeanRegistrationException e) {
            log.log(Level.WARNING,"Could not register static example mbean on MBeanServer. Check Stacktrace.", e);
        }
    }

    /**
     * Unregister element from mbean server.
     * @param objectName object name to object
     */
    public void unregisterElement(final ObjectName objectName){
        try{
            this.mbs.unregisterMBean(objectName);
        } catch (InstanceNotFoundException e) {
            log.log(Level.WARNING, "Cannot find element under the passed objectName. " + objectName.toString());
        } catch (MBeanRegistrationException e) {
            log.log(Level.WARNING, "Cannot unregister element from mbean server");
        }
    }
}
