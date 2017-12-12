package de.b4sh.byter.server;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import com.udojava.jmx.wrapper.JMXBeanParameter;
import de.b4sh.byter.server.network.NetworkType;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.jmx.JmxEntity;
import de.b4sh.byter.utils.writer.WriterType;

/**
 * JmxEntity Class for NetworkManager.
 */
@JMXBean(description = "Byter.Server.NetworkManager")
public final class ServerJmxNetworkManager extends JmxEntity {

    private static final Logger log = Logger.getLogger(ServerJmxController.class.getName());
    private final NetworkManager networkManager;
    private NetworkType networkType;
    private WriterType writerType;
    private int networkBufferSize;
    private int writerBufferSize;

    /**
     * public constructor with base initialisation.
     * @param packageName package the jmx entity should be registered under
     * @param type the type the jmx entity should be registered under
     * @param networkManager networkManager object to switch between states for active tests
     */
    ServerJmxNetworkManager(final String packageName, final String type, final NetworkManager networkManager) {
        super(packageName, type);
        this.networkManager = networkManager;
        this.networkType = NetworkType.None;
        this.writerType = WriterType.None;
        this.networkBufferSize = 8 * 1024;
        this.writerBufferSize = 8 * 1024;
        this.networkManager.setFilePath(System.getProperty("user.dir"));
        this.passInitialValuesToNetworkManager();
    }

    /**
     * Pass the initial value to the NetworkManager.
     * Else these fields are null initialised and it may come to unwanted behavior of the application.
     */
    private void passInitialValuesToNetworkManager(){
        this.networkManager.setWriterType(this.writerType);
        this.networkManager.setNetworkType(this.networkType);
    }

    /**
     * is the current store done with its task?
     * @return flag if task is done or not (true, false)
     */
    @JMXBeanAttribute(name = "CurrentTestStoreDone", description = "is the current set implementation at the store done?")
    public boolean isStoreDone(){
        return this.networkManager.isTestSzenarioImplementationDone();
    }

    /**
     * rebuild the evaluation folder
     */
    @JMXBeanOperation(name = "RebuildEvaluationFolder", description = "rebuild the evaluation folder")
    public void rebuildEvaluationFolder(){
        FileManager.rebuildEvaluationFolder(this.networkManager.getFileName());
    }

    /**
     * get the current set measurement volume.
     * @return int value with the measurement volume.
     */
    @JMXBeanAttribute(name = "MeasurementVolume", description = "Get the count of how many measurements should be fulfilled")
    public int getMeasurementVolume(){
        return this.networkManager.getMeasurementVolume();
    }

    /**
     * Set a new measurement volume.
     * @param volume limit of new measurement tracking list
     */
    @JMXBeanOperation(name = "changeMeasurementVolume", description = "Set a new measurement count")
    public void setMeasurementVolume(
        @JMXBeanParameter(name = "newVolume", description = "new maximum tracking size")final int volume
    ){
        this.networkManager.setMeasurementVolume(volume);
    }

    /**
     * get the current byte target as long.
     * @return byte target
     */
    @JMXBeanAttribute(name = "ByteTarget", description = "Get the current set byte target.")
    public long getByteTarget(){
        return this.networkManager.getByteTarget();
    }

    /**
     * Set a new byte target as long.
     * @param byteTarget byte target
     */
    @JMXBeanOperation(name = "changeByteTarget", description = "Set a new byte target for this test.")
    public void setNewByteTarget(
        @JMXBeanParameter(name = "newTarget", description = "new target to reach")final long byteTarget
    ){
        this.networkManager.setByteTarget(byteTarget);
    }

    /**
     * get the current flag for taking network measurements.
     * @return true or false
     */
    @JMXBeanAttribute(name = "TakeNetworkMeasurements", description = "Is NetworkManager taking network measurements?")
    public boolean getTakeNetworkMeasurements(){
        return this.networkManager.isTakeNetworkMeasurements();
    }

