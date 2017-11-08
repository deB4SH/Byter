/*
 * File: NetworkDatagram
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-31
 * Type: Class
 */
package de.b4sh.byter.server.network;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
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
 * DataInput implementation for network purposes.
 */
public final class NetworkDataInput implements NetworkInterface {

    private static final Logger log = Logger.getLogger(NetworkDataInput.class.getName());
    private final Socket clientSocket;
    private final WriterInterface writer;
    private final int bufferSize;
    private final PerformanceTimer pt;
    private NetworkMode networkMode;
    private boolean isDone;

    /**
     * constructor for networkDatagram socket.
     * @param bufferSize size of the actual receive "buffer"
     * @param writer writer that should write data to disc
     * @param clientSocket socket client is connected to
     */
    public NetworkDataInput(final int bufferSize, final WriterInterface writer, final Socket clientSocket){
        this(bufferSize,writer,clientSocket,null);
    }

    /**
     * constructor for networkDatagram socket.
     * @param bufferSize size of the actual receive "buffer"
     * @param writer writer that should write data to disc
     * @param clientSocket socket client is connected to
     * @param pt performance timer to use
     */
    public NetworkDataInput(final int bufferSize, final WriterInterface writer, final Socket clientSocket, final PerformanceTimer pt){
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
        writer.handleData(data,index);
    }

    @Override
    public void finishSocket() {
        log.log(Level.INFO, "Clear up the NetworkDataInput Implementation");
        this.networkMode = NetworkMode.FINISH;
        try{
            this.writer.finish();
            this.clientSocket.close();
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
        log.log(Level.INFO, "RUN: NetworkDataInput");
        try(
            final DataInputStream din = new DataInputStream(this.clientSocket.getInputStream());
            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(this.clientSocket.getOutputStream());
        ){
            final byte[] buffer = new byte[this.bufferSize];
            while (!this.clientSocket.isClosed()) {
                int n;
                int current = 0;
                while ((n = din.read(buffer, current, buffer.length - current)) >= 0) {
                    current += n; //increment the current position inside the buffer with the read bytes
                    //if current equals length of the buffer or there is nothing left in the input
                    if (current == this.bufferSize || n == -1) {
                        handleData(buffer);
                        current = 0;
                    }
                }
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
        log.log(Level.INFO,"Server passing logged measurements to file!");
        if(this.pt != null){
            PerformanceTimerHelper.createEvaluationData(this.writer.getFileName(),"network",this.pt);
        }
    }

    @Override
    public boolean isDone() {
        return this.isDone;
    }
}
