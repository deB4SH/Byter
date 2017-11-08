package de.b4sh.byter.utils.writer;

import de.b4sh.byter.utils.measurements.PerformanceTimer;

/**
 * General purpose Interface for every writer.
 * Declares function to use everywhere.
 */
public interface WriterInterface {

    /**
     * Shorthand function for passing a full byte array.
     * The offset is set to byteArray.length()
     * @param bytes byte array
     */
    void handleData(byte[] bytes);

    /**
     * Handle data with a specific offset told, to not read junk at the end of the array.
     * @param bytes byte array
     * @param offset max element of true data inside the array
     */
    void handleData(byte[] bytes, int offset);

    /**
     * Finish up open streams.
     * Release file locks.
     */
    void finish();

    /**
     * Get the current state of the writer interface.
     * Checks if its finished or not.
     * @return boolean with current state
     */
    boolean isFinished();

    /**
     * run method.
     */
    void run();

    /**
     * Returns the used PerformanceTimer or null if none was used.
     * @return timer with tracked data
     */
    PerformanceTimer getTimer();

    /**
     * set the parameters for the run method.
     * @param chunk chunk to write
     * @param edge edge chunk to achieve target byte size
     * @param interations how many times should the chunk be written to disc
     */
    void setRunParameters(byte[] chunk, byte[] edge, int interations);

    /**
     * get the set full chunk.
     * @return byte array with the set chunk
     */
    byte[] getChunk();

    /**
     * get the edge chunk to fulfill byte target.
     * @return byte array with edge chunk size.
     */
    byte[] getEdge();

    /**
     * get the iteration count to fulfill.
     * @return count of required runs
     */
    int getIteration();

    /**
     * Get the current flag of automaticFileRemoval.
     * @return true | false
     */
    boolean getAutomaticFileRemoval();

    /**
     * Set a new flag for automaticFileRemoval.
     * @param flag flag to set
     */
    void setAutomaticFileRemoval(boolean flag);

    /**
     * Get the current set FileName.
     * @return String with filename.
     */
    String getFileName();

    /**
     * print the available evaluation data.
     */
    void printEvaluationData();

}
