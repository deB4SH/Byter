package de.b4sh.byter.server;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.server.network.NetworkType;
import de.b4sh.byter.server.store.BaseStore;
import de.b4sh.byter.server.store.DirectStoreHandler;
import de.b4sh.byter.server.store.StoreType;
import de.b4sh.byter.utils.io.PortScanner;
import de.b4sh.byter.utils.jmx.MBeanHelper;
import de.b4sh.byter.utils.writer.WriterType;

/**
 * NetworkManager opens Sockets for incoming data and
 * switches between Store-Implementations and subclasses in them.
 */
final class NetworkManager {

    private static final Logger log = Logger.getLogger(NetworkManager.class.getName());
    private final ExecutorService threadPool;
    private final MBeanHelper mBeanHelper;
    private StoreRunner storeRunner;
    private Thread storeRunnerThread;
    private boolean takeNetworkMeasurements;
    private boolean takeWriterMeasurements;
    private long byteTarget;
    private int measurementVolume;
    //socket related
    private int serverSocketStartPort;
    private int serverSocketPort;
    private boolean serverSocketStarted;
    private boolean serverSocketKeepAlive;
    //handler related
    private StoreType storeType;
    private NetworkType networkType;
    private WriterType writerType;
    private String filePath;
    private String fileName;
    private boolean automaticFileRemoval;

    /**
     * Networkmanager Constructor.
     * @param mBeanHelper MBeanHelper class with active MBeanServer
     * @param executorPoolSize set the size of the executor pool
     * @param serverSocketStartPort set the port where the port scanner should start
     */
    NetworkManager(final MBeanHelper mBeanHelper, final int executorPoolSize, final int serverSocketStartPort){
        this.threadPool = Executors.newFixedThreadPool(executorPoolSize);
        this.mBeanHelper = mBeanHelper;
        this.serverSocketStartPort = serverSocketStartPort;
        this.serverSocketKeepAlive = false;
        this.serverSocketStarted = false;
        this.storeType = StoreType.None;
        this.serverSocketPort = -1;
        this.fileName = "none";
        this.takeNetworkMeasurements = false;
        this.takeWriterMeasurements = true;
        this.byteTarget = -1;
        this.measurementVolume = -1;
        this.automaticFileRemoval = true;
    }
    /**
     * start a direct storage service.
     * pipes network buffer directly to disc
     * @param networkBufferSize buffer size of the network implementation
     * @param writerBufferSize buffer size of the writer implementation
     */
    void startDirectStore(final int networkBufferSize, final int writerBufferSize){
        if(storeRunner != null){
            log.log(Level.WARNING,"There is a StoreRunner going on!");
            return;
        }
        this.storeRunner = new StoreRunner(StoreType.DirectStore,writerBufferSize,networkBufferSize);
        this.storeType = StoreType.DirectStore;
        this.storeRunnerThread = new Thread(this.storeRunner);
        this.storeRunnerThread.setDaemon(true);
        this.storeRunnerThread.start();
    }

    /**
     * Stop the currently running StoreRunner service.
     */
    void stopStoreRunner(){
        final boolean oldNetworkMeasurementFlag = takeNetworkMeasurements;
        final boolean oldWriterMeasurementFlag = takeWriterMeasurements;
        this.takeWriterMeasurements = false;
        this.takeNetworkMeasurements = false;
        //set keep alive to false
        this.serverSocketKeepAlive = false;
        //unblock server socket
        storeRunner.setUnblockConnection(true);
        try{
            if(this.serverSocketPort != -1){
                final Socket ss = new Socket("localhost", this.serverSocketPort);
                ss.close();
            }else{
                log.log(Level.INFO,"There is no Store Handler running currently! Did you invoke this function by hand?");
            }
        } catch (final UnknownHostException e) {
            log.log(Level.WARNING, "cannot resolve host. UnknownHostException in stopStoreRunner");
        } catch (final ConnectException ce){
            log.log(Level.INFO, "seems that there isn't a service running on port " + this.serverSocketPort + ". ConnectException!");
        } catch (final IOException e) {
            log.log(Level.WARNING, "IO Exception in run Method. Check stacktrace!",e);
        }finally {
            //set storeRunner to null - for new storerunners to start
            storeRunner = null;
            this.storeType = StoreType.None;
            this.serverSocketPort = -1;
            this.serverSocketStarted = false;
            log.log(Level.INFO, "Successfully closed the old StoreHandler. Now open for new.");
        }
        this.takeWriterMeasurements = oldWriterMeasurementFlag;
        this.takeNetworkMeasurements = oldNetworkMeasurementFlag;
    }

