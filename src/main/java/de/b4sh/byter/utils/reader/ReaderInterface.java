/*
 * File: ReaderInterface
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-09
 * Type: Interface
 */
package de.b4sh.byter.utils.reader;

/**
 * Interface for reader implementations.
 * This defines the base set of functions that every reader should have.
 */
public interface ReaderInterface {

    /**
     * Base read implementation for reading from a file.
     * This should be the base function for readNextBlock() and readNextBlock(length).
     * Both are based on a global offset which gets read.
     * @param b predefined byte array
     * @return byte array
     */
    byte[] read(byte[] b);

    /**
     * Read the next block of bytes from a file.
     * @return byte array of the next data chunk
     */
    byte[] readNextBlock();

    /**
     * Read the next block of bytes from a file.
     * @param length how many bytes should be read
     * @return byte array of the next data chunk
     */
    byte[] readNextBlock(int length);

    /**
     * Read the next block of bytes from a file with a specific offset given.
     * @param length how many bytes should be read
     * @param offset offset to read data from
     * @return byte array of the next data chunk
     */
    byte[] readNextBlock(int length, int offset);

    /**
     * Checks if there a next block to read.
     * @return true | false
     */
    boolean hasNextBlock();

    /**
     * Close up every open stream.
     */
    void finish();

    /**
     * if the reader should read continously data.
     * if yes implement some kind of callback to push data to.
     */
    void run();

}
