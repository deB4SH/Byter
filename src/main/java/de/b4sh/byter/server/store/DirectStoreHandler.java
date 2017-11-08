/*
 * File: BasicSocketHandler
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-03
 * Type: Class
 */
package de.b4sh.byter.server.store;

import java.io.File;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import de.b4sh.byter.server.network.NetworkBuffered;
import de.b4sh.byter.server.network.NetworkDataInput;
import de.b4sh.byter.server.network.NetworkInterface;
import de.b4sh.byter.server.network.NetworkType;
import de.b4sh.byter.server.network.NetworkWorkpile;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.writer.WriterArchival;
import de.b4sh.byter.utils.writer.WriterBuffered;
import de.b4sh.byter.utils.writer.WriterFileChannel;
import de.b4sh.byter.utils.writer.WriterInterface;
import de.b4sh.byter.utils.writer.WriterNull;
import de.b4sh.byter.utils.writer.WriterRandomAccessFile;
import de.b4sh.byter.utils.writer.WriterType;

/**
 * Implementation that directly fills a desired puffer and writes it onto the disc.
 */
@JMXBean(description = "DirectStoreHandler")
public final class DirectStoreHandler extends BaseStore{
    private static final Logger log = Logger.getLogger(DirectStoreHandler.class.getName());
    private static final String evaluationFolder = System.getProperty("user.dir") + File.separator + "evaluation";
    private final Socket clientSocket;
    private final long byteTarget;
    //Timer
    private final boolean takeNetworkMeasurements;
    private final boolean takeWriterMeasurements;
    private final int measurementVolume;
    private PerformanceTimer writerPerformanceTimer;
    private PerformanceTimer networkPerformanceTimer;
    //working
    private WriterType writerType;
    private int writerBufferSize;
    private NetworkType networkType;
    private int networkBufferSize;
    private NetworkInterface network;
    private WriterInterface writer;
    private String filePath;
    private String fileName;
    private boolean automaticFileRemoval;

    /**
     * Constructor for directStore.
     * @param client client socket
     * @param writerBufferSize buffer size for the writer implementation
     * @param networkBufferSize buffer size for the network implementation
     * @param networkType network type
     * @param writerType writer type
     * @param filePath filepath to write to
     * @param fileName fileName of the actual file
     * @param measurementVolume defines the limit of measurement points to take
     * @param automaticFileRemoval should the created file removed after the test?
     * @param byteTarget byte target to reach
     * @param takeNetworkMeasurements should the application take network measurements
     * @param takeWriterMeasurements should the application take writer measurements
     */
    public DirectStoreHandler(final Socket client, final int writerBufferSize,
                              final int networkBufferSize, final NetworkType networkType,
                              final WriterType writerType, final String filePath,
                              final String fileName,
                              final boolean takeNetworkMeasurements, final boolean takeWriterMeasurements,
                              final long byteTarget, final int measurementVolume, final boolean automaticFileRemoval) {
        super(StoreType.DirectStore);
        this.clientSocket = client;
        this.writerBufferSize = writerBufferSize;
        this.networkBufferSize =  networkBufferSize;
        this.writerType = writerType;
        this.networkType = networkType;
        this.filePath = filePath;
        this.fileName = fileName == null ? StringGenerator.nextRandomString(5) : fileName;
        this.takeNetworkMeasurements = takeNetworkMeasurements;
        this.takeWriterMeasurements = takeWriterMeasurements;
        this.byteTarget = byteTarget;
        this.measurementVolume = measurementVolume;
        this.automaticFileRemoval = automaticFileRemoval;
        this.initService();
    }

    private void initService(){
        //init timers - if the measurements should be taken
        if(this.takeWriterMeasurements){
            final int skippedSteps = calculateSkippedMeasurements(this.byteTarget,this.measurementVolume,this.networkBufferSize);
            this.writerPerformanceTimer = new PerformanceTimer("DirectStoreHandler-Writer",this.measurementVolume,skippedSteps);
        }
        if(this.takeNetworkMeasurements){
            final int skippedSteps = calculateSkippedMeasurements(this.byteTarget,this.measurementVolume,this.writerBufferSize);
            this.networkPerformanceTimer = new PerformanceTimer("DirectStoreHandler-Network",this.measurementVolume,skippedSteps);
        }
        //init services
        this.initWriter();
        this.initSocket();
    }

    private int calculateSkippedMeasurements(final long byteTarget, final int measurementVolume, final int bufferSize){
        final int maxTracking = (int) (byteTarget / bufferSize);
        return maxTracking / measurementVolume;
    }

