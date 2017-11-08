/*
 * File: NetworkClient
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-07-22
 * Type: Class
 */
package de.b4sh.byter.client.network;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.measurements.Measurement;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.measurements.PerformanceTimerHelper;

/**
 * Class for network based tasks.
 * Sending sweet bytes to server eg.
 * using BufferedOutputStream to transmit data.
 * @see de.b4sh.byter.server.Server
 */
public final class PlainNetworkClient implements NetworkClientInterface,Runnable{
    private static final Logger log = Logger.getLogger(PlainNetworkClient.class.getName());
    private final String testName;
    private final String hostAddress;
    private final int hostPort;
    private final int pregeneratedChunkSize;
    private final long transmitSize;
    private final byte[] pregeneratedChunk;
    private final int networkBufferSize;
    private final PerformanceTimer pt;
    private boolean isRunning;



    /**
     * General constructor for NetworkClientInterface.
     * @param hostAddress host address of server
     * @param hostPort host port of server
     * @param pregeneratedChunkSize size of the chunk that should be pregenerated
     * @param networkBufferSize the network buffer size
     * @param transmitSize the size of data to transmit
     * @param testName the current active testName
     */
    public PlainNetworkClient(final String hostAddress, final int hostPort, final int networkBufferSize,
                              final int pregeneratedChunkSize, final long transmitSize, final String testName) {
        this.testName = testName;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.networkBufferSize = networkBufferSize;
        this.pregeneratedChunkSize = pregeneratedChunkSize;
        this.transmitSize = transmitSize;
        this.pregeneratedChunk = this.pregenerateChunk(new byte[this.pregeneratedChunkSize]);
        this.pt = null;
        this.run();
    }

    /**
     * General constructor for NetworkClientInterface.
     * Also takes a performance timer to take measurements.
     * @param hostAddress host address of server
     * @param hostPort host port of server
     * @param pregeneratedChunkSize size of the chunk that should be pregenerated
     * @param networkBufferSize the network buffer size
     * @param transmitSize the size of data to transmit
     * @param pt performance timer to use
     * @param testName the current active testName
     */
    public PlainNetworkClient(final String hostAddress, final int hostPort, final int networkBufferSize,
                              final int pregeneratedChunkSize, final long transmitSize,
                              final PerformanceTimer pt, final String testName) {
        this.testName = testName;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.networkBufferSize = networkBufferSize;
        this.pregeneratedChunkSize = pregeneratedChunkSize;
        this.transmitSize = transmitSize;
        this.pregeneratedChunk = this.pregenerateChunk(new byte[this.pregeneratedChunkSize]);
        this.pt = pt;
        this.run();
    }

    private void printRunStatistic(){
        log.log(Level.INFO,"PlainNetworkClient Configuration. \n"
                + "Host: " + this.hostAddress + ":" + this.hostPort + " \n"
                + "BufferSize: " + this.networkBufferSize + " \n"
                + "ChunkSize: " + this.pregeneratedChunk.length + " \n"
                + "TransmitTarget: " + this.transmitSize
        );
    }

    /**
     * run method that starts the transmission of data.
     */
    @Override
    public void run() {
        this.isRunning = true;
        this.printRunStatistic();
        try(
            final Socket ss = new Socket(this.hostAddress, this.hostPort); //server socket
            final BufferedOutputStream outStream = new BufferedOutputStream(ss.getOutputStream(),this.networkBufferSize)
        ){
            //calc how many full byte arrays are needed to transport to fulfill the main part of the transmission
            final int fulfillmentRuns = (int)(this.transmitSize/this.pregeneratedChunk.length);
            //calculate the edge case to fulfill the target
            final byte[] edgeCase = this.pregenerateChunk(new byte[(int)(this.transmitSize % pregeneratedChunkSize)]);
            //transmit data
            for(int i = 0; i < fulfillmentRuns; i++){
                if(this.pt == null)
                    this.sendData(outStream,this.pregeneratedChunk);
                else
                    this.sendDataWithMeasurement(outStream,this.pregeneratedChunk);
            }
            //edge work
            if(this.pt == null)
                this.sendData(outStream,edgeCase);
            else
                this.sendDataWithMeasurement(outStream,edgeCase);
        } catch (UnknownHostException e) {
            log.log(Level.WARNING,"cannot resolve host. UnknownHostException in PlainNetworkClient run");
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception in run Method. Check stacktrace!",e);
        }
        this.isRunning = false;
        this.finish();
    }

    private void sendDataWithMeasurement(final BufferedOutputStream outputStream, final byte[] data) throws IOException {
        final long tStart = System.nanoTime();
        outputStream.write(data);
        final long tEnd = System.nanoTime();
        this.pt.addNewMeasurement(new Measurement("PlainNetworkClient",data.length,tStart,tEnd));
    }

    private void sendData(final BufferedOutputStream outputStream, final byte[] data) throws IOException {
        outputStream.write(data);
    }

    @Override
    public byte[] pregenerateChunk(final byte[] chunkToFill) {
        new Random().nextBytes(chunkToFill);
        return chunkToFill;
    }

    /**
     * get the actual pregenerated chunk size.
     * @return int with the pregenerated chunk size
     */
    @Override
    public int getActualChunkSize() {
        return this.pregeneratedChunk.length;
    }

    /**
     * Get the current status of the NetworkClientInterface.
     * Checks if the process is still running or not.
     * @return boolean true | false
     */
    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void finish() {
        if(this.pt != null){
            this.createEvaluationData();
        }
    }

    /**
     * Get the set pregenerated chunk size.
     * @return int with the estimated value
     */
    public int getPregeneratedChunkSize(){
        return this.pregeneratedChunkSize;
    }

    private void createEvaluationData(){
        log.log(Level.INFO,"PlainNetworkClient passing logged measurements to file!");
        if(this.pt != null){
            PerformanceTimerHelper.createEvaluationData(this.testName,"plain_network",this.pt);
        }
    }
}
