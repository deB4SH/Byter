package de.b4sh.byter.server.network;

import java.io.BufferedOutputStream;

/**
 * Interface for service implementation that should be supported.
 * Additional function could be expected on
 */
public interface NetworkInterface {

    /**
     * Handles the passed data.
     * @param data data array
     */
    void handleData(byte[] data);

    /**
     * Handles the passed data with a specific index with actual data.
     * @param data data array
     * @param index index how many of the data is actually new
     */
    void handleData(byte[] data, int index);

    /**
     * Function for closing the opened socket.
     */
    void finishSocket();

    /**
     * Function for sending a PING-PONG between server and client.
     * @param out output stream
     */
    void sendHeartBeat(BufferedOutputStream out);

    /**
     * Run Method to start the NetworkInterface.
     */
    void run();

    /**
     * Function to output evaluation data.
     */
    void printEvaluationData();

    /**
     * function to announce if the server is done with processing the incomming packages by the client.
     * this function is needed for async test cases.
     * @return true | false (default)
     */
    boolean isDone();

}
