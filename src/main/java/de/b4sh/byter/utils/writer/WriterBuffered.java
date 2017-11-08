/*
 * File: WriterBuffered
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-11
 * Type: Class
 */
package de.b4sh.byter.utils.writer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.measurements.Measurement;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.measurements.PerformanceTimerHelper;

/**
 * Buffered Output Stream implementation for server.
 */
public final class WriterBuffered implements WriterInterface,Runnable {

    private static final Logger log = Logger.getLogger(WriterBuffered.class.getName());
    private final File writeFile;
    private final String fileName;
    private final PerformanceTimer pt;
    private final boolean takeMeasurements;
    private BufferedOutputStream bos;
    //run parameter
    private byte[] chunk;
    private byte[] edge;
    private int iteration;
    private boolean isFinished;
    private boolean automaticFileRemoval;

    /**
     * Constructor for Buffered Output Stream Writer.
     * @param writerBufferSize the size of the buffer size
     * @param fileToWriteTo file destination to write data to
     */
    public WriterBuffered(final int writerBufferSize, final File fileToWriteTo) {
        this.writeFile = fileToWriteTo;
        this.fileName = this.writeFile.getName();
        this.automaticFileRemoval = true;
        this.pt = null;
        this.takeMeasurements = false;
        this.isFinished = false;
        try{
            final FileOutputStream fos = new FileOutputStream(fileToWriteTo,true);
            bos = new BufferedOutputStream(fos,writerBufferSize);
        } catch (FileNotFoundException e) {
            log.log(Level.WARNING,"File not found Exception during init of WriterBuffered",e);
        }
    }

    /**
     * Constructor for Buffered Output Stream Writer.
     * Also including a performanceTimer for measurements
     * @param writerBufferSize the size of the buffer size
     * @param fileToWriteTo file destination to write data to
     * @param timer timer that collects the measurements
     */
    public WriterBuffered(final int writerBufferSize, final File fileToWriteTo, final PerformanceTimer timer) {
        this.writeFile = fileToWriteTo;
        this.fileName = this.writeFile.getName();
        this.automaticFileRemoval = true;
        this.pt = timer;
        this.takeMeasurements = true;
        try{
            final FileOutputStream fos = new FileOutputStream(fileToWriteTo,true);
            bos = new BufferedOutputStream(fos,writerBufferSize);
        } catch (FileNotFoundException e) {
            log.log(Level.WARNING,"File not found Exception during init of WriterBuffered",e);
        }
    }

    @Override
    public void handleData(final byte[] bytes) {
        this.handleData(bytes,bytes.length);
    }

    @Override
    public void handleData(final byte[] bytes, final int offset) {
        if(null != bos){
            if(takeMeasurements)
                handleDataWithMeasurement(bytes,offset);
            else
                handleDataWithoutMeasurement(bytes,offset);
        }
    }

    private void handleDataWithMeasurement(final byte[] bytes, final int offset){
        try{
            final long tStart = System.nanoTime();
            bos.write(bytes,0,offset);
            bos.flush();
            final long tEnd = System.nanoTime();
            pt.addNewMeasurement(new Measurement(WriterBuffered.class.getName(),offset,tStart,tEnd));
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during data processing. Check Stacktrace for details.",e);
        }
    }

    private void handleDataWithoutMeasurement(final byte[] bytes, final int offset){
        try{
            bos.write(bytes,0,offset);
            bos.flush();
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during data processing. Check Stacktrace for details.",e);
        }
    }

    @Override
    public void finish() {
        log.log(Level.INFO, "Finish WriterBuffered \n AutomaticFileRemoval: "
                + this.automaticFileRemoval +" -|- File Existing: " + this.writeFile.exists());
        if(null != bos){
            try {
                bos.close();
                this.isFinished = true;
                ThreadManager.nap(125); //wait 125ms for the writer to close the stream and everthing
                if(this.writeFile.exists() && this.automaticFileRemoval){
                    log.log(Level.INFO, "Removing DataFile from WriterBuffered Test.\n Size: " + this.writeFile.length());
                    FileManager.removeFile(this.writeFile);
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "IO Exception on closing the BufferedOutputStream.");
            }
        }
        if(this.pt != null && !this.pt.getData().isEmpty())
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
        return this.automaticFileRemoval;
    }

    @Override
    public void setAutomaticFileRemoval(final boolean flag) {
        this.automaticFileRemoval = flag;
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