    /**
     * Creates a ServerSocket if there isn't any service running.
     * @return opened ServerSocket
     * @throws IOException IO Exception if something weird is happening
     */
    private ServerSocket startServerSocket(final int port) throws IOException {
        if(!this.serverSocketStarted){
            log.log(Level.WARNING, "Trying to start a ServerSocket on port: " + port);
            final ServerSocket socket = new ServerSocket(port);
            socket.setReuseAddress(true);
            this.serverSocketStarted = true;
            this.serverSocketKeepAlive = true;
            return socket;
        }else{
            log.log(Level.WARNING,"There is currently a ServerSocket running on port: " + port);
            return null;
        }
    }

    /**
     * Finds the next open port and sets a fields on NetworkManager.
     * Also returning the found port to the calling procedure.
     * @param range range to scan for an open port
     * @return integer with the open port
     */
    private int findNextOpenPort(final int range){
        //start socket service
        this.serverSocketPort = PortScanner.getNextPort(this.serverSocketStartPort,range);
        return this.serverSocketPort;
    }

    /**
     * Get the current flag of automatic file removal.
     * @return true | false
     */
    public boolean isAutomaticFileRemoval() {
        return automaticFileRemoval;
    }

    /**
     * set a new flag to the automatic file removal.
     * @param automaticFileRemoval new flag
     */
    public void setAutomaticFileRemoval(final boolean automaticFileRemoval) {
        this.automaticFileRemoval = automaticFileRemoval;
    }

    /**
     * Get the count of measurements that should be created on tests.
     * @return current list max size
     */
    int getMeasurementVolume() {
        return measurementVolume;
    }

    /**
     * set a new max size for measurements
     * @param measurementVolume size
     */
    void setMeasurementVolume(final int measurementVolume) {
        this.measurementVolume = measurementVolume;
    }

    /**
     * Get the current set value of takeNetworkMeasurements.
     * @return true | false
     */
    boolean isTakeNetworkMeasurements() {
        return this.takeNetworkMeasurements;
    }

    /**
     * Set a new value for takeNetworkMeasurements.
     * @param takeMeasurements the new param (true|false)
     */
    void setTakeNetworkMeasurements(boolean takeMeasurements) {
        this.takeNetworkMeasurements = takeMeasurements;
    }

    /**
     * Get the current set value of takeWriterMeasurements.
     * @return true | false
     */
    boolean isTakeWriterMeasurements() {
        return takeWriterMeasurements;
    }

    /**
     * Set a new value for takeWriterMeasurements.
     * @param takeWriterMeasurements the new param(true|false)
     */
    void setTakeWriterMeasurements(final boolean takeWriterMeasurements) {
        this.takeWriterMeasurements = takeWriterMeasurements;
    }

    /**
     * Get the current set byte target.
     * @return long with how many bytes should be received.
     */
    long getByteTarget() {
        return byteTarget;
    }

    /**
     * set a new byte target for received bytes.
     * @param byteTarget new target
     */
    void setByteTarget(long byteTarget) {
        this.byteTarget = byteTarget;
    }

    /**
     * Get the currently active store type.
     * @return None(default) | any other implemented store type
     */
    public StoreType getCurrentActiveStoreType(){
        return this.storeType;
    }

    /**
     * Get the current set network type.
     * @return currently set network type
     */
    public NetworkType getNetworkType() {
        return networkType;
    }

    /**
     * Get the current set writer type.
     * @return currently set writer type
     */
    public WriterType getWriterType() {
        return writerType;
    }

    /**
     * Set a new network type.
     * @param networkType new network type
     */
    public void setNetworkType(final NetworkType networkType) {
        this.networkType = networkType;
    }

    /**
     * Set a new writer type.
     * @param writerType new writer type
     */
    public void setWriterType(final WriterType writerType) {
        this.writerType = writerType;
    }

    /**
     * get the set file path.
     * @return String with the filepath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * set a new file path.
     * @param filePath String with new filepath
     */
    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    /**
     * get the current active server socket port.
     * @return 0 (default , no running service) or other actual used port
     */
    public int getServerSocketPort() {
        return serverSocketPort;
    }

