package de.b4sh.byter.utils.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Helper Class for communicating with the jmx component of the client.
 * This Class is specific for the Jmx Client Disc part.
 */
public final class JmxClientDiscHelper extends JmxHelper {

    private static final Logger log = Logger.getLogger(JmxClientDiscHelper.class.getName());

    private JmxClientDiscHelper(){
        //nop
    }

    /**
     * Starts a single writer under the set parameters.
     * @param mbs mbean server
     * @param on client disc object name
     */
    public static void startWriter(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"startWriter");
    }

    /**
     * Starts multiple writer under the set parameters.
     * @param mbs mbean server
     * @param on client disc object name
     * @param count count to start
     */
    public static void startMulitpleWriter(final MBeanServerConnection mbs, final ObjectName on, final int count){
        invokeSingleIntegerData(mbs,on,"startMultiWriter",count);
    }

    /**
     * Stops the service pool and resets the environment on the client disc controller.
     * @param mbs mbean server
     * @param on client disc object name
     */
    public static void stopServicePool(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs, on, "stopServicePool");
    }

    /**
     * Rebuilds the folder where the evaluation data is submitted to.
     * @param mbs mbean server
     * @param on object name
     */
    public static void rebuildEvaluationFolder(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"RebuildEvaluationFolder");
    }

    /**
     * get the the flag if the writer is done.
     * @param mbs mbean server
     * @param on object name
     * @return true | false
     */
    public static boolean getActiveWriterDone(final MBeanServerConnection mbs, final ObjectName on){
        return (boolean) getAttribute(mbs,on,"activeWritersDone");
    }

    /**
     * Get the set writer implementation.
     * @param mbs mbean server
     * @param on client disc object name
     * @return String
     */
    public static String getWriterImplementation(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs,on,"WriterImplementation");
    }

    /**
     * Get the output path.
     * @param mbs mbean server
     * @param on client disc object name
     * @return String
     */
    public static String getOutputPath(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs,on,"OutputPath");
    }

    /**
     * Get the chunk size.
     * @param mbs mbean server
     * @param on client disc object name
     * @return int
     */
    public static int getChunkSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs,on,"ChunkSize");
    }

    /**
     * Get the byte target.
     * @param mbs mbean server
     * @param on client disc object name
     * @return long
     */
    public static long getByteTarget(final MBeanServerConnection mbs, final ObjectName on){
        return (long)getAttribute(mbs,on,"ByteTarget");
    }

    /**
     * Get the writer buffer size.
     * @param mbs mbean server
     * @param on client disc object name
     * @return int
     */
    public static int getWriterBufferSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs,on,"WriterBufferSize");
    }

    /**
     * Get the measurement volume.
     * @param mbs mbean server
     * @param on client disc object name
     * @return int
     */
    public static int getMeasurementVolume(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs,on,"MeasurementVolume");
    }

    /**
     * set a new writer implementation.
     * @param mbs mbean server
     * @param on client disc object name
     * @param newImpl key of the writer implementation
     */
    public static void setWriterImplementation(final MBeanServerConnection mbs, final ObjectName on, final String newImpl){
        final String response = invokeSingleStringData(mbs,on,"SetWriterImplementation",newImpl);
        log.log(Level.INFO, "JMX-Response SetWriterImplementation: " + response);
    }

    /**
     * set a new output path.
     * @param mbs mbean server
     * @param on client disc object name
     * @param newPath new path to write to
     */
    public static void setOutputPath(final MBeanServerConnection mbs, final ObjectName on, final String newPath){
        final String response = invokeSingleStringData(mbs,on,"SetOutputPath",newPath);
        log.log(Level.INFO, "JMX-Response SetOutputPath: " + response);
    }

    /**
     * set a new chunk size.
     * @param mbs mbean server
     * @param on client disc object name
     * @param newChunkSize new chunk size
     */
    public static void setChunkSize(final MBeanServerConnection mbs, final ObjectName on, final int newChunkSize){
        invokeSingleIntegerData(mbs,on,"SetChunkSize",newChunkSize);
    }

    /**
     * set a new byte target.
     * @param mbs mbean server
     * @param on client disc object name
     * @param newTarget new target
     */
    public static void setByteTarget(final MBeanServerConnection mbs, final ObjectName on, final long newTarget){
        invokeSingleLongData(mbs,on,"SetByteTarget",newTarget);
    }

    /**
     * set a new writer buffer size.
     * @param mbs mbean server
     * @param on client disc object name
     * @param newBufferSize new buffer size
     */
    public static void setWriterBufferSize(final MBeanServerConnection mbs, final ObjectName on, final int newBufferSize){
        invokeSingleIntegerData(mbs,on,"SetWriterBufferSize", newBufferSize);
    }

    /**
     * set a new measurement volume.
     * size of the list which contains the measurements.
     * @param mbs mbean server
     * @param on client disc object name
     * @param newMeasurement new list size
     */
    public static void setMeasurementVolume(final MBeanServerConnection mbs, final ObjectName on, final int newMeasurement){
        invokeSingleIntegerData(mbs,on,"SetMeasurementVolume",newMeasurement);
    }

    /**
     * function that tests the set parameters.
     * @param mbs mbean server
     * @param on client disc object name
     */
    public static void invokeDiscTest(final MBeanServerConnection mbs, final ObjectName on) {
        invokeNoParams(mbs, on, "DiscWriteTest");
    }

    /**
     * set a new file name to write data to.
     * @param mbs mbean server
     * @param disc client disc object name
     * @param s filename
     */
    public static void setFileName(final MBeanServerConnection mbs, final ObjectName disc, final String s) {
        invokeSingleStringData(mbs,disc,"SetFileName",s);
    }

    /**
     * set a new flag for automatic file removal
     * @param mbs mbean server
     * @param disc client disc object name
     * @param flag flag to set
     */
    public static void setAutomaticFileRemoval(final MBeanServerConnection mbs, final ObjectName disc, final boolean flag) {
        invokeSingleStringData(mbs,disc,"setAutomaticFileRemoval",""+flag);
    }
}
