package de.b4sh.byter.utils.writer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.measurements.Measurement;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.measurements.PerformanceTimerHelper;
import de.b4sh.byter.utils.reader.ReaderInterface;

/**
 * Archive alike writer for testing purposes.
 * This simulates a archive write process.
 * For both actions a buffered writer is used.
 * This is a special type of writer. So its not referenced inside the WriterType.
 * @see de.b4sh.byter.utils.writer.WriterType
 */
public final class WriterArchival implements WriterInterface,Runnable {

    private static Logger log = Logger.getLogger(WriterArchival.class.getName());
    private final File dataFile;
    private final String fileName;
    private final File indexFile;
    private final WriterInterface dataWriter;
    private final WriterInterface indexWriter;
    private final PerformanceTimer pt;
    private final boolean takeMeasurements;
    private ReaderInterface reader;
    //run parameter
    private byte[] chunk;
    private byte[] edge;
    private int iteration;
    private boolean isFinished;
    private boolean automaticFileRemove;

    /**
     * Construct an archival alike writer.
     * The written data is random generated.
     * @param dataFile the file to write data to (this needs to be a full file link)
     * @param indexFile the file to write index data to (this needs to be a full file link)
     * @param dataWriterBuffer the size of the dataWriterBuffer
     */
    public WriterArchival(final String dataFile, final String indexFile, final int dataWriterBuffer) {
        this.dataFile = new File(dataFile);
        this.indexFile = new File(indexFile);
        this.fileName = this.dataFile.getName();
        this.dataWriter = new WriterBuffered(dataWriterBuffer,this.dataFile);
        this.indexWriter = new WriterRandomAccessFile(8192, this.indexFile);
        this.pt = null;
        this.takeMeasurements = false;
        this.isFinished = false;
        this.automaticFileRemove = true;
    }

    /**
     * Construct an archival alike writer.
     * The written data is random generated.
     * @param dataFile the file to write data to (this needs to be a full file link)
     * @param indexFile the file to write index data to (this needs to be a full file link)
     * @param dataWriterBuffer the size of the dataWriterBuffer
     */
    public WriterArchival(final File dataFile, final File indexFile, final int dataWriterBuffer) {
        this.dataFile = dataFile;
        this.indexFile = indexFile;
        this.fileName = this.dataFile.getName();
        this.dataWriter = new WriterBuffered(dataWriterBuffer,dataFile);
        this.indexWriter = new WriterBuffered(8192, indexFile);
        this.pt = null;
        this.takeMeasurements = false;
        this.isFinished = false;
        this.automaticFileRemove = true;
    }

    /**
     * Construct an archival alike writer.
     * The written data is random generated.
     * @param dataFile the file to write data to (this needs to be a full file link)
     * @param indexFile the file to write index data to (this needs to be a full file link)
     * @param dataWriterBuffer the size of the dataWriterBuffer
     * @param timer timer that collects the measurements
     */
    public WriterArchival(final String dataFile, final String indexFile, final int dataWriterBuffer, final PerformanceTimer timer) {
        this.dataFile = new File(dataFile);
        this.indexFile = new File(indexFile);
        this.fileName = this.dataFile.getName();
        this.dataWriter = new WriterBuffered(dataWriterBuffer,this.dataFile);
        this.indexWriter = new WriterRandomAccessFile(8192, this.indexFile);
        this.pt = timer;
        this.takeMeasurements = true;
        this.isFinished = false;
        this.automaticFileRemove = true;
    }

    /**
     * Construct an archival alike writer.
     * The written data is random generated.
     * @param dataFile the file to write data to (this needs to be a full file link)
     * @param indexFile the file to write index data to (this needs to be a full file link)
     * @param dataWriterBuffer the size of the dataWriterBuffer
     * @param timer timer that collects the measurements
     */
    public WriterArchival(final File dataFile, final File indexFile, final int dataWriterBuffer, final PerformanceTimer timer) {
        this.dataFile = dataFile;
        this.indexFile = indexFile;
        this.fileName = this.dataFile.getName();
        this.dataWriter = new WriterBuffered(dataWriterBuffer,dataFile);
        this.indexWriter = new WriterBuffered(8192, indexFile);
        this.pt = timer;
        this.takeMeasurements = true;
        this.isFinished = false;
        this.automaticFileRemove = true;
    }

    @Override
    public void handleData(final byte[] bytes) {
        this.handleData(bytes,bytes.length);
    }

    @Override
    public void handleData(final byte[] bytes, final int offset) {
        if(this.takeMeasurements)
            this.handleDataWithMeasurement(bytes,offset);
        else
            this.handleDataWithoutMeasurement(bytes,offset);
    }

