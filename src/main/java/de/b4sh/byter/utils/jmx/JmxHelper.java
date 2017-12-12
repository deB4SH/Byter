/*
 * File: JmxHelper
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-21
 * Type: Class
 */
package de.b4sh.byter.utils.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;

/**
 * Generic Jmx Helper Class.
 * This class supports both main-helper classes (client, server). Just to keep the basic function at one place.
 */
class JmxHelper {

    private static final Logger log = Logger.getLogger(JmxHelper.class.getName());

    /**
     * package private constructor.
     */
    JmxHelper(){
        //nop
    }

    /**
     * Get the MBeanServerConnection.
     * @param connector active JmxConnector
     * @return the mbeanserverconnection from given jmxconnector
     */
    public static MBeanServerConnection getMBeanServer(final JMXConnector connector){
        try{
            return connector.getMBeanServerConnection();
        } catch (final IOException e) {
            log.log(Level.WARNING,"IO Exception during connecting or getting MBeanServer.",e);
        }
        return null;
    }

    /**
     * Find all MBeans with the given canonical name in it.
     * Used for looking which MBeans are registered to one namespace.
     *      eg. java.net.xyz
     * @param mbs MBeanServerConnection to corresponding server.
     * @param canonicalNamePart canonical name you are looking for.
     * @return ObjectName or Null
     */
    public static List<ObjectName> findObjectNames(final MBeanServerConnection mbs, final String canonicalNamePart){
        final List<ObjectName> names = new ArrayList<ObjectName>();
        try{
            for(final ObjectName on: mbs.queryNames(null,null)){
                if(on.getCanonicalName().contains(canonicalNamePart))
                    names.add(on);
            }
            return names;
        } catch (final IOException e) {
            log.log(Level.WARNING, "IO Exception during browse for ObjectNames with canonicalNamePart. \n"
                    + "See Stacktrace for more Information",e);
        }
        return null;
    }

    /**
     * Find the first MBean with given canonical name and type.
     * @param mbs MBeanServerConnection to corresponding server
     * @param canonicalNamePart canonical name you are looking for
     * @param type given type for the object
     * @return ObjectName or null (default)
     */
    public static ObjectName findObjectName(final MBeanServerConnection mbs, final String canonicalNamePart, final String type){
        try{
            for(final ObjectName on: mbs.queryNames(null,null)){
                final String fullObjectName = on.getCanonicalName();
                if(fullObjectName.contains(canonicalNamePart)){ //check if the canonicalNamePart is ok
                    if(fullObjectName.contains(":type")){//make sure its a "valid" objectname
                        if(fullObjectName.split(":type")[1].contains(type)){//found your type
                            return on;
                        }
                    }
                }
            }
        } catch (final IOException e) {
            log.log(Level.WARNING, "IO Exception during browse for ObjectNames with canonicalNamePart and given type. \n"
                    + "See Stacktrace for more Information",e);
        }
        return null;
    }

