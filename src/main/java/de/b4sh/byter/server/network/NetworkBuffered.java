package de.b4sh.byter.server.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.exception.ServerNetworkError;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.measurements.PerformanceTimerHelper;
import de.b4sh.byter.utils.writer.WriterInterface;

/**
 * Network: Buffered Implementation.
 * this class implements a BufferedInputStream as service option.
 */
public final class NetworkBuffered implements NetworkInterface, Runnable {

    private static final Logger log = Logger.getLogger(NetworkBuffered.class.getName());

    private final Socket clientSocket;
    private final PerformanceTimer pt;
    private int bufferSize = 8192;
    private WriterInterface writer;
    private NetworkMode networkMode;
    private boolean isDone;

    /**
     * Constructor for NetworkBuffered.
     * @param bufferSize buffer size of buffered service parts
     * @param writer writer implementation to use
     * @param clientSocket socket client is connected to
     */
    public NetworkBuffered(final int bufferSize, final WriterInterface writer, final Socket clientSocket) {
        this(bufferSize,writer,clientSocket,null);
    }

    /**
     * Constructor for NetworkBuffered.
     * Including taking measurements
     * @param bufferSize buffer size of buffered service parts
     * @param writer writer implementation to use
     * @param clientSocket socket client is connected to
     * @param pt performance timer to use
     */
    public NetworkBuffered(final int bufferSize, final WriterInterface writer, final Socket clientSocket, final PerformanceTimer pt) {
        this.bufferSize = bufferSize;
        this.writer = writer;
        this.clientSocket = clientSocket;
        this.pt = pt;
        this.networkMode = NetworkMode.RUNNING;
        this.isDone = false;
    }


    @Override
    public void handleData(final byte[] data) {
        this.handleData(data,data.length);
    }

    @Override
    public void handleData(final byte[] data, final int index) {
        writer.handleData(data,index); //passing the data to the linked writer implementation
        //network part has no other part on working with the data
    }

    @Override
    public void finishSocket() {
        log.log(Level.INFO,"Clear up the NetworkBuffered Implementation");
        this.networkMode = NetworkMode.FINISH;
        try{
            //close writer first
            this.writer.finish();
            //close socket to client last!
            clientSocket.close();
        } catch (final IOException e) {
            log.log(Level.WARNING,"Can not close the client socket after failed heart beat request. Check Stacktrace for more details.",e);
        }
        if(this.pt != null)
            printEvaluationData();
        this.isDone = true;
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
        log.log(Level.INFO,"RUN: NetworkBuffered");
        try(
            final BufferedInputStream bufferedInputStream = new BufferedInputStream(this.clientSocket.getInputStream(),this.bufferSize);
            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(this.clientSocket.getOutputStream());
        ) {
            final byte[] buffer = new byte[this.bufferSize];
            while (!this.clientSocket.isClosed()) {
                int n;
                int current = 0;
                while ((n = bufferedInputStream.read(buffer, current, buffer.length - current)) >= 0) {
                    current += n; //increment the current position inside the buffer with the read bytes
                    //if current equals length of the buffer or there is nothing left in the input
                    if (current == this.bufferSize || n == -1) {
                        handleData(buffer);
                        current = 0;
                    }
                }
                //do the edgecase work (last bytes to fulfill target)
                if(current != 0)
                    handleData(buffer, current);
                this.sendHeartBeat(bufferedOutputStream);
            }
        } catch (final SocketException se){
            if(networkMode != NetworkMode.FINISH)
                log.log(Level.WARNING, ServerNetworkError.SOCKET_CLOSED.getReason());
        } catch (final IOException e) {
            log.log(Level.WARNING, ServerNetworkError.IO_EXCEPTION.getReason(),e);
        }
    }

    @Override
    public void printEvaluationData() {
        log.log(Level.INFO,"Server passing logged measurements to file! Current Test: " + this.writer.getFileName());
        if(this.pt != null){
            PerformanceTimerHelper.createEvaluationData(this.writer.getFileName(),"network",this.pt);
        }
    }

    @Override
    public boolean isDone() {
        return this.isDone;
    }
}