    /**
     * get the current flag for taking writer measurements.
     * @return true or false
     */
    @JMXBeanAttribute(name = "TakeWriterMeasurements", description = "Is NetworkManager taking writer measurements?")
    public boolean getTakeWriterMeasurements(){
        return this.networkManager.isTakeWriterMeasurements();
    }

    /**
     * set a new flag for take network measurements.
     * @param param1 true or false as string
     */
    @JMXBeanOperation(name = "changeTakeNetworkMeasurements", description = "Change the state of taking network measurements.")
    public void setNewMeasurementNetworkState(
        @JMXBeanParameter(name = "newState", description = "the new state")final String param1
    ){
        final String parameter = String.valueOf(param1);//convert into a real string
        if("true".equals(parameter))
            this.networkManager.setTakeNetworkMeasurements(true);
        else if("false".equals(parameter))
            this.networkManager.setTakeNetworkMeasurements(false);
        else
            log.log(Level.INFO, "Server JmxNetworkManager \n Received a wrong flag for changing state of network measurements. received: "
                                        + parameter + " | current state: " + this.networkManager.isTakeNetworkMeasurements());
    }

    /**
     * set a new flag for take writer measurements.
     * @param param1 true or false as string
     */
    @JMXBeanOperation(name = "changeTakeWriterMeasurements", description = "Change the state of taking writer measurements.")
    public void setNewMeasurementWriterState(
            @JMXBeanParameter(name = "newState", description = "the new state")final String param1
    ){
        final String parameter = String.valueOf(param1);//convert into a real string
        if("true".equals(parameter))
            this.networkManager.setTakeWriterMeasurements(true);
        else if("false".equals(parameter))
            this.networkManager.setTakeWriterMeasurements(false);
        else
            log.log(Level.INFO, "Server JmxNetworkManager \n Received a wrong flag for changing state of writer measurements. received: "
                    + parameter + " | current state: " + this.networkManager.isTakeNetworkMeasurements());
    }
    /**
     * JMX function for starting an direct server.
     * stores the data up to byte[BufferSize] and saves the data when full
     * @return String message that DirectStore is started
     */
    @JMXBeanOperation(name = "DirectStoreHandler",description = "Starts a server with a direct byte handling")
    public String startDirectStore(){
        this.networkManager.startDirectStore(this.networkBufferSize,this.writerBufferSize);
        return "DirectStore successfully started!";
    }

    /**
     * JMX function for changing the network buffer size.
     * @param networkBufferSize the desired network buffer size
     */
    @JMXBeanOperation(name = "changeNetworkBufferSize", description = "change the service buffer size")
    public void changeNetworkBufferSize(
        @JMXBeanParameter(name = "bufferSize",description = "actual new buffer size for service") final int networkBufferSize
    ){
        this.networkBufferSize = networkBufferSize;
    }

    /**
     * JMX function for changing the writer buffer size.
     * @param writerBufferSize the desired writer buffer size
     */
    @JMXBeanOperation(name = "changeWriterBufferSize", description = "change the writer buffer size")
    public void changeWriterBufferSize(
        @JMXBeanParameter(name = "bufferSize", description = "actual new buffer size for writer") final int writerBufferSize
    ){
        this.writerBufferSize = writerBufferSize;
    }

    /**
     * Get the current used storage type.
     * @return String with StorageType
     */
    @JMXBeanAttribute(name = "StorageType", description = "currently used storage type")
    public String getStorageType(){
        return this.networkManager.getCurrentActiveStoreType().toString();
    }

    @JMXBeanAttribute(name = "AutomaticFileRemoval", description = "currently flag for automatic file removal")
    public boolean getAutomaticRemove(){
        return this.networkManager.isAutomaticFileRemoval();
    }

    @JMXBeanOperation(name = "setAutomaticFileRemoval", description = "set a new flag for automatic file removal")
    public void setAutomaticFileRemoval(
        @JMXBeanParameter(name = "flag", description = "flag to set")final String flag
    ){
        final String value = String.valueOf(flag);
        if("true".equals(value))
            this.networkManager.setAutomaticFileRemoval(true);
        else if("false".equals(value))
            this.networkManager.setAutomaticFileRemoval(false);
    }