    /**
     * get the current status from StoreRunner and the possibility to accept connections.
     * @return true | false (default)
     */
    public boolean getSocketAcceptingClient(){
        if(storeRunner != null)
            return this.storeRunner.getSocketAcceptingClient();
        else
            return false;
    }

    /**
     * get the current set FileName or null if filename is NONE.
     * @return String | null
     */
    public String getFileName() {
        if(!"none".equals(fileName))
            return fileName;
        else
            return null;
    }

    /**
     * set a new file name.
     * @param fileName new filename
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    private File getCurrentSetFile(){
        return new File(filePath,fileName);
    }

    public boolean getCurrentFileExisting(){
        return getCurrentSetFile().exists();
    }

    public long getCurrentFileLength(){
        final File file = getCurrentSetFile();
        if(file.exists())
            return file.length();
        else
            return -1;
    }

    public boolean isTestSzenarioImplementationDone(){
        if(storeRunner.store != null){
            return storeRunner.store.isImplementationDone();
        }else{
            return true;
        }
    }

    /**
     * Runner Class to keep the main thread reactive.
     */
    class StoreRunner implements Runnable{

        private final StoreType storeType;
        private final int writerBufferSize;
        private final int networkBufferSize;
        private boolean socketAcceptingClient;
        private BaseStore store;
        private boolean unblockConnection;

        /**
         * Constructor for StoreRunner.
         * @param storeType storetype to use
         * @param writerBufferSize size of the writer buffer
         * @param networkBufferSize size of the network buffer
         */
        StoreRunner(final StoreType storeType,
                    final int writerBufferSize,
                    final int networkBufferSize){
            this.storeType = storeType;
            this.writerBufferSize = writerBufferSize;
            this.networkBufferSize = networkBufferSize;
            this.socketAcceptingClient = false;
            this.unblockConnection = false;
        }

        @Override
        public void run() {
            final ServerSocket socket;
            try {
                socket = startServerSocket(findNextOpenPort(50));
                if(socket != null){ //nullcheck if there is already a service running
                    //DirectStoreHandler dhs = null;
                    while(serverSocketKeepAlive){ //keep alive until socket should die
                        this.socketAcceptingClient = true;
                        final Socket client = socket.accept();
                        client.setReuseAddress(true);
                        client.setSoTimeout(60*1000); //one minute timeout (60 * 1000 ms)
                        if(!this.unblockConnection && store != null){
                            log.log(Level.WARNING, "Sorry there is currently a test running. I cant open up an another Store in this configuration.");
                            client.close();
                        }
                        if(this.storeType.equals(StoreType.DirectStore)){
                            store = new DirectStoreHandler(client,writerBufferSize,networkBufferSize,networkType,
                                    writerType,filePath,getFileName(),takeNetworkMeasurements,
                                    takeWriterMeasurements,byteTarget,measurementVolume,automaticFileRemoval);
                            //mBeanHelper.registerElement(dhs, dhs.getObjectName());
                            //the jmx implementation of the stores is for the current use-case unused.
                            threadPool.execute(store);
                        } else{
                            log.log(Level.WARNING,"No StoreType set!");
                            client.close();
                        }
                    }
                    socket.close();
                    this.socketAcceptingClient = false;
                    //if(dhs != null) //check if the dhs is null , for some unknown reasons
                    //    mBeanHelper.unregisterElement(dhs.getObjectName());
                }else{
                    log.log(Level.WARNING, "Socket is null. Is there any other service running simultaneously?");
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "IO Exception occured inside the DirectStore. Check Stacktrace",e);
            }
        }

        /**
         * Get the current state of the Socket.
         * @return false | true
         */
        public boolean getSocketAcceptingClient(){
            return this.socketAcceptingClient;
        }

        /**
         * Get the current set store.
         * @return StoreInstance
         */
        public BaseStore getStore() {
            return store;
        }

        /**
         * Set the current store.
         * @param store Store
         */
        public void setStore(final BaseStore store) {
            this.store = store;
        }

        /**
         * Set a flag for the software to unblock the current socket.
         * @param unblockConnection flag for unblockConnection.
         */
        public void setUnblockConnection(final boolean unblockConnection) {
            this.unblockConnection = unblockConnection;
        }
    }
}
