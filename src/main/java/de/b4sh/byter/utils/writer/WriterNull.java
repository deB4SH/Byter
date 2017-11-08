/*
 * File: WriterBuffered
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-11
 * Type: Class
 */
package de.b4sh.byter.utils.writer;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.measurements.PerformanceTimerHelper;

/**
 * Null-Writer implementation for server.
 * This class does nothing with its given byte arrays.
 */
public final class WriterNull implements WriterInterface,Runnable {

    private static final Logger log = Logger.getLogger(WriterNull.class.getName());
    private final PerformanceTimer pt;

    /**
     * Constructor for null writer.
     * @param writerBufferSize null
     * @param fileToWriteTo null
     */
    public WriterNull(final int writerBufferSize, final File fileToWriteTo) {
        this.pt = null;
    }

    /**
     * Constructor for null writer.
     * @param writerBufferSize null
     * @param fileToWriteTo null
     * @param pt timer that collects the measurements
     */
    public WriterNull(final int writerBufferSize, final File fileToWriteTo, final PerformanceTimer pt) {
        this.pt = pt;
    }

    @Override
    public void handleData(final byte[] bytes) {
        //no operation
    }

    @Override
    public void handleData(final byte[] bytes, final int offset) {
        //no operation
    }

    @Override
    public void finish() {
        //no operation
    }

    /**
     * Returns always true
     * @return true
     */
    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void run() {
        //no operation
    }

    @Override
    public PerformanceTimer getTimer() {
        return this.pt;
    }

    /**
     * set the parameters needed for a run as Thread.
     * this writer does nothing with the passed data
     * @param chunk chunk to write multiple times
     * @param edge edge chunk to achieve the desired count
     * @param interations how many times should the whole chunk be written to file
     */
    public void setRunParameters(final byte[] chunk, final byte[] edge, final int interations){
        //no operation
    }

    /**
     * get the set chunk.
     * @return null
     */
    public byte[] getChunk() {
        return null;
    }

    /**
     * get the edge chunk.
     * @return null
     */
    public byte[] getEdge() {
        return null;
    }

    /**
     * get the iteartion count that should be fulfilled.
     * @return interger min value as null
     */
    public int getIteration() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean getAutomaticFileRemoval() {
        return false;
    }

    @Override
    public void setAutomaticFileRemoval(final boolean flag) {
        
    }

    @Override
    public String getFileName() {
        return "";
    }

    @Override
    public void printEvaluationData() {
        log.log(Level.INFO,"Server passing logged measurements to file!");
        //check if evaluation folder is existing
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
