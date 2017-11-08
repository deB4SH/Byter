package de.b4sh.byter.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import com.udojava.jmx.wrapper.JMXBeanParameter;
import de.b4sh.byter.utils.data.DateGenerator;
import de.b4sh.byter.utils.jmx.JmxEntity;
import de.b4sh.byter.utils.reader.ReaderInterface;
import de.b4sh.byter.utils.reader.ReaderRandomAccessFile;
import de.b4sh.byter.utils.reader.ReaderType;

/**
 * Client Disc Test Controller Class.
 */
@JMXBean(description = "Byter.Client.Reader")
public final class ClientJmxReader extends JmxEntity {

    private static final Logger log = Logger.getLogger(ClientJmxReader.class.getName());
    private static final String evalDirectory = System.getProperty("user.dir") + File.separator + "evaluation" + File.separator + DateGenerator.generateTodayString() + "_reader";
    //vars
    private ExecutorService servicePool;
    private List<ReaderInterface> activeReader;
    private String path;
    private String fileName;
    private ReaderType readerType;
    private int chunkSize;
    private boolean takeMeasurements;
    private int measurementVolume;


    /**
     * public constructor with base initialisation.
     * @param packageName packageDirectory this jmx object should be register'd under
     * @param type under which type should this object be known
     */
    ClientJmxReader(final String packageName, final String type) {
        super(packageName, type);
        this.path = System.getProperty("user.dir");
        this.fileName = "none";
        this.chunkSize = 8192;
        this.takeMeasurements = true;
        this.measurementVolume = -1;
        this.readerType = ReaderType.none;
        this.servicePool = Executors.newCachedThreadPool();
        this.activeReader = new ArrayList<>();
    }

    /**
     * Get the current set path of the next test run.
     * @return String with the full path.
     */
    @JMXBeanAttribute(name = "Path", description = "path where the file is located")
    public String getPath() {
        return path;
    }

    /**
     * Set a new path where the data is located at.
     * @param path path
     */
    @JMXBeanOperation(name = "setPath", description = "set a new path")
    public void setPath(
        @JMXBeanParameter(name = "newPath", description = "the new path") final String path
    ) {
        this.path = String.valueOf(path); //set new path and translate into real string object
    }

    /**
     * Get the current set fileName to read data from.
     * @return String with the current file name.
     */
    @JMXBeanAttribute(name = "FileName", description = "filename to read")
    public String getFileName() {
        return fileName;
    }

    /**
     * Set a new fileName to read data from
     * @param fileName String with the new file name.
     */
    @JMXBeanOperation(name = "setFileName", description = "set a new fileName")
    public void setFileName(
        @JMXBeanParameter(name = "newFileName", description = "the new fileName") final String fileName
    ) {
        this.fileName = String.valueOf(fileName);
    }

    /**
     * Get the current set chunk size of the reader implementation.
     * @return Integer with the chunk size.
     */
    @JMXBeanAttribute(name = "ChunkSize", description = "current active chunk size")
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Set a new chunk size to use for a reader.
     * @param chunkSize chunk size for reader impl.
     */
    @JMXBeanOperation(name = "setChunkSize", description = "set a new chunk size")
    public void setChunkSize(
        @JMXBeanParameter(name = "newChunkSize", description = "the new chunk size") final int chunkSize
    ) {
        this.chunkSize = chunkSize;
    }

    /**
     * Get the current set flag if measurements should be taken.
     * @return String-boolean with true or false
     */
    @JMXBeanAttribute(name = "TakeMeasurements", description = "current flag of taking measurements")
    public String getTakeMeasurements() {
        return String.valueOf(takeMeasurements);
    }

    /**
     * Get the current flag if measurements should be taken.
     * @return boolean with true or false.
     */
    public boolean isTakeMeasurements() {
        return takeMeasurements;
    }

    /**
     * Set a new flag for takeMeasurements.
     * @param param String with true or false as content.
     */
    @JMXBeanOperation(name = "setTakeMeasurements", description = "set a new flag for takeMeasurements")
    public void setTakeMeasurements(
        @JMXBeanParameter(name = "newTakeMeasurements", description = "the new flag for takeMeasurement") final String param
    ) {
        final String flag = String.valueOf(param);
        if("true".equals(flag))
            this.takeMeasurements = true;
        else if("false".equals(flag))
            this.takeMeasurements = false;
        else
            log.log(Level.INFO,"ClientJmxReader.setTakeMeasurements - wrong flag passed: " + flag);

    }