    private void handleDataWithMeasurement(final byte[] bytes, final int offset){
        final long tStart = System.nanoTime();
        //get current file offset
        final long currentFileOffset = this.dataFile.length();
        //start timestamp
        final long start = System.nanoTime();
        //write data to file
        this.dataWriter.handleData(bytes,offset);
        //stop timestamp
        final long stop = System.nanoTime();
        //write metadata to index file (offsetWhereDataIsInFile, start, stop)
        this.indexWriter.handleData(toByteArray(start));
        this.indexWriter.handleData(toByteArray(stop));
        this.indexWriter.handleData(toByteArray(currentFileOffset));
        final long tStop = System.nanoTime();
        pt.addNewMeasurement(new Measurement(WriterArchival.class.getName(),offset,tStart,tStop));
    }

    private void handleDataWithoutMeasurement(final byte[] bytes, final int offset){
        //get current file offset
        final long currentFileOffset = this.dataFile.length();
        //start timestamp
        final long start = System.nanoTime();
        //write data to file
        this.dataWriter.handleData(bytes,offset);
        //stop timestamp
        final long stop = System.nanoTime();
        //write metadata to index file (offsetWhereDataIsInFile, start, stop)
        this.indexWriter.handleData(toByteArray(start));
        this.indexWriter.handleData(toByteArray(stop));
        this.indexWriter.handleData(toByteArray(currentFileOffset));
    }

    @Override
    public void finish() {
        log.log(Level.INFO, "Finish WriterArchival \n AutomaticFileRemoval: "
                + automaticFileRemove +" -|- File Existing: " + dataFile.exists());
        this.dataWriter.finish();
        this.indexWriter.finish();
        //set finished flag
        this.isFinished = true;
        //remove files
        if(this.dataFile.exists() && automaticFileRemove){
            log.log(Level.INFO, "Removing DataFile from WriterArchival Test.\n Size: " + this.dataFile.length());
            FileManager.removeFile(this.dataFile);
        }
        if(this.indexFile.exists() && automaticFileRemove){
            log.log(Level.INFO, "Removing IndexFile from WriterArchival Test.\n Size: " + this.dataFile.length());
            FileManager.removeFile(this.indexFile);
        }
        if(this.pt != null)
            printEvaluationData();
    }

    @Override
    public boolean isFinished() {
        return this.isFinished;
    }

    @Override
    public void run() {
        if(chunk != null && edge != null){
            for(int i = 0; i < this.iteration; i++){
                this.handleData(this.chunk);
            }
            this.handleData(this.edge,this.edge.length); //fix edge case (missing bytes)
            this.finish(); //finish writer - to close stream and open file locks
        }else{
            log.log(Level.WARNING,"Please set a chunk and a edge chunk. Stopping here. Can't progress without");
            this.isFinished = true;
        }
    }

    @Override
    public PerformanceTimer getTimer() {
        return this.pt;
    }

    /**
     * Cast a long to a byte array.
     * @param element element to cast
     */
    private byte[] toByteArray(final long element){
        final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(element);
        return buffer.array();
    }

    /**
     * set the parameters needed for a run as Thread.
     * @param chunk chunk to write multiple times
     * @param edge edge chunk to achieve the desired count
     * @param it how many times should the whole chunk be written to file
     */
    public void setRunParameters(final byte[] chunk, final byte[] edge, final int it){
        this.chunk = chunk;
        this.edge = edge;
        this.iteration = it;
    }

    /**
     * get the set chunk.
     * @return byte array with set chunk
     */
    public byte[] getChunk() {
        return chunk;
    }

    /**
     * get the edge chunk.
     * @return byte array with set edge chunk
     */
    public byte[] getEdge() {
        return edge;
    }

    /**
     * get the iteartion count that should be fulfilled.
     * @return interger with the count
     */
    public int getIteration() {
        return iteration;
    }

    @Override
    public boolean getAutomaticFileRemoval() {
        return this.automaticFileRemove;
    }

    @Override
    public void setAutomaticFileRemoval(final boolean flag) {
        this.automaticFileRemove = flag;
        if(this.dataWriter != null)
            this.dataWriter.setAutomaticFileRemoval(flag);
        if(this.indexWriter != null)
            this.indexWriter.setAutomaticFileRemoval(flag);
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public void printEvaluationData() {
        log.log(Level.INFO,"Server passing logged measurements to file!");
        final String path;
        if(this.getFileName().contains(".")){
            path = this.getFileName().split("\\.")[0];
        }else{
            path = this.getFileName();
        }
        if(this.pt != null){
            PerformanceTimerHelper.createEvaluationData(path,"writer",this.pt);
        }
    }
}
