/*
 * File: NetworkClientInterface
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-21
 * Type: Interface
 */
package de.b4sh.byter.client.network;

/**
 * Interface to declare base functions for each network implementation on the client side.
 */
public interface NetworkClientInterface {

    /**
     * Function for pregenerating a chunk.
     * Returns a filled chunk.
     * @param chunkToFill chunk to fill with data.
     * @return filled chunk
     */
    byte[] pregenerateChunk(byte[] chunkToFill);

    /**
     * Get the actual chunk size of the filled chunk.
     * @return int with size
     */
    int getActualChunkSize();

    /**
     * Get the current status of the NetworkClientInterface.
     * Checks if the process is still running or not.
     * @return boolean true | false
     */
    boolean isRunning();

    /**
     * Method to clean everything or print results to files.
     */
    void finish();
}
