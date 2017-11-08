/*
 * File: WriterWorkpile
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-11
 * Type: Class
 */
package de.b4sh.byter.utils.writer;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.measurements.PerformanceTimerHelper;

/**
 * This class represents the idea of working with a workpile to accept multiple arrays of data and write them sync.
 */
public final class WriterWorkpile implements WriterInterface,Runnable{

    private static final Logger log = Logger.getLogger(WriterWorkpile.class.getName());
    private final Queue<byte[]> workpile;
    private final WriterType workingWriter;
    private final File writeFile;
    private final String fileName;
    private final int writerBufferSize;
    private final PerformanceTimer pt;
    private final boolean takeMeasurements;
    //pile writer
    private WorkpileWriter workpileWriter;
    private Thread workpileWorkerThread;
    private boolean keepWorkerAlive;
    private boolean tryFinish;
    //run parameter
    private byte[] chunk;
    private byte[] edge;
    private int iteration;
    private boolean isFinished;
    private boolean automaticFileRemoval;

    /**
     * Constructor for Workpile Writer.
     * @param writeFile the file to write data to
     * @param writerBufferSize
     */
    public WriterWorkpile(final File writeFile, final WriterType workingWriter, final int writerBufferSize){
        this.writeFile = writeFile;
        this.fileName = this.writeFile.getName();
        this.workingWriter = workingWriter;
        this.writerBufferSize = writerBufferSize;
        this.workpile = new LinkedList<>();
        this.pt = null;
        this.takeMeasurements = false;
        this.tryFinish = false;
        this.automaticFileRemoval = true;
        this.initWorkpileWorker();
    }

    private void initWorkpileWorker(){
        switch(this.workingWriter){
            case BufferedWriter:{
                this.workpileWriter = new WorkpileWriter(new WriterBuffered(this.writerBufferSize,this.writeFile));
            }
        }
        this.keepWorkerAlive = true;
        this.workpileWorkerThread = new Thread(this.workpileWriter);
        this.workpileWorkerThread.setDaemon(true);
        this.workpileWorkerThread.start();
    }

    @Override
    public void handleData(byte[] bytes) {
        this.handleData(bytes,bytes.length);
    }

    @Override
    public void handleData(byte[] bytes, int offset) {
        this.workpile.add(bytes);
    }

    @Override
    public void finish() {
        this.tryFinish = true;
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
            log.log(Level.WARNING,"Please set a chunk and a edge chunk ");
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
     * @return byte array with set edge chunk-
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
    public void setAutomaticFileRemoval(boolean flag) {
        this.automaticFileRemoval = flag;
        this.workpileWriter.carryFileRemovalFlag(flag);
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

    /**
     * Anonymous Worker class to write piled byte-arrays to file.
     */
    final class WorkpileWriter implements Runnable{
        private final Logger workingLog = Logger.getLogger(WorkpileWriter.class.getName());
        private final WriterInterface writer;

        WorkpileWriter(final WriterInterface writer) {
            this.writer = writer;
        }

        @Override
        public void run() {
            while (keepWorkerAlive) {
                if (workpile.size() > 0) {
                    final byte[] pile = workpile.poll();//take first element
                    this.writer.handleData(pile); //write element to file
                } else {
                    this.nap(250); //wait for 250ms and check again if there is new content
                }
                if(tryFinish && workpile.isEmpty()){
                    keepWorkerAlive = false;
                    this.writer.finish(); //finish subwriter to close open locks
                    ThreadManager.nap(125); //wait 125ms for the writer to close the stream and everthing
                    if(writeFile.exists() && automaticFileRemoval){
                        log.log(Level.INFO, "Removing DataFile from WriterWorkpile Test.");
                        FileManager.removeFile(writeFile);
                    }
                    if(pt != null){
                        printEvaluationData();
                    }
                }
            }
        }

        public void carryFileRemovalFlag(final boolean flag){
            this.writer.setAutomaticFileRemoval(flag);
        }

        /**
         * Let the Thread sleep a bit
         * @param napTime time to sleep in ms
         */
        private void nap(final long napTime){
            try {
                Thread.sleep(napTime);
            } catch (InterruptedException e) {
                workingLog.log(Level.WARNING,"Exception during nap(). See Stacktrace for issue please",e);
            }
        }
    }
}
