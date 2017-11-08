/*
 * File: JmxClientReaderHelper
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-09-27
 * Type: Class
 */
package de.b4sh.byter.utils.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import de.b4sh.byter.utils.reader.ReaderType;

public class JmxClientReaderHelper extends JmxHelper {

    private static final Logger log = Logger.getLogger(JmxClientReaderHelper.class.getName());

    /**
     * Stop the running service pool.
     * @param mbs mbean server
     * @param on client reader object name
     * @return response from jmx
     */
    public static String stopServicePool(final MBeanServerConnection mbs, final ObjectName on){
        return invokeNoParamsWithStatusReturn(mbs,on,"stopServicePool");
    }

    /**
     * Stop the running service pool.
     * @param mbs mbean server
     * @param on client reader object name
     */
    public static void stopServicePoolIgnoreReturn(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"stopServicePool");
    }

    /**
     * Invokes function to start a single reader under given parameters.
     * @param mbs mbean server
     * @param on client reader object name
     */
    public static void startSingleReader(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"startReader");
    }

    /**
     * Invokes function to start multiple reader on this client.
     * @param mbs mbean server
     * @param on client reader object name
     * @param count count how many reader should started
     */
    public static void startMultipleReader(final MBeanServerConnection mbs, final ObjectName on, final int count){
        invokeSingleIntegerData(mbs,on,"startMultiReader",count);
    }

    /**
     * get the current reader pool size.
     * @param mbs mbean server
     * @param on client reader object name
     * @return int with the size of the pool.
     */
    public static int getReaderPoolSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int) getAttribute(mbs,on,"ReaderPoolSize");
    }

    /**
     * get the current set reader type as String.
     * @param mbs mbean server
     * @param on client reader object name
     * @return String with type
     */
    public static String getReaderType(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs,on,"ReaderType");
    }

    /**
     * get the current set reader type.
     * @param mbs mbean server
     * @param on client reader object name
     * @return active ReaderType object
     */
    public static ReaderType getReaderTypeAsType(final MBeanServerConnection mbs, final ObjectName on){
        String param = getReaderType(mbs,on);
        return ReaderType.getTypeByKey(param);
    }

    /**
     * set a new reader type on client side.
     * @see de.b4sh.byter.utils.reader.ReaderType for currently allowed types.
     * @param mbs mbean server
     * @param on client reader object name
     * @param parameter type to set.
     */
    public static void setReaderType(final MBeanServerConnection mbs, final ObjectName on, final String parameter){
        invokeSingleStringData(mbs,on,"setNewReaderType",parameter);
    }

    /**
     * get the current set measurement volume.
     * @param mbs mbean server
     * @param on client reader object name
     * @return int with the amount of measurements that should taken
     */
    public static int getMeasurementVolume(final MBeanServerConnection mbs, final ObjectName on){
        return (int) getAttribute(mbs,on,"MeasurementVolume");
    }

    /**
     * set a new measurement volume limit.
     * @param mbs mbean server
     * @param on client reader object name
     * @param parameter max size of the measurement list
     */
    public static void setMeasurementVolume(final MBeanServerConnection mbs, final ObjectName on, final int parameter){
        invokeSingleIntegerData(mbs,on,"setMeasurementVolume",parameter);
    }

    /**
     * get the current state of the TakeMeasurements flag.
     * @param mbs mbean server
     * @param on client reader object name
     * @return String with the boolean value
     */
    public static String getTakeMeasurements(final MBeanServerConnection mbs, final ObjectName on){
        return (String) getAttribute(mbs,on,"TakeMeasurements");
    }

    /**
     * get the current state of the TakeMeasurements flag as boolean.
     * @param mbs mbean server
     * @param on client reader object name
     * @return boolean | false is the default value or error value
     */
    public static boolean getTakeMeasurementsAsBoolean(final MBeanServerConnection mbs, final ObjectName on){
        String param = getTakeMeasurements(mbs,on);
        if("true".equals(param))
            return true;
        else if("false".equals(param))
            return false;
        else
            log.log(Level.INFO,"JmxClientReaderHelper getTakeMeasurement - JMX returned some weird value for TakeMeasurements: " + param);
        return false;
    }

    /**
     * set a new flag for TakeMeasurements.
     * @param mbs mbean server
     * @param on client reader object name
     * @param flag true | false
     */
    public static void setTakeMeasurements(final MBeanServerConnection mbs, final ObjectName on, final boolean flag){
        invokeSingleStringData(mbs,on,"setTakeMeasurements",String.valueOf(flag));
    }

    /**
     * get the current set chunk size.
     * how many data should be read in one cycle if possible.
     * @param mbs mbean server
     * @param on client reader object name
     * @return int value with the estimated chunk size read.
     */
    public static int getChunkSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int) getAttribute(mbs,on,"ChunkSize");
    }

    /**
     * set a new chunk size for reader.
     * @param mbs mbean server
     * @param on client reader object name
     * @param size size of the chunk as int.
     */
    public static void setChunkSize(final MBeanServerConnection mbs, final ObjectName on, final int size){
        invokeSingleIntegerData(mbs,on,"setChunkSize",size);
    }

    /**
     * get the current set filename to read from.
     * filename should contain the extension too.
     * @param mbs mbean server
     * @param on client reader object name
     * @return String with filename
     */
    public static String getFileName(final MBeanServerConnection mbs, final ObjectName on){
        return (String) getAttribute(mbs,on,"FileName");
    }

    /**
     * set a new fileName to read from.
     * @param mbs mbean server
     * @param on client reader object name
     * @param param new fileName
     */
    public static void setFileName(final MBeanServerConnection mbs, final ObjectName on, final String param){
        invokeSingleStringData(mbs,on,"setFileName",param);
    }

    /**
     * get the current set filepath to the located file.
     * @param mbs mbean server
     * @param on client reader object name
     * @return file path to head to
     */
    public static String getFilePath(final MBeanServerConnection mbs, final ObjectName on){
        return (String) getAttribute(mbs,on,"Path");
    }

    /**
     * set a new file path.
     * @param mbs mbean server
     * @param on client reader object name
     * @param param path to file
     */
    public static void setFilePath(final MBeanServerConnection mbs, final ObjectName on, final String param){
        invokeSingleStringData(mbs,on,"setPath",param);
    }

}
