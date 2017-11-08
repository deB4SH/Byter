/*
 * File: NetworkWorkpile
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-05
 * Type: Class
 */
package de.b4sh.byter.server.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.exception.ServerNetworkError;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.measurements.PerformanceTimerHelper;
import de.b4sh.byter.utils.writer.WriterInterface;


/**
 * NetworkWorpile class
 * Represents an async implementation which "consumes" all passed data into a workpile.
 * Network is done via BufferedOutputStream and BufferedInputStream.
 * Data is stored in a synchronized queue.
 * Write through happens over a class privat runner that pull it from the queue and put the data into the writer impl.
 * The pile uses a ConcurrentLinkedQueue.
 * @see java.util.concurrent.ConcurrentLinkedQueue
 */
public final class NetworkWorkpile implements NetworkInterface, Runnable{

    private static final Logger log = Logger.getLogger(NetworkWorkpile.class.getName());
    private final Socket clientSocket;
    private final PerformanceTimer pt;
    private int bufferSize;
    private LinkedBlockingQueue<byte[]> pile; //threadsafe queue impl
    private WriterInterface writer;
    private NetworkMode networkMode;
    private boolean keepPileWorkerAlive;
    private Thread workerThread;
    private PileRunner pileRunner;
    private boolean isDone;

    /**
     * Constructor for NetworkWorkpile without measurements.
     * @param bufferSize buffersize the network implementation should use
     * @param writer writer object to use
     * @param socket client socket
     */
    public NetworkWorkpile(final int bufferSize, final WriterInterface writer, final Socket socket){
        this(bufferSize, writer, socket, null);
    }

    /**
     * Constructor for NetworkWorkpile with measurements.
     * @param bufferSize buffersize the network implementation should use
     * @param writer writer object to use
     * @param socket client socket
     * @param pt performance timer to take measurements
     */
    public NetworkWorkpile(final int bufferSize, final WriterInterface writer, final Socket socket, final PerformanceTimer pt){
        this.bufferSize = bufferSize;
        this.writer = writer;
        this.clientSocket = socket;
        this.pt = pt;
        this.pile = new LinkedBlockingQueue<>(3000); //limit the queue length to 3000 elements
        this.pileRunner = new PileRunner();
        this.keepPileWorkerAlive = true;
        this.networkMode = NetworkMode.RUNNING;
        this.isDone = false;
    }

    @Override
    public void handleData(final byte[] data) {
        this.handleData(data,data.length);
    }

    @Override
    public void handleData(final byte[] data, final int index) {
        writer.handleData(data,index);
    }

    @Override
    public void finishSocket() {
        log.log(Level.INFO,"Clear up the NetworkWorkpile Implementation");

        try{
            //close writer first
            //this.writer.finish(); //writer should not be closed here - the writer is on it self and unchained from the network
            //close socket to client last!
            clientSocket.close();
        } catch (final IOException e) {
            log.log(Level.WARNING,"Can not close the client socket after failed heart beat request. Check Stacktrace for more details.",e);
        }
        if(this.pt != null)
            printEvaluationData();

        this.networkMode = NetworkMode.FINISH;
    }

    @Override
    public void sendHeartBeat(final BufferedOutputStream out) {
        try {
            out.write("CONNECTION CHECK".getBytes(Charset.forName("UTF-8")));
        } catch (final SocketException se){
            log.log(Level.FINE, "Cleaning up the socket.");
            finishSocket();
        } catch (final IOException e) {
            log.log(Level.INFO,"IO Exception during HeartBeat. Seems that the Client disconnected.");
        }
    }

    @Override
    public void run() {
        log.log(Level.INFO, "RUN: Networkworkpile");
        this.workerThread = new Thread(this.pileRunner);
        this.workerThread.start();
        log.log(Level.INFO, "Starting the other Part.");
        try(
            final BufferedInputStream bIn = new BufferedInputStream(this.clientSocket.getInputStream(),this.bufferSize);
            final BufferedOutputStream bOut = new BufferedOutputStream(this.clientSocket.getOutputStream());
        ){
            final byte[] buffer = new byte[this.bufferSize];
            while (!this.clientSocket.isClosed()) {
                int n;
                int current = 0;
                while ((n = bIn.read(buffer, current, buffer.length - current)) >= 0) {
                    current += n; //increment the current position inside the buffer with the read bytes
                    //if current equals length of the buffer or there is nothing left in the input
                    if (current == this.bufferSize || n == -1) {
                        while(!this.pile.offer(buffer)){
                            ThreadManager.nap(250); //nap 250ms for list to shrink
                            log.log(Level.INFO, "Pile Size: " + this.pile.size());
                        }
                        current = 0;
                    }
                }
                //do the edgecase work (last bytes to fulfill target)
                //handleData(buffer, current);
                if(current != 0) //catch the state that the client is connected but nothing is transmitted
                    this.pile.add(getEdgeChunk(buffer,current)); //add the edge case to list.
                this.sendHeartBeat(bOut);
            }
        } catch (final SocketException se){
            if(networkMode != NetworkMode.FINISH)
                log.log(Level.WARNING, ServerNetworkError.SOCKET_CLOSED.getReason());
        } catch (final IOException e) {
            log.log(Level.WARNING, "IO Exception during NetworkWorkpile run.",e);
        }
    }

    @Override
    public void printEvaluationData() {
        log.log(Level.INFO,"Server passing logged measurements to file!");
        if(this.pt != null){
            PerformanceTimerHelper.createEvaluationData(this.writer.getFileName(),"network",this.pt);
        }
    }

    @Override
    public boolean isDone() {
        return this.isDone;
    }

    private byte[] getEdgeChunk(final byte[] data, final int index){
        if(index == data.length)
            return data;
        else{
            return Arrays.copyOf(data,index);
        }
    }

    final class PileRunner implements Runnable{

        private final Logger workingLogger = Logger.getLogger(PileRunner.class.getName());

        @Override
        public void run() {
            byte[] curr;
            while (keepPileWorkerAlive){
                if((curr = pile.poll()) != null){
                    writer.handleData(curr);
                }else{
                    workingLogger.log(Level.INFO,"Queue is empty waiting 250ms for next entries or finishing the implementation.");
                    nap(250); //sleep 250 ms
                    if(networkMode == NetworkMode.FINISH && pile.isEmpty()){
                        keepPileWorkerAlive = false;
                        writer.finish();
                    }
                }
            }
            //say that the implementation is done progressing
            isDone = true;
        }

        /**
         * Let the Thread sleep a bit
         * @param napTime time to sleep in ms
         */
        private void nap(final long napTime){
            try {
                Thread.sleep(napTime);
            } catch (InterruptedException e) {
                workingLogger.log(Level.WARNING,"Exception during nap(). See Stacktrace for issue please",e);
            }
        }
    }
}
