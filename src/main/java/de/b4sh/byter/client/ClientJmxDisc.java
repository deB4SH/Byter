package de.b4sh.byter.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import com.udojava.jmx.wrapper.JMXBeanParameter;
import de.b4sh.byter.utils.data.ChunkGenerator;
import de.b4sh.byter.utils.data.DateGenerator;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.exception.ClientDiscError;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.jmx.JmxEntity;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.writer.WriterArchival;
import de.b4sh.byter.utils.writer.WriterBuffered;
import de.b4sh.byter.utils.writer.WriterFileChannel;
import de.b4sh.byter.utils.writer.WriterInterface;
import de.b4sh.byter.utils.writer.WriterNull;
import de.b4sh.byter.utils.writer.WriterRandomAccessFile;
import de.b4sh.byter.utils.writer.WriterType;
import de.b4sh.byter.utils.writer.WriterWorkpile;

/**
 * Client Disc Test Controller Class.
 */
@JMXBean(description = "Byter.Client.Disc")
public final class ClientJmxDisc extends JmxEntity {

    private static final Logger log = Logger.getLogger(ClientJmxDisc.class.getName());
    private static final String evalDirectory = System.getProperty("user.dir") + File.separator + "evaluation" + File.separator + DateGenerator.generateTodayString() + "_direct";
    //test variables
    private WriterType writerType;
    private String outputPath;
    private int chunkSize;
    private long byteTarget;
    private int writerBufferSize;
    private int measurementVolume;
    private String fileName;
    private boolean automaticFileRemoval;
    //new approach
    private ExecutorService servicePool;
    private List<WriterInterface> activeWriter;
    private boolean activeWriterDone;

    /**
     * public constructor with base initialisation.
     * @param packageName packageDirectory this jmx object should be register'd under
     * @param type under which type should this object be known
     */
    ClientJmxDisc(final String packageName, final String type) {
        super(packageName, type);
        this.writerType = WriterType.None;
        this.writerBufferSize = 8192;
        this.outputPath = System.getProperty("user.dir");
        this.chunkSize = 8192;
        this.byteTarget = 8192 * 10;
        this.measurementVolume = 2500;
        this.fileName = "none";
        this.servicePool = Executors.newCachedThreadPool();
        this.activeWriter = new ArrayList<>();
        this.automaticFileRemoval = true;
    }

    /**
     * Starts a single Writer instance in a threaded way.
     * Uses the servicePool for action.
     */
    @JMXBeanOperation(name = "startWriter", description = "starts a single writer under given configuration")
    public void startSingleWriter(){
        if(activeWriter.isEmpty()){
            final String name = this.getFileNameOrRandom();
            this.startWriterThreaded(name);
        }
    }

    /**
     * Starts multiple writer instances in a threaded way.
     * Uses the servicePool for actions.
     * @param count how many should be started
     */
    @JMXBeanOperation(name = "startMultiWriter", description = "start multiple writer under given configuration")
    public void startMultiWriter(
        @JMXBeanParameter(name = "count", description = "count of how many writer should be started")final int count
    ){
        if(count <= 0){
            log.log(Level.WARNING, "given count is zero or below. stopping here");
            return;
        }
        if(activeWriter.isEmpty()){
            final String name = this.getFileNameOrRandom();
            for(int i = 0; i < count; i++)
                this.startWriterThreaded(name+"-i");
        }
    }

    /**
     * Shuts down and resets the service pool + list for every "active" writer currently.
     * @return String message with success or failure
     */
    @JMXBeanOperation(name = "stopServicePool", description = "shutdown the service pool and reset the environment")
    public String stopServicePool(){
        if(!this.servicePool.isTerminated()){
            final int oldActiveWriterSize = this.activeWriter.size();
            for(final WriterInterface wi: this.activeWriter){
                if(!wi.isFinished())
                    wi.finish();
                    //try to finish again - or try at least
                    //currently all writers finish themself after reaching their target -> inside run-method
            }
            this.servicePool.shutdown();//issue a gentle shutdown
            threadSleep(1000); //give threads a second time to shutdown correctly after the issue
            this.servicePool.shutdownNow();
            this.resetEnvironment();
            return "01 - successfully stopped the current writer pool | size was " + oldActiveWriterSize;
        }else{
            return "00 - the pool is terminated already";
        }
    }