    /**
     * Get the current used writer type.
     * @return String with the WriterType
     */
    @JMXBeanAttribute(name = "WriterType", description = "which writer implementation is used")
    public String getWriterType(){
        return this.writerType.toString();
    }

    /**
     * Get the current used network type.
     * @return String with the NetworkType
     */
    @JMXBeanAttribute(name = "NetworkType", description = "which network implementation is used")
    public String getNetworkType(){
        return this.networkType.toString();
    }

    /**
     * Change the network type.
     * For the short names see NetworkType ENum.
     * @see de.b4sh.byter.server.network.NetworkType
     * @param param1 short name for the NetworkType
     * @return String with all possible short names (on failure) | true as text
     */
    @JMXBeanOperation(name = "changeNetworkType", description = "change the current set network type")
    public String changeNetworkType(
            @JMXBeanParameter(name = "networkType", description = "new network type") final String param1
    ){
        //workaround that the jmxbeanparameter is not acutally a string
        final String newNetworkType = String.valueOf(param1).toLowerCase();
        //check for every registered network type
        for(NetworkType nt: NetworkType.values()){
            if(newNetworkType.equals(nt.getKey())){
                if(this.networkManager.getNetworkType() != nt){
                    this.networkType = nt;
                    this.networkManager.setNetworkType(nt);
                    return "changed NetworkType to " + this.networkType.toString();
                }else{
                    return "the choosen network type is already set!";
                }
            }
        }
        return NetworkType.getOptionList();
    }

    /**
     * Change the writer type.
     * For the short names see WriterType Enum.
     * @see de.b4sh.byter.utils.writer.WriterType
     * @param param1 short name for the WriterType
     * @return String with all possible short names (on failure) | true as text
     */
    @JMXBeanOperation(name = "changeWriterType", description = "change the current set writer type")
    public String changeWriterType(
            @JMXBeanParameter(name = "writerType", description = "new writer type") final String param1
    ){
        //workaround that the jmxbeanparameter is not acutally a string
        final String newWriterType = String.valueOf(param1).toLowerCase();
        //iterate through every registered type
        for(WriterType wt: WriterType.values()){
            if(wt.getKey().contains(newWriterType)) {
                if(this.networkManager.getWriterType() != wt){
                    this.writerType = wt;
                    this.networkManager.setWriterType(wt);
                    return "changed WriterType to " + this.writerType.toString();
                }else{
                    return "the choosen writer type is already set!";
                }
            }
        }
        //error out that this wasn't a valid key
        return WriterType.getOptionList();
    }

    /**
     * Set a new file name.
     * @param param1 new file name
     */
    @JMXBeanOperation(name = "changeFileName", description = "change the current set file name to write to")
    public void changeFileName(
        @JMXBeanParameter(name = "fileName", description = "the new file name") final String param1
    ){
        final String newFileName = String.valueOf(param1);
        this.networkManager.setFileName(newFileName);
    }

    /**
     * Return current set file name to network manager.
     * @return String
     */
    @JMXBeanAttribute(name = "FileName", description = "current active file name")
    public String getFileName(){
        final String curr = this.networkManager.getFileName();
        if(curr == null)
            return "none";
        else
            return curr;
    }

    /**
     * get the current write buffer size.
     * @return int with writeBufferSize
     */
    @JMXBeanAttribute(name = "WriterBufferSize", description = "currently active writer buffer")
    public int getWriterBufferSize(){
        return this.writerBufferSize;
    }

    /**
     * get the current network buffer size.
     * @return int with networkBufferSize
     */
    @JMXBeanAttribute(name = "NetworkBufferSize", description = "currently active network buffer")
    public int getNetworkBufferSize(){
        return this.networkBufferSize;
    }

    /**
     * get the current set file path to write data to.
     * @return String with the file path
     */
    @JMXBeanAttribute(name = "FilePath", description = "filepath to write to")
    public String getFilePath(){
        return this.networkManager.getFilePath();
    }

