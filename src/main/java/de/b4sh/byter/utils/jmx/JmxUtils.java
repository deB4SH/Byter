package de.b4sh.byter.utils.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * JmxUtil Class.
 * Place for every jmx related function that won't fit anywhere else.
 */
public final class JmxUtils {

    private static final Logger log = Logger.getLogger(JmxUtils.class.getName());
    private static final String basePackage = "de.b4sh.byter";
    private static int writerId;

    private JmxUtils(){
        //nop
    }

    /**
     * creates a ObjectName under the basePackage with given type.
     * @param type desired type
     * @param concatRandomInt should the type contain a int concatenated
     * @return null(default) | instanced ObjectName
     */
    public static ObjectName objectName(final String type, final boolean concatRandomInt){
        return objectName(basePackage,type,concatRandomInt);
    }

    /**
     * creates a ObjectName with the basePackage name + concatPackage and selected type.
     * @param type desired type
     * @param concatRandomInt should the type contain a int concatenated
     * @param concatPackage add something to the base package
     * @return null(default) | instanced ObjectName
     */
    public static ObjectName objectName(final String type, final boolean concatRandomInt, final String concatPackage){
        return objectName(basePackage+"."+concatPackage,type,concatRandomInt);
    }

    /**
     * creates a ObjectName with specific packagename and type.
     * @param packageName desired packageName
     * @param type desired type
     * @param concatRandomInt concat a int at the type
     * @return null(default) | instanced ObjectName
     */
    public static ObjectName objectName(final String packageName, final String type, final boolean concatRandomInt){
        try{
            if(!concatRandomInt)
                return new ObjectName( packageName+":type="+type);
            else{
                writerId++;
                return new ObjectName( packageName+":type="+type+"-id-"+writerId);
            }
        } catch (MalformedObjectNameException e) {
            log.log(Level.WARNING, "ObjectName is malformed!");
            return null;
        }
    }

}
