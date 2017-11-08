/*
 * File: WriterBuffered
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-11
 * Type: Class
 */
package de.b4sh.byter.utils.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.measurements.Measurement;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.measurements.PerformanceTimerHelper;

/**
 * FileChannel Writer implementation for server.
 */
public final class WriterFileChannel implements WriterInterface,Runnable {

    private static final Logger log = Logger.getLogger(WriterFileChannel.class.getName());
    private FileChannel channel;
    private File writeFile;
    private final String fileName;
    private final PerformanceTimer pt;
    private final boolean takeMeasurements;
    //run parameter
    private byte[] chunk;
    private byte[] edge;
    private int iteration;
    private boolean isFinished;
    private boolean automaticFileRemoval;

    /**
     * Constructor for FileChannel.
     * @param writerBufferSize buffer size for file channel implementation.
     * @param fileToWriteTo file to write to
     */
    public WriterFileChannel(final int writerBufferSize, final File fileToWriteTo) {
        this.pt = null;
        this.takeMeasurements = false;
        this.writeFile = fileToWriteTo;
        this.fileName = this.writeFile.getName();
        this.automaticFileRemoval = true;
        try{
            channel = new FileOutputStream(fileToWriteTo,true).getChannel();
        } catch (FileNotFoundException e) {
            log.log(Level.WARNING,"File not found Exception during init of WriterFileChannel");
        }
    }

    /**
     * Constructor for FileChannel.
     * @param writerBufferSize buffer size for file channel implementation.
     * @param fileToWriteTo file to write to
     * @param timer timer that collects the measurements
     */
    public WriterFileChannel(final int writerBufferSize, final File fileToWriteTo, final PerformanceTimer timer) {
        this.pt = timer;
        this.takeMeasurements = true;
        this.writeFile = fileToWriteTo;
        this.fileName = this.writeFile.getName();
        this.automaticFileRemoval = true;
        try{
            channel = new FileOutputStream(fileToWriteTo,true).getChannel();
        } catch (FileNotFoundException e) {
            log.log(Level.WARNING,"File not found Exception during init of WriterFileChannel");
        }
    }

    @Override
    public void handleData(final byte[] bytes) {
        this.handleData(bytes,bytes.length);
    }

    @Override
    public void handleData(final byte[] bytes, final int offset) {
        if(this.takeMeasurements)
            this.handleDataWithMeasurements(bytes,offset);
        else
            this.handleDataWithoutMeasurements(bytes,offset);
    }

    private void handleDataWithMeasurements(final byte[] bytes, final int offset){
        if(null != channel){
            try {
                final long tStart = System.nanoTime();
                channel.write(ByteBuffer.wrap(bytes,0,offset));
                final long tEnd = System.nanoTime();
                this.pt.addNewMeasurement(new Measurement(WriterFileChannel.class.getName(),bytes.length,tStart,tEnd));
            } catch (IOException e) {
                log.log(Level.WARNING, "IO Exception while processing data. Check stacktrace",e);
            }
        }
    }

    private void handleDataWithoutMeasurements(final byte[] bytes, final int offset){
        if(null != channel){
            try {
                channel.write(ByteBuffer.wrap(bytes,0,offset));
            } catch (IOException e) {
                log.log(Level.WARNING, "IO Exception while processing data. Check stacktrace",e);
            }
        }
    }

    @Override
    public void finish() {
        log.log(Level.INFO, "Finish WriterFileChannel \n AutomaticFileRemoval: "
                + this.automaticFileRemoval +" -|- File Existing: " + this.writeFile.exists());
        if (null != channel){
            try {
                channel.close();
                this.isFinished = true;
                ThreadManager.nap(125); //wait 125ms for the writer to close the stream and everthing
                if (this.writeFile.exists() && automaticFileRemoval) {
                    log.log(Level.INFO, "Removing DataFile from WriterFileChannel Test");
                    FileManager.removeFile(this.writeFile);
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "IO Exception on closing FileChannel.");
            }
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