    /**
     * change the file path.
     * @param param1 new path to storage
     * @return String with error message | Success-message
     */
    @JMXBeanOperation(name = "changeFilePath",description = "change the current set file path")
    public String changeFilePath(
        @JMXBeanParameter(name = "newPath", description = "new full path")final String param1
    ){
        final StringBuilder ret = new StringBuilder();
        //fix JMXBeanParameter String
        final String newPath = String.valueOf(param1);
        //catch empty path and rewrite it to test-space
        if(newPath.equals("")){
            final String overrideDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "store";
            if(FileManager.isPathWritalbe(overrideDirectory)){
                this.networkManager.setFilePath(overrideDirectory);
                ret.append("Changed the file path to: ").append(this.networkManager.getFilePath());
            }else{
                FileManager.createFolder(overrideDirectory);
                this.networkManager.setFilePath(overrideDirectory);
                ret.append("Created and changed the file path to: ").append(this.networkManager.getFilePath());
            }

        }
        //check if path is relative
        if(FileManager.isPathRelative(newPath)){
            final String extractedPath = FileManager.operationSystemBasedPathCorrection(FileManager.transformRelativeToAbsolutPath(newPath));
            if(FileManager.isPathWritalbe(extractedPath)){
                this.networkManager.setFilePath(extractedPath);
                ret.append("Changed the file path to: ").append(this.networkManager.getFilePath());
                log.log(Level.INFO, ret.toString());
            }
        }else{
            //check if path exists , if not return "not existing path" or to use changeFilePathAndCreate
            if(FileManager.isPathWritalbe(newPath)){
                this.networkManager.setFilePath(newPath);
                ret.append("Changed the file path to: ").append(this.networkManager.getFilePath());
            }else{
                ret.append("ERROR: PATH NOT WRITEABLE OR NOT EXISTING! \n \n");
                ret.append("Seems that the new file path is not writable or is not existing.\n");
                ret.append("If you want to use this path you could use changeFilePathAndCreate instead.");
            }
        }
        return ret.toString();
    }

    /**
     * change the file path and create not existing folders.
     * @param param1 new path to storage
     * @return String with error message | Success-message
     */
    @JMXBeanOperation(name ="changeFilePathAndCreate", description = "change the current set file path and create folders if neccessary")
    public String changeFilePathAndCreate(
        @JMXBeanParameter(name = "newPath", description = "new full path")final String param1
    ){
        final StringBuilder ret = new StringBuilder();
        //fix JMXBeanParameter
        final String newPath = String.valueOf(param1);
        //create dirs for the new path
        if(FileManager.createFolder(newPath)){
            //check if created path is writeable
            if(FileManager.isPathWritalbe(newPath)){
                this.networkManager.setFilePath(newPath);
                ret.append("Changed the file path to: ").append(this.networkManager.getFilePath());
            }else{
                ret.append("ERROR: PATH NOT WRITABLE! \n \n");
                ret.append("Seems that the application cannot write at given path.");
            }
        } else {
            ret.append("ERROR: PATH NOT CREATED! \n \n");
            ret.append("Seems that the application cannot create the given path.");
        }
        return ret.toString();
    }

    /**
     * Get the currently active ServerSocket port.
     * @return -1 (if nothing is running) | active port number as int
     */
    @JMXBeanAttribute(name = "ServerSocket", description = "port of the started server socket")
    public int getSocketSocket(){
        return this.networkManager.getServerSocketPort();
    }

    /**
     * Get the current state of the server socket.
     * @return false | true
     */
    @JMXBeanAttribute(name = "ServerSocketAccepting", description = "is the server socket ready for accepting clients")
    public boolean getSocketAcceptionState(){
        return this.networkManager.getSocketAcceptingClient();
    }

    /**
     * Stops the currently running Handler Service.
     */
    @JMXBeanOperation(name = "ShutdownHandler", description = "stops or shutdown the currently running handler service")
    public void shutdownRunningHandler(){
        this.networkManager.stopStoreRunner();
    }
}