    private void initWriter(){
        final File fileToWriteTo = new File(filePath,this.fileName + ".test");
        switch (writerType){
            case None:
                log.log(Level.INFO, "no writer service selected!");
                break;
            case BufferedWriter:
                log.log(Level.INFO, "Buffered Writer selected.");
                if(takeWriterMeasurements)
                    this.writer = new WriterBuffered(this.writerBufferSize,fileToWriteTo,writerPerformanceTimer);
                else
                    this.writer = new WriterBuffered(this.writerBufferSize,fileToWriteTo);
                break;
            case RAFWriter:
                log.log(Level.INFO, "RandomAccessFile Writer selected.");
                if(takeWriterMeasurements)
                    this.writer = new WriterRandomAccessFile(this.writerBufferSize,fileToWriteTo,writerPerformanceTimer);
                else
                    this.writer = new WriterRandomAccessFile(this.writerBufferSize,fileToWriteTo);
                break;
            case NullWriter:
                log.log(Level.INFO, "Null Writer selected.");
                if(takeWriterMeasurements)
                    this.writer = new WriterNull(this.writerBufferSize,fileToWriteTo,writerPerformanceTimer);
                else
                    this.writer = new WriterNull(this.writerBufferSize,fileToWriteTo);
                break;
            case FileChannelWriter:
                log.log(Level.INFO, "FileChannel Writer selected.");
                if(takeWriterMeasurements)
                    this.writer = new WriterFileChannel(this.writerBufferSize,fileToWriteTo,writerPerformanceTimer);
                else
                    this.writer = new WriterFileChannel(this.writerBufferSize,fileToWriteTo);
                break;
            case Archival:
                log.log(Level.INFO, "Archival Writer selected.");
                final File indexFile = new File(filePath,this.fileName + ".index");
                if(takeWriterMeasurements)
                    this.writer = new WriterArchival(fileToWriteTo,indexFile,this.writerBufferSize,writerPerformanceTimer);
                else
                    this.writer = new WriterArchival(fileToWriteTo,indexFile,this.writerBufferSize);
                break;
            default:
                break;
        }
        //set automatic removal flag
        //mainly useful for test to set it to false (so the data is avail. after test and could be asserted)
        if(!automaticFileRemoval)
            this.writer.setAutomaticFileRemoval(false);
    }

    private void initSocket(){
        switch (networkType){
            case None:
                log.log(Level.INFO,"no service type selected! shutdown()");
                shutdown();
                break;
            case BufferedNetwork:
                log.log(Level.INFO, "Buffered Network selected!");
                if(this.takeNetworkMeasurements)
                    this.network = new NetworkBuffered(this.networkBufferSize, this.writer, this.clientSocket,this.networkPerformanceTimer);
                else
                    this.network = new NetworkBuffered(this.networkBufferSize, this.writer, this.clientSocket);
                break;
            case DataInput:
                log.log(Level.INFO, "DataInput Network selected");
                if(this.takeNetworkMeasurements)
                    this.network = new NetworkDataInput(this.networkBufferSize,this.writer,this.clientSocket, this.networkPerformanceTimer);
                else
                    this.network = new NetworkDataInput(this.networkBufferSize,this.writer,this.clientSocket);
                break;
            case BufferedWorkpile:
                log.log(Level.INFO, "BufferedWorkpile Network selected");
                if(this.takeNetworkMeasurements)
                    this.network = new NetworkWorkpile(this.networkBufferSize,this.writer,this.clientSocket,this.networkPerformanceTimer);
                else
                    this.network = new NetworkWorkpile(this.networkBufferSize,this.writer,this.clientSocket);
                break;
            default:
                log.log(Level.WARNING, "DirctStoreHandler: Default Socket Block. How did you end up here?");
                shutdown();
                break;
        }
    }

    @Override
    public void run() {
        log.log(Level.INFO,"Trying to start DirectStoreHandler.\n"
                + "WriterType: " + this.writerType.name() + "\n"
                + "NetworkType: " + this.networkType.name() + "\n"
                + "BufferSize: " + this.writerBufferSize + " WriterBufferSize |  " + this.networkBufferSize + " NetworkBufferSize");
        if(this.network != null)
            this.network.run();
    }

    @Override
    void boot() {
        //nop
    }

    @Override
    void shutdown() {
        //nop
    }

    @Override
    public boolean isImplementationDone() {
        return this.network.isDone();
    }

    /**
     * Get the current running StoreType as String via JMX.
     * @return String with current running StoreType
     */
    @JMXBeanAttribute(name = "StoreType", description = "storetype of current handler")
    public String getCurrentStoreType(){
        return this.getStoreType().toString();
    }
}