    /**
     * Get the current measurement volume.
     * @return integer with the amount of data points that should be taken.
     */
    @JMXBeanAttribute(name = "MeasurementVolume", description = "current value of measurementVolume")
    public int getMeasurementVolume() {
        return measurementVolume;
    }

    /**
     * Set a new limit for measurement collection.
     * @param measurementVolume integer with new amount.
     */
    @JMXBeanOperation(name = "setMeasurementVolume", description = "set a new measurement Volume")
    public void setMeasurementVolume(
        @JMXBeanParameter(name = "measurementVolume", description = "new measurement volume") final int measurementVolume
    ) {
        this.measurementVolume = measurementVolume;
    }

    /**
     * set a new reader type.
     * Is used to decide which Reader should be used after invoking the start!
     * @param param String with key of Reader.
     * @see de.b4sh.byter.utils.reader.ReaderType
     */
    @JMXBeanOperation(name = "setNewReaderType", description = "set a new reader type")
    public void setNewReaderType(
        @JMXBeanParameter(name = "readerType", description = "new reader type") final String param
    ){
        final String type = String.valueOf(param);
        //check if the type is registered
        if(ReaderType.isTypeRegistered(type)){
            //set new type by key
            this.readerType = ReaderType.getTypeByKey(type);
        }
    }

    /**
     * get the current set reader type.
     * @return String with the reader type key.
     */
    @JMXBeanAttribute(name = "ReaderType", description = "current set reader type")
    public String getReaderType() {
        return readerType.getType();
    }

    @JMXBeanAttribute(name = "ReaderPoolSize", description = "current reader pool size")
    public int getReaderPoolSize(){
        if(this.activeReader != null)
            return this.activeReader.size();
        else
            return 0;
    }

    /**
     * Shuts down and resets the service pool + list for every "active" writer currently.
     * @return String message with success or failure
     */
    @JMXBeanOperation(name = "stopServicePool", description = "force a stop of the service pool")
    public String stopServicePool(){
        if(!this.servicePool.isTerminated()){
            int oldActiveReaderSize = this.activeReader.size();
            for(final ReaderInterface ri: this.activeReader){
                ri.finish();
            }
            this.servicePool.shutdown();
            //wait a bit for the service to finish
            threadSleep(1000); //sleep one second to give pool time to react
            //force shutdown
            this.servicePool.shutdownNow();
            //reset environment
            this.resetEnvironment();
            return "01 - successfully stopped the current reader pool | size was " + oldActiveReaderSize;
        }else{
            return "00 - the pool is terminated already";
        }
    }

    /**
     * Start a new reader.
     */
    @JMXBeanOperation(name = "startReader", description = "start a single reader on this instance")
    public void startSingleReader(){
        if(this.activeReader.isEmpty()){
            this.startReader();
        }else{
            log.log(Level.WARNING,"There are active Readers in the list. Didn't you stop the pool first?");
        }
    }

    /**
     * Start multiple reader at once.
     * @param count how many to start
     */
    @JMXBeanOperation(name = "startMultiReader", description = "start a multiple reader on this instance")
    public void startMultiReader(
        @JMXBeanParameter(name = "count", description = "how many reader should be started")final int count
    ){
        if(count <= 0){ //null check for given count
            log.log(Level.WARNING, "passed count is zero or below! - not starting any service!");
            return;
        }
        if(this.activeReader.isEmpty()){
            for(int i = 0; i < count; i++)
                this.startReader();
        }else{
            log.log(Level.WARNING,"There are active Readers in the list. Didn't you stop the pool first?");
        }
    }

    private void startReader(){
        if(this.activeReader == null)
            log.log(Level.WARNING,"something is wrong with the activeReader list. hickup?");
        try{
            switch (this.readerType){
                case none:{
                    break;
                }
                case rafr:{
                    final File file = new File(this.path,this.fileName);
                    if(!file.exists()){
                        log.log(Level.WARNING,"File is not existing that should be read. ");
                    }
                    final ReaderRandomAccessFile ri = new ReaderRandomAccessFile(this.chunkSize,file);
                    final Thread runTime = new Thread(ri);
                    this.activeReader.add(ri);
                    this.servicePool.submit(runTime);
                }
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void resetEnvironment(){
        this.activeReader = new ArrayList<>();
        this.servicePool = Executors.newCachedThreadPool();
    }

    private void threadSleep(final long time){
        try{
            Thread.sleep(time);
        } catch (final InterruptedException e) {
            log.log(Level.WARNING, "could not sleep while waiting.");
        }
    }
}