    /**
     * Random Write Test with WriterImplementations.
     */
    @JMXBeanOperation(name = "DiscWriteTest", description = "Perform a write test with random bytes to disc. - legacy method!")
    public void discPerformanceTest(){
        startWriter();
    }

    private void workflow(final WriterInterface writer, final byte[] chunk, final byte[] edge, final int iterations){
        //run through the most part of the data that should be written to disc
        for(int i = 0; i < iterations; i++){
            writer.handleData(chunk);
        }
        writer.handleData(edge,edge.length); //fix edge case (missing bytes)
        writer.finish(); //finish writer - to close stream and open file locks
    }

    /**
     * Invoke function to rebuild the evaluation folder.
     * Required for setting a new evaluation folder for each test.
     */
    @JMXBeanOperation(name = "RebuildEvaluationFolder", description = "rebuild the evaluation folder")
    public void rebuildEvaluationFolder(){
        FileManager.rebuildEvaluationFolder(this.fileName);
    }

    /**
     * Checks if the currently running writer are done.
     * @return true(done) | false(not done or not started)
     */
    @JMXBeanAttribute(name = "activeWritersDone", description = "check if the activated writers are done")
    public boolean getActiveWritersDone(){
        if(this.activeWriter != null){//check if the list is avail
            if(this.activeWriter.isEmpty()){//check if the list is empty - if empty  return false
                return false;
            }else{//with entries
                for(WriterInterface wi: this.activeWriter){
                    if(!wi.isFinished())//if any writer in this list is not finished return false
                        return false;
                }
                return true; //if every reader in the list is done - return true
            }
        }else{
            return false;
        }
    }

    /**
     * Set a flag for automatic file removal after _test_.
     * Automatic removal should be deactivated when running junit tests.
     * Else it's unable to check if the file is written and exists.
     * @param param flag to set (true, false) as String (transmission over jmx)
     */
    @JMXBeanOperation(name = "setAutomaticFileRemoval", description = "decides if the files should be removed automatically")
    public void setAutomaticFileRemoval(
            @JMXBeanParameter(name = "flag", description = "flag to set")final String param
    ){
        final String flag = String.valueOf(param);
        if("true".equals(flag))
            this.automaticFileRemoval = true;
        else if("false".equals(flag))
            this.automaticFileRemoval = false;
    }

    /**
     * get the current set filename.
     * @return String with filename
     */
    @JMXBeanAttribute(name = "FileName", description = "file name to write data to")
    public String getFileName(){
        return this.fileName;
    }

    /**
     * get the current set file. if non set it returns a random 5 long name.
     * @return String with filename or generated name
     */
    private String getFileNameOrRandom(){
        if("none".equals(this.fileName)){
            return StringGenerator.nextRandomString(5);
        }
        return this.fileName;
    }

    /**
     * get the current set writer implementation.
     * @return String of writer implementation key.
     */
    @JMXBeanAttribute(name = "WriterImplementation", description = "implementation that is used to write data")
    public String getWriterImplementation(){
        return this.writerType.getKey();
    }

    /**
     * get the current set output path.
     * @return String with output path
     */
    @JMXBeanAttribute(name = "OutputPath", description = "path where data is written to")
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * get the current set chunk size.
     * @return size of the chunk that is generated.
     */
    @JMXBeanAttribute(name = "ChunkSize", description = "size of the chunk that is pregenerated")
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * get the set byte target.
     * defines how many bytes should be written to disc.
     * @return target
     */
    @JMXBeanAttribute(name = "ByteTarget", description = "amount of data that is written to disc")
    public long getByteTarget() {
        return byteTarget;
    }

    /**
     * get the current set writer buffer.
     * @return buffer size
     */
    @JMXBeanAttribute(name = "WriterBufferSize", description = "size of the writer buffer")
    public int getWriterBufferSize() {
        return writerBufferSize;
    }

    /**
     * get the current set target of how many measurements should be taken.
     * @return max size of measurement list
     */
    @JMXBeanAttribute(name = "MeasurementVolume", description = "count of measurements that are created in the process")
    public int getMeasurementVolume() {
        return measurementVolume;
    }

    /**
     * set a new writer implementation.
     * @see de.b4sh.byter.utils.writer.WriterType
     * @param param1 new implementation as string. requires a key from WriterType Enum.
     * @return String with errorMessage or success msg.
     */
    @JMXBeanOperation(name = "SetWriterImplementation", description = "set a new WriterImplementation")
    public String setWriterImplementation(
        @JMXBeanParameter(name = "newWriterImplementation") final String param1
    ){
        final String newWriterType = String.valueOf(param1).toLowerCase();
        for(WriterType wt: WriterType.values()){
            if(wt.getKey().equals(newWriterType)){
                this.writerType = wt;
                return "Set the new WriterType to " + this.writerType.name();
            }
        }
        //error out
        return WriterType.getOptionList();
    }

