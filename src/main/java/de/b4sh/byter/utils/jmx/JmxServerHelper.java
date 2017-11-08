package de.b4sh.byter.utils.jmx;

import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Helper Class for communicating with the jmx component of the server.
 */
public final class JmxServerHelper extends JmxHelper {

    private static final Logger log = Logger.getLogger(JmxServerHelper.class.getName());

    private JmxServerHelper(){
        //nop
    }

    /**
     * get the current state if the commander is done with its task.
     * @param mbs mbean server
     * @param on object name
     * @return boolean if its done
     */
    public static boolean isCurrentStoreDone(final MBeanServerConnection mbs, final ObjectName on){
        return (boolean) getAttribute(mbs,on,"CurrentTestStoreDone");
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
     * Transmit the actual byte target.
     * @param mbs mbean server
     * @param on object name
     * @param byteTarget bytetarget to reach
     */
    public static void setByteTargetForTest(final MBeanServerConnection mbs, final ObjectName on, final long byteTarget){
        invokeSingleLongData(mbs,on,"changeByteTarget",byteTarget);
    }

    /**
     * Set a new measurement volume
     * @param mbs mbean server
     * @param on network manager object name
     * @param count count to set
     */
    public static void setMeasurementVolume(final MBeanServerConnection mbs, final ObjectName on, final int count){
        invokeSingleIntegerData(mbs,on,"changeMeasurementVolume",count);
    }

    /**
     * Get the current set network buffer size at the network manager.
     * @param mbs mbean sserver
     * @param on network manager object name
     * @return int
     */
    public static int getNetworkManagerNetworkBufferSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs,on,"NetworkBufferSize");
    }

    /**
     * get the current set file name at the network manager.
     * @param mbs mbean server
     * @param on network manager object name
     * @return String
     */
    public static String getFileName(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs,on,"FileName");
    }

    /**
     * get the current set writer buffer size at the network manager.
     * @param mbs mbean server
     * @param on network manager object name
     * @return int
     */
    public static int getNetworkManagerWriterBufferSize(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs,on,"WriterBufferSize");
    }

    /**
     * get the current set network type.
     * @param mbs mbean server
     * @param on network manager object name
     * @return String (null possible)
     */
    public static String getNetworkManagerNetworkType(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs,on,"NetworkType");
    }

    /**
     * get the current set storage type.
     * @param mbs mbean server
     * @param on network manager object name
     * @return String (null possible)
     */
    public static String getNetworkManagerStorageType(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs,on,"StorageType");
    }

    /**
     * get the current state of the server socket.
     * @param mbs mbean server
     * @param on network manager object name
     * @return boolean
     */
    public static boolean getServerSocketAccepting(final MBeanServerConnection mbs, final ObjectName on){
        return (boolean)getAttribute(mbs,on,"ServerSocketAccepting");
    }

    /**
     * get the current set file path.
     * @param mbs mbean server
     * @param on network manager object name
     * @return String
     */
    public static String getNetworkManagerFilePath(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs,on,"FilePath");
    }

    /**
     * get the current set server socket port as int.
     * @param mbs mbean server
     * @param on object name
     * @return int (0 default , no active service)
     */
    public static int getNetworkManagerServerSocketPort(final MBeanServerConnection mbs, final ObjectName on){
        return (int)getAttribute(mbs,on,"ServerSocket");
    }

    /**
     * get the current set writer type.
     * @param mbs mbean server
     * @param on network manager object name
     * @return String (null possible)
     */
    public static String getNetworkManagerWriterType(final MBeanServerConnection mbs, final ObjectName on){
        return (String)getAttribute(mbs,on,"WriterType");
    }

    /**
     * set a new network buffer size.
     * @param mbs mbean server
     * @param on network manager object name
     * @param newBufferSize new buffer size
     */
    public static void setNetworkManagerNetworkBufferSize(final MBeanServerConnection mbs, final ObjectName on, final int newBufferSize){
        invokeSingleIntegerData(mbs,on,"changeNetworkBufferSize",newBufferSize);
    }

    /**
     * set a new writer buffer size.
     * @param mbs mbean server
     * @param on network manager object name
     * @param newBufferSize new buffer size
     */
    public static void setNetworkManagerWriterBufferSize(final MBeanServerConnection mbs, final ObjectName on, final int newBufferSize){
        invokeSingleIntegerData(mbs, on,"changeWriterBufferSize",newBufferSize);
    }

    /**
     * set a new writer type.
     * @param mbs mbean server
     * @param on network manager object name
     * @param newType new type that should be set
     * @return String (null possible)
     */
    public static String setNetworkManagerWriterType(final MBeanServerConnection mbs, final ObjectName on, final String newType){
        return invokeSingleStringData(mbs, on,"changeWriterType", newType);
    }

    public static void setAutomaticFileRemoval(final MBeanServerConnection mbs, final ObjectName on, final boolean flag){
        invokeSingleStringData(mbs, on, "setAutomaticFileRemoval", ""+flag);
    }

    /**
     * set a new file name to network manager.
     * @param mbs mbean server
     * @param on network manager object name
     * @param newFileName new filename to set
     */
    public static void setNetworkManagerFileName(final MBeanServerConnection mbs, final ObjectName on, final String newFileName){
        invokeSingleStringData(mbs, on,"changeFileName", newFileName);
    }

    /**
     * set a new network type.
     * @param mbs mbean server
     * @param on network manager object name
     * @param newType new type that should be set
     * @return String (null possible)
     */
    public static String setNetworkManagerNetworkType(final MBeanServerConnection mbs, final ObjectName on, final String newType){
        return invokeSingleStringData(mbs,on,"changeNetworkType", newType);
    }

    /**
     * set a new filepath on network manager mbean.
     * @param mbs mbean server
     * @param on object name
     * @param newPath net path to set
     * @return String
     */
    public static String setNetworkManagerFilePath(final MBeanServerConnection mbs, final ObjectName on, final String newPath){
        return invokeSingleStringData(mbs,on,"changeFilePath", newPath);
    }

    /**
     * set a new filepath on network manager mbean and creates missing directories.
     * @param mbs mbean server
     * @param on object name
     * @param newPath new path to set
     * @return String
     */
    public static String setNetworkManagerFilePathAndCreate(final MBeanServerConnection mbs, final ObjectName on, final String newPath){
        return invokeSingleStringData(mbs,on,"changeFilePathAndCreate", newPath);
    }

    /**
     * start direct store handler on server.
     * @param mbs mbean server
     * @param on network manager object name
     */
    public static void startNetworkManagerDirectStoreHandler(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"DirectStoreHandler");
    }

    /**
     * start archive alike handler on server.
     * @param mbs mbean server
     * @param on network manager object name
     */
    public static void startNetworkManagerArchiveStoreHandler(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"ArchiveStoreHandler");
    }

    /**
     * Shuts down the whole server service (jmxconnector).
     * @param mbs mbean server
     * @param on server controller object name
     */
    public static void shutdownServerService(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"Shutdown");
    }

    /**
     * Shuts down the current running server handler.
     * @param mbs mbean server
     * @param on server network manager object name
     */
    public static void shutdownHandler(final MBeanServerConnection mbs, final ObjectName on){
        invokeNoParams(mbs,on,"ShutdownHandler");
    }


}
