package de.b4sh.byter.utils.jmx;

import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import de.b4sh.byter.utils.exception.FunctionNotImplementedException;

/**
 * Helper Class for communicating with the jmx component of the client.
 * This class is specific for the Jmx Client Network part!
 */
public final class JmxClientNetworkHelper extends JmxHelper {

    private static final Logger log = Logger.getLogger(JmxClientNetworkHelper.class.getName());

    private JmxClientNetworkHelper(){
        //nop
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
     * get the current set measurement volume amount.
     * @param mbs mbean server
     * @param on client network mbean
     * @return amount of targeted tracked data
     */
    public static int getMeasurementVolume(final MBeanServerConnection mbs, final ObjectName on){
        return (int) getAttribute(mbs,on,"MeasurementVolume");
    }

    /**
     * set a new measurement volume.
     * @param mbs mbean server
     * @param on client network object name
     * @param param new measurement value to use
     */
    public static void setMeasurementVolume(final MBeanServerConnection mbs, final ObjectName on, final int param){
        invokeSingleIntegerData(mbs,on,"setMeasurementVolume",param);
    }

    /**
     * get the current take measurements flag.
     * @param mbs mbean server
     * @param on client network object name
     * @return boolean with the current flag
     */
    public static boolean getTakeMeasurements(final MBeanServerConnection mbs, final ObjectName on){
        return (boolean) getAttribute(mbs,on,"TakeMeasurements");
    }

    /**
     * set a new flag for take measurements.
     * @param mbs mbean server
     * @param on client network object name
     * @param param boolean to set
     */
    public static void setTakeMeasurements(final MBeanServerConnection mbs, final ObjectName on, final boolean param){
        invokeSingleStringData(mbs,on,"setTakeMeasurements", ""+param);
    }

    /**
     * get the current set test name.
     * @param mbs mbean server
     * @param on client network object name
     * @return String with the current set test name
     */
    public static String getTestName(final MBeanServerConnection mbs, final ObjectName on){
        return (String) getAttribute(mbs,on,"TestName");
    }

    /**
     * set a new test name at the client.
     * @param mbs mbean server
     * @param on client network object name
     * @param name name to set
     */
    public static void setTestName(final MBeanServerConnection mbs, final ObjectName on, final String name){
        invokeSingleStringData(mbs,on,"setTestName",name);
    }

    /**
     * get the actualGeneratedChunkSize via jmx.
     * @param mbs mbean server
     * @param on client network mbean
     * @return int
     */
    public static int getActualGeneratedChunkSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs,on,"ActualGeneratedChunkSize");
    }

    /**
     * get ClientExecutorService Status via jmx.
     * @param mbs mbean server
     * @param on client network mbean
     * @return boolean
     */
    public static boolean getExecutorServiceStatus(final MBeanServerConnection mbs, final ObjectName on){
        return (boolean)getAttribute(mbs,on,"ExecutorStatus");
    }

    /**
     * get NetworkBufferSize via jmx.
     * @param mbs mbean server
     * @param on client network mbean
     * @return int
     */
    public static int getNetworkBufferSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs, on, "NetworkBufferSize");
    }

    /**
     * get set host address.
     * @param mbs mbean server
     * @param on client network mbean
     * @return String
     */
    public static String getServerHostAddress(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs, on, "ServerIp");
    }

    /**
     * get host port.
     * @param mbs mbean server
     * @param on client network mbean
     * @return int
     */
    public static int getServerHostPort(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs, on, "ServerPort");
    }

    /**
     * get the targeted pregeneration chunk size.
     * @param mbs mbean server
     * @param on client network mbean
     * @return int
     */
    public static int getTargetPregenChunkSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs, on, "TargetPregenChunkSize");
    }

    /**
     * get the transmission goal of the set task.
     * @param mbs mbean server
     * @param on client network mbean
     * @return long
     */
    public static long getTransmitTarget(final MBeanServerConnection mbs, final ObjectName on){
        return (long)getAttribute(mbs, on, "TransmitTarget");
    }

    /**
     * Get the status of the task completion.
     * true (done) | false working
     * @param mbs mbean server
     * @param on client network mbean
     * @return  flag if task is fulfilled or not
     */
    public static boolean getTaskFulfilled(final MBeanServerConnection mbs, final ObjectName on){
        return (boolean)getAttribute(mbs,on,"LastTaskFulfilled");
    }

    /**
     * change the currently set host address.
     * @param mbs mbean server
     * @param on client network mbean
     * @param newHost new host address to use
     */
    public static void changeServerHostAddress(final MBeanServerConnection mbs, final ObjectName on, final String newHost){
        invokeSingleStringData(mbs,on,"NewServerIp",newHost);
    }

    /**
     * change the currently set host port.
     * @param mbs mbean server
     * @param on client network mbean
     * @param newPort new host port to use
     */
    public static void changeServerHostPort(final MBeanServerConnection mbs, final ObjectName on, final int newPort){
        invokeSingleIntegerData(mbs, on, "NewServerPort", newPort);
    }

    /**
     * change the currently set transmission target for file transfer.
     * @param mbs mbean server
     * @param on client network mbean
     * @param newTarget new transmission target
     */
    public static void changeTransmitTarget(final MBeanServerConnection mbs, final ObjectName on, final long newTarget){
        invokeSingleLongData(mbs, on, "NewTransmitTarget", newTarget);
    }

    /**
     * change the currently set pregenerated chunk size for file transfer.
     * @param mbs mbean server
     * @param on client network mbean
     * @param newChunkSize new chunk size to pre generated
     */
    public static void changePregenChunkSize(final MBeanServerConnection mbs, final ObjectName on, final int newChunkSize){
        invokeSingleIntegerData(mbs,on,"NewPregenChunkSize",newChunkSize);
    }

    /**
     * Start a Plain Network Server, that transmits the set informations.
     * @param mbs mbean server
     * @param on client network mbean
     */
    public static void startPlainNetwork(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs, on, "StartPlainNetwork");
    }

    /**
     * change the network buffer size used in a network handler.
     * @param mbs mbean server
     * @param on client network mbean
     * @param newBuffer new buffer to start with
     */
    public static void changeNetworkBufferSize(final MBeanServerConnection mbs, final ObjectName on, final int newBuffer){
        invokeSingleIntegerData(mbs, on, "changeNetworkBufferSize", newBuffer);
    }

    /**
     * Shuts down the whole client service (jmxconnector).
     * @param mbs mbean server
     * @param on client controller object name
     */
    public static void shutdownClientService(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"Shutdown");
    }

    /**
     * Invoke a disc write test on client disc jmx.
     * @param mbs mbean server
     * @param on client disc mbean
     */
    public static void invokeDiscWriteTest(final MBeanServerConnection mbs, final ObjectName on){
        //TODO: implement get and set befor and than invoke func.
        throw new FunctionNotImplementedException("invokeDiscWriteTest");
    }
}