    /**
     * Set a new output path.
     * @param param1 path to write data to.
     * @return String with error or success message.
     */
    @JMXBeanOperation(name = "SetOutputPath", description = "set a new output path")
    public String setOutputPath(
        @JMXBeanParameter(name = "newOutputPath") final String param1
    ){
        final StringBuilder ret = new StringBuilder();
        //fix JMXBeanParameter String
        final String newPath = String.valueOf(param1);
        //catch empty path and rewrite it to test-space
        if("".equals(newPath)){
            final String overrideDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "store";
            if(FileManager.isPathWritalbe(overrideDirectory)){
                this.outputPath = overrideDirectory;
                ret.append("Changed the file path to: ").append(this.outputPath);
            }else{
                FileManager.createFolder(overrideDirectory);
                this.outputPath = overrideDirectory;
                ret.append("Created and changed the file path to: ").append(this.outputPath);
            }

        }
        if(FileManager.isPathRelative(newPath)){
            final String preOs = FileManager.transformRelativeToAbsolutPath(newPath);
            final String overridePath = FileManager.operationSystemBasedPathCorrection(preOs);
            if(FileManager.isPathWritalbe(overridePath)){
                this.outputPath = overridePath;
                ret.append("Changed the file path to: ").append(this.outputPath);
                log.log(Level.INFO, ret.toString());
            }else{
                ret.append("ERROR: PATH NOT WRITEABLE OR NOT EXISTING! \n \n");
                ret.append("Seems that the new file path is not writable or is not existing.\n");
                ret.append("If you want to use this path you could use changeFilePathAndCreate instead.");
            }
        }else{
            //check if path exists , if not return "not existing path" or to use changeFilePathAndCreate
            if(FileManager.isPathWritalbe(newPath)){
                this.outputPath = newPath;
                ret.append("Changed the file path to: ").append(this.outputPath);
            }else{
                ret.append("ERROR: PATH NOT WRITEABLE OR NOT EXISTING! \n \n");
                ret.append("Seems that the new file path is not writable or is not existing.\n");
                ret.append("If you want to use this path you could use changeFilePathAndCreate instead.");
            }
        }
        return ret.toString();
    }

    /**
     * Set a new chunk size.
     * @param chunkSize new chunk size.
     */
    @JMXBeanOperation(name = "SetChunkSize", description = "set a new chunk size")
    public void setChunkSize(
        @JMXBeanParameter(name = "NewChunkSize") final int chunkSize
    ){
        this.chunkSize = chunkSize;
    }

    /**
     * set a new byte target.
     * @param byteTarget new byte target.
     */
    @JMXBeanOperation(name = "SetByteTarget", description = "set a new byte target to reach")
    public void setByteTarget(
        @JMXBeanParameter(name = "NewByteTarget") final long byteTarget
    ){
        this.byteTarget = byteTarget;
    }

    /**
     * set a new buffer size for the writer implementation.
     * @param writerBufferSize new buffer size.
     */
    @JMXBeanOperation(name = "SetWriterBufferSize", description = "set a writer buffer size")
    public void setWriterBufferSize(
        @JMXBeanParameter(name = "NewWriterBufferSize") final int writerBufferSize
    ){
        this.writerBufferSize = writerBufferSize;
    }

    /**
     * set a new measurement list max size.
     * @param measurementVolume new max size of the measurements list
     */
    @JMXBeanOperation(name = "SetMeasurementVolume", description = "set a new measurement volume")
    public void setMeasurementVolume(
        @JMXBeanParameter(name = "NewMeasurementVolume") final int measurementVolume
    ){
        this.measurementVolume = measurementVolume;
    }

    /**
     * set a new file name for file creation.
     * @param param1 new file name
     */
    @JMXBeanOperation(name = "SetFileName", description = "set a new file name")
    public void setFileName(
        @JMXBeanParameter(name = "NewFileName") final String param1
    ){
        this.fileName = String.valueOf(param1);
    }

    private void resetEnvironment(){
        this.activeWriter = new ArrayList<>();
        this.servicePool = Executors.newCachedThreadPool();
    }

