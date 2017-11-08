/*
 * File: ReaderRandomAccessFile
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-09
 * Type: Class
 */
package de.b4sh.byter.utils.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class reads files as byte stream.
 * Based on Random Access File
 */
public final class ReaderRandomAccessFile implements ReaderInterface,Runnable {

    private final Logger log = Logger.getLogger(ReaderRandomAccessFile.class.getName());
    private final File readFile;
    private final int blockSize;
    private final RandomAccessFile raf;
    private long internalFileOffset;
    private boolean keepAlive;
    
    /**
     * Constructor for ReaderRandomAccessFile.
     * @param blockSize the size each individual block should have
     * @param readFile the file that should be read
     */
    public ReaderRandomAccessFile(final int blockSize, final File readFile) throws FileNotFoundException {
        this.blockSize = blockSize;
        this.readFile = readFile;
        this.internalFileOffset = 0;
        //raf init
        this.raf = new RandomAccessFile(this.readFile,"r");
    }

    @Override
    public byte[] read(byte[] b) {
        try{
            //check if the bytesize needs to be reduced
            final int maxLengthPossible = getAllowedReadLength(b.length);
            if(b.length != maxLengthPossible )
                b = new byte[maxLengthPossible];
            this.raf.seek(internalFileOffset);
            this.raf.read(b);
            internalFileOffset += b.length;
            return b;
        } catch (IOException e) {
            log.log(Level.INFO,"IO Exception during read.");
            return new byte[0];
        }
    }

    @Override
    public byte[] readNextBlock() {
        return this.read(new byte[this.blockSize]);
    }

    @Override
    public byte[] readNextBlock(final int length) {
        return this.read(new byte[length]);
    }

    @Override
    public byte[] readNextBlock(final int length, final int offset) {
        try{
            byte[] b;
            //check if the bytesize needs to be reduced
            final int maxLengthPossible = getAllowedReadLength(length);
            if(length != maxLengthPossible )
                b = new byte[maxLengthPossible];
            else
                b = new byte[length];
            this.raf.seek(internalFileOffset);
            this.raf.read(b);
            return b;
        } catch (IOException e) {
            log.log(Level.INFO,"IO Exception during read.");
            return new byte[0];
        }
    }

    @Override
    public boolean hasNextBlock() {
        return this.internalFileOffset != this.readFile.length();
    }

    @Override
    public void finish() {
        this.keepAlive = false; //stop the run process if one was started
        try {
            this.raf.close();
        } catch (IOException e) {
            log.log(Level.INFO,"IO Exception during the close of stream");
        }
    }

    @Override
    public void run() {
        this.keepAlive = true;
        while (keepAlive){
            if(this.hasNextBlock()){
                this.readNextBlock();
                //read block is currently not interesting - implement a callback if you need the content also
                //maybe rxJava observable -> subscriber pattern ;)
            }else{
                try{
                    Thread.sleep(250); //sleep 250ms
                } catch (InterruptedException e) {
                    log.log(Level.WARNING, "cannot sleep in nap");
                }
            }
        }
    }

    /**
     * checks if the target read length in realtion to the file offset is bigger than the actual file length.
     * @param targetLength maximum length to reach
     * @return actual allowed max length
     */
    private int getAllowedReadLength(final int targetLength){
        if(this.internalFileOffset + targetLength > this.readFile.length()){
            return (int) (this.readFile.length() - this.internalFileOffset);
        }else{
            return targetLength;
        }
    }

    /**
     * Set the keep alive status.
     * Mainly to end the run method.
     * @param keepAlive flag for keep alive
     */
    public void setKeepAlive(final boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
}