    /**
     * Get JMX-Operations from a specific object.
     * @param mb corresponding MBeanServerConnection
     * @param on ObjectName on MBeanServer
     * @return List of invokable JMX-Operations found
     * @throws IntrospectionException IntrospectionException
     * @throws ReflectionException ReflectionException
     * @throws InstanceNotFoundException InstanceNotFoundException
     * @throws IOException IOException
     */
    public static List<MBeanOperationInfo> getOperations(final MBeanServerConnection mb, final ObjectName on)
            throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        final List<MBeanOperationInfo> operationInfos = new ArrayList<>();
        Collections.addAll(operationInfos, mb.getMBeanInfo(on).getOperations());
        return operationInfos;
    }

    /**
     * WrapperFunction for getting an attribute of an objectName.
     * This function is secured through try-catch.
     * @param mb mbeanserver
     * @param on corresponding objectname
     * @param attributeName attributename you're looking for!
     * @return null(default) | object
     */
    static Object getAttribute(final MBeanServerConnection mb, final ObjectName on, final String attributeName){
        try{
            return mb.getAttribute(on,attributeName);
        } catch (final ReflectionException | InstanceNotFoundException | AttributeNotFoundException | MBeanException e) {
            log.log(Level.WARNING, "MBean Issue at getting Attribute from MBeanServer. Attribute: " + attributeName,e);
        } catch (final IOException e) {
            log.log(Level.WARNING,"IO Exception while getting Attribute from MBeanServer",e);
        }
        return null;
    }

    /**
     * Invoke function with a single int as parameter.
     * @param mbs mbeanserver
     * @param on objectname
     * @param functionName desired function
     * @param pass data to pass to function
     */
    static void invokeSingleIntegerData(final MBeanServerConnection mbs, final ObjectName on, final String functionName,final int pass){
        try{
            final Integer[] data = new Integer[1];
            data[0] = pass;
            mbs.invoke(on,functionName,data,new String[]{"int"});
        } catch (final ReflectionException | InstanceNotFoundException | MBeanException e) {
            log.log(Level.WARNING,"ReflectionException | InstanceNotFoundException | MBeanException during " + functionName + " invoke.");
        } catch (final IOException e) {
            log.log(Level.WARNING, "Generic IO Exception. Check Stacktrace.",e);
        }
    }

    /**
     * Invoke function with a single long as parameter.
     * @param mbs mbean server
     * @param on object name
     * @param functionName desired function
     * @param pass data to pass to function
     */
    static void invokeSingleLongData(final MBeanServerConnection mbs, final ObjectName on, final String functionName, final long pass){
        try{
            final Long[] data = new Long[1];
            data[0] = pass;
            mbs.invoke(on,functionName,data,new String[]{"long"});
        } catch (final ReflectionException | InstanceNotFoundException | MBeanException e) {
            log.log(Level.WARNING,"ReflectionException | InstanceNotFoundException | MBeanException during " + functionName + " invoke.");
        } catch (final IOException e) {
            log.log(Level.WARNING, "Generic IO Exception. Check Stacktrace.",e);
        }
    }

    /**
     * Invoke function with a single String as parameter.
     * Catching the returned lines (if they exist) and returns them.
     * @param mbs mbeanserver
     * @param on objectname
     * @param functionName desired function
     * @param pass data to pass to function
     */
    static String invokeSingleStringData(final MBeanServerConnection mbs, final ObjectName on, final String functionName, final String pass){
        try{
            final String[] data = new String[1];
            data[0] = pass;
            return (String)mbs.invoke(on,functionName,data,new String[]{"java.lang.String"});
        } catch (final ReflectionException | InstanceNotFoundException | MBeanException e) {
            log.log(Level.WARNING,"ReflectionException | InstanceNotFoundException | MBeanException during " + functionName + " invoke.");
        } catch (final IOException e) {
            log.log(Level.WARNING, "Generic IO Exception. Check Stacktrace.",e);
        }
        return null;
    }

    /**
     * Invoke a parameterless function.
     * @param mbs mbeanserver
     * @param on objectname
     * @param functionName function name to invoke
     */
    static void invokeNoParams(final MBeanServerConnection mbs, final ObjectName on, final String functionName){
        try{
            mbs.invoke(on,functionName,null,null);
        } catch (final ReflectionException | InstanceNotFoundException | MBeanException e) {
            log.log(Level.WARNING,"ReflectionException | InstanceNotFoundException | MBeanException during " + functionName + " invoke.",e);
        } catch (final IOException e) {
            log.log(Level.WARNING, "Generic IO Exception. Check Stacktrace.",e);
        }
    }

    /**
     * Invoke a parameterless function.
     * @param mbs mbeanserver
     * @param on objectname
     * @param functionName function name to invoke
     */
    static String invokeNoParamsWithStatusReturn(final MBeanServerConnection mbs, final ObjectName on, final String functionName){
        try{
            return (String)mbs.invoke(on,functionName,null,null);
        } catch (final ReflectionException | InstanceNotFoundException | MBeanException e) {
            log.log(Level.WARNING,"ReflectionException | InstanceNotFoundException | MBeanException during " + functionName + " invoke.",e);
        } catch (final IOException e) {
            log.log(Level.WARNING, "Generic IO Exception. Check Stacktrace.",e);
        }
        return null;
    }

    /**
     * Unregister all mbeans by given namepart.
     * @param mbs mbean server
     * @param canonicalNamePart canonical name
     */
    public static void unregisterEveryUserMBean(final MBeanServerConnection mbs, final String canonicalNamePart){
        final ArrayList<ObjectName> onList = (ArrayList<ObjectName>) findObjectNames(mbs,canonicalNamePart);
        for(ObjectName on: onList){
            try {
                mbs.unregisterMBean(on);
            } catch (InstanceNotFoundException | MBeanRegistrationException e) {
                log.log(Level.WARNING, "Seems that the MBean is already unregistered. "
                        + "Instance Not Found or Registration Exception on unregisterUserMbeans");
            } catch (IOException e) {
                log.log(Level.WARNING, "IO Exception during unregistering every user mbean. check stacktrace",e);
            }
        }
    }
}