    private void threadSleep(final long time){
        try{
            Thread.sleep(time);
        } catch (final InterruptedException e) {
            log.log(Level.WARNING, "could not sleep while waiting.");
        }
    }

    /**
     * starts a writer as a thread and puts it into the servicePool for run purposes!
     */
    private void startWriterThreaded(final String name){
        //basic informations
        final File writeFile = new File(this.outputPath, name +".data");
        final byte[] chunk = ChunkGenerator.generateChunk(this.chunkSize); //generate a new randomized chunk of data
        //calculate runs to fulfill
        final int iterations = (int)(byteTarget / chunk.length);
        final int edgeSize = (int)(byteTarget % chunk.length);
        final byte[] edge = ChunkGenerator.generateChunk(edgeSize);
        final WriterInterface wi = initWriter(this.writerType,writeFile,name);
        wi.setRunParameters(chunk,edge,iterations);
        wi.setAutomaticFileRemoval(this.automaticFileRemoval);
        if(wi == null){
            log.log(Level.WARNING, "WriterInterface was null, did you passed a wrong WriterType? Stopping here to start a new Writer");
            return;
        }
        this.activeWriter.add(wi);
        final Thread runnerThread = createThreadFromWriter(wi);
        if(runnerThread == null){
            log.log(Level.WARNING, "runnerThread was null, did you passed a null WirterInterface or some new Interface that is not in the list? "
                    +"Stopping here to start a new Writer");
            return;
        }
        this.servicePool.submit(runnerThread);
        log.log(Level.INFO, "successfully added Writer " + this.activeWriter.size() + ".");
    }

    private Thread createThreadFromWriter(final WriterInterface wi){
        if(wi instanceof WriterArchival){
            return new Thread((WriterArchival)wi);
        }
        else if(wi instanceof WriterBuffered){
            return new Thread((WriterBuffered)wi);
        }
        else if(wi instanceof WriterWorkpile){
            return new Thread((WriterWorkpile)wi);
        }
        else if(wi instanceof WriterNull){
            return new Thread((WriterNull)wi);
        }
        else if(wi instanceof WriterRandomAccessFile){
            return new Thread((WriterRandomAccessFile)wi);
        }
        else if(wi instanceof WriterFileChannel){
            return new Thread((WriterFileChannel)wi);
        }else{
            return null;
        }
    }

    /**
     * creates the writerInterface object by selected type.
     * @param type writerInterface you want
     * @param file file to write to
     * @param name name of the file (needed for archival writer for the most part)
     * @return writer object instanced
     */
    public WriterInterface initWriter(final WriterType type, final File file, final String name){
        final WriterInterface writer;
        final int measurementSteps = PerformanceTimer.calculateSkippedMeasurements(this.byteTarget,this.measurementVolume,this.writerBufferSize);
        switch (type){
            case None:
                writer = createWriterCaseNone();
                break;
            case BufferedWriter:
                writer = createWriterCaseBuffered(measurementSteps,file);
                break;
            case Archival:
                writer = createWriterCaseArchival(measurementSteps,file,name);
                break;
            case RAFWriter:
                writer = createWriterCaseRAF(measurementSteps,file);
                break;
            case NullWriter:
                writer = createWriterCaseNull(measurementSteps,file);
                break;
            case FileChannelWriter:
                writer = createWriterCaseFileChannel(measurementSteps,file);
                break;
            default:
                writer = createWriterCaseNone();
                break;
        }
        return writer;
    }

    private WriterInterface createWriterCaseFileChannel(final int measurementSteps, final File file){
        final PerformanceTimer workingTimer = new PerformanceTimer(WriterFileChannel.class.getName(),measurementVolume,measurementSteps);
        return new WriterFileChannel(this.writerBufferSize, file, workingTimer);
    }

    private WriterInterface createWriterCaseNull(final int measurementSteps, final File file){
        final PerformanceTimer workingTimer = new PerformanceTimer(WriterNull.class.getName(),measurementVolume,measurementSteps);
        return new WriterNull(this.writerBufferSize, file, workingTimer);
    }

    private WriterInterface createWriterCaseRAF(final int measurementSteps, final File file){
        final PerformanceTimer workingTimer = new PerformanceTimer(WriterRandomAccessFile.class.getName(),measurementVolume,measurementSteps);
        return new WriterRandomAccessFile(this.writerBufferSize, file, workingTimer);
    }

