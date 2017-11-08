package de.b4sh.byter.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import de.b4sh.byter.utils.io.PortScanner;
import de.b4sh.byter.utils.io.ThreadManager;

public final class PortScannerTest {

    private static final Logger log = Logger.getLogger(PortScanner.class.getName());

    @Test
    public void testPortScanner(){
        int port = 0;
        port = PortScanner.getNextPort(64800,10);
        Assert.assertNotEquals(port,0);
        Assert.assertEquals(64800, port);
        log.info("Port is: " + port);
    }

    @Test
    public void testPortScannerBlockedPort() throws IOException {
        final int portToTest = 64820;
        final ServerSocket blockingSocket = startServer(portToTest);
        final Thread serverThread = startSimpleListenerOnServer(blockingSocket);
        ThreadManager.nap(250); //just a bit time for the thread to start.
        final int port = PortScanner.getNextPort(portToTest, 1);
        Assert.assertNotNull(port);
        Assert.assertEquals(-1, port);
        serverThread.stop();
    }

    @Test
    public void testPortScannerWithMultipleBlockedPorts() throws IOException {
        final int startPort = 64840;
        final int portToTest = 64843;
        final List<ServerSocket> blockingSockets = new ArrayList<>();
        final List<Thread> blockingThreads = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            blockingSockets.add(startServer(startPort+i));
        }
        for(ServerSocket s: blockingSockets){
            blockingThreads.add(startSimpleListenerOnServer(s));
        }
        ThreadManager.nap(250);//just a bit time for the thread to start.
        final int port = PortScanner.getNextPort(startPort,5);
        Assert.assertNotNull(port);
        Assert.assertEquals(portToTest, port);
    }

    private static ServerSocket startServer(final int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);
        socket.setReuseAddress(true);
        return socket;
    }

    private static Thread startSimpleListenerOnServer(final ServerSocket socket){
        final Thread t1 = new Thread(new ServerRunner(socket));
        t1.start();
        return t1;
    }

    static final class ServerRunner implements Runnable{
        private final Logger log = Logger.getLogger(ServerRunner.class.getName());
        private final ServerSocket socket;

        ServerRunner(ServerSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                this.socket.accept();
            } catch (IOException e) {
                log.log(Level.WARNING,"IO Exception during accepting socket.");
            }
        }
    }
}