    private WriterInterface createWriterCaseArchival(final int measurementSteps, final File file, final String name){
        final PerformanceTimer workingTimer = new PerformanceTimer(WriterArchival.class.getName(),measurementVolume,measurementSteps);
        final File indexFile = new File(this.outputPath, name +".index");
        return new WriterArchival(file,indexFile,this.writerBufferSize, workingTimer);
    }

    private WriterInterface createWriterCaseBuffered(final int measurementSteps, final File file){
        final PerformanceTimer workingTimer = new PerformanceTimer(WriterBuffered.class.getName(),measurementVolume,measurementSteps);
        return new WriterBuffered(this.writerBufferSize,file,workingTimer);
    }

    private WriterInterface createWriterCaseNone(){
        log.log(Level.WARNING, ClientDiscError.NO_WRITER_SELECTED.getReason());
        return null;
    }

    /**
     * legacy method to start a single writer and also run the test inside this function. (DEPRECATED)
     * not in a thread way.
     * prints the evaluated data to logger.
     * THIS METHOD IS JUST THERE FOR THE OLD WORKFLOW - DONT USE THIS.
     */
    @Deprecated
    private void startWriter(){
        //basic informations
        final String name = this.getFileNameOrRandom();
        final File writeFile = new File(this.outputPath, name +".data");
        final byte[] chunk = ChunkGenerator.generateChunk(this.chunkSize); //generate a new randomized chunk of data
        //calculate runs to fulfill
        final int iterations = (int)(byteTarget / chunk.length);
        final int edgeSize = (int)(byteTarget % chunk.length);
        final byte[] edge = ChunkGenerator.generateChunk(edgeSize);
        //function specific
        final StringBuilder sb = new StringBuilder(1000);
        //actual performance timer that contains the true values
        final PerformanceTimer pt;
        if(writerType == WriterType.BufferedWriter){
            final PerformanceTimer workingTimer = new PerformanceTimer(WriterBuffered.class.getName());
            final WriterInterface writer = new WriterBuffered(this.writerBufferSize, writeFile, workingTimer);
            workflow(writer, chunk, edge, iterations);
            pt = writer.getTimer();
        }
        else if(writerType == WriterType.FileChannelWriter){
            final PerformanceTimer workingTimer = new PerformanceTimer(WriterFileChannel.class.getName());
            final WriterInterface writer = new WriterFileChannel(this.writerBufferSize, writeFile, workingTimer);
            workflow(writer, chunk, edge, iterations);
            pt = writer.getTimer();
        }
        else if(writerType == WriterType.NullWriter){
            final PerformanceTimer workingTimer = new PerformanceTimer(WriterNull.class.getName());
            final WriterInterface writer = new WriterNull(this.writerBufferSize, writeFile, workingTimer);
            workflow(writer, chunk, edge, iterations);
            pt = writer.getTimer();
        }
        else if(writerType == WriterType.RAFWriter){
            final PerformanceTimer workingTimer = new PerformanceTimer(WriterRandomAccessFile.class.getName());
            final WriterInterface writer = new WriterRandomAccessFile(this.writerBufferSize, writeFile, workingTimer);
            workflow(writer, chunk, edge, iterations);
            pt = writer.getTimer();
        }
        else if(writerType == WriterType.Archival){
            final PerformanceTimer workingTimer = new PerformanceTimer(WriterArchival.class.getName());
            final File indexFile = new File(this.outputPath, name +".index");
            final WriterInterface writer = new WriterArchival(writeFile,indexFile,this.writerBufferSize, workingTimer);
            workflow(writer, chunk, edge, iterations);
            pt = writer.getTimer();
        }
        else{
            //ERROR workflow
            log.log(Level.WARNING, ClientDiscError.NO_WRITER_SELECTED.getReason());
            return;
        }
        final Map<String,Float> evalMap = pt.evaluate(); //do standard evaluation on taken measurements
        FileManager.createFolder(evalDirectory);
        final File evalFile = new File(evalDirectory, DateGenerator.generateTimeStringForFile()
                + "_" + name + " _"
                + "_" + writerType.getKey() + " _"+".eval.json");
        pt.writeEvaluationToFile(evalFile,evalMap); //write the eval data to a file for later-review
        for(String key: evalMap.keySet()){
            sb.append(key + " : " + evalMap.get(key) + " \n");
        }
        log.log(Level.INFO, "\n" + sb.toString());
    }
}
