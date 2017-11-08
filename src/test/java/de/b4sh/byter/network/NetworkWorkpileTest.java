/*
 * File: NetworkWorkpileTest
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-05
 * Type: Class
 */
package de.b4sh.byter.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.b4sh.byter.server.network.NetworkInterface;
import de.b4sh.byter.server.network.NetworkWorkpile;
import de.b4sh.byter.utils.data.ChunkGenerator;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.PortScanner;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.writer.WriterBuffered;
import de.b4sh.byter.utils.writer.WriterInterface;

public class NetworkWorkpileTest {

    private static String testSpaceDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "network_workpile_test";
    private static final Logger log = Logger.getLogger(NetworkWorkpileTest.class.getName());
    private static ServerSocket server;
    private static int serverPort;
    private static Socket serverClientSocket;
    private static Socket client;

    @BeforeClass
    public static void buildTestEnvironment() throws IOException {
        FileManager.createFolder(testSpaceDirectory);
        serverPort = findNextOpenPort(61500, 10);
        server = startServer(serverPort);
    }

    @AfterClass
    public static void clearAfterEnvironment(){
        log.log(Level.INFO, "NetworkWorkpileTest AfterClass Block");
        FileManager.removeAllFilesInDirectory(testSpaceDirectory);
    }

    @Test
    public void testNetworkWorkpile(){
        final ClientSender cs = new ClientSender();
        final ServerRunner sr = new ServerRunner();
        final Thread cT = new Thread(cs);
        final Thread sT = new Thread(sr);
        sT.start();
        cT.start();
        //wait until threads are up and running
        while(client == null){
            log.log(Level.FINEST, "Test{Client,Server} not running yet. Please wait a bit!");
        }
        //
        final File file = new File(testSpaceDirectory,"network_workpile_test.file");
        final WriterInterface wi = new WriterBuffered(8000,file);
        wi.setAutomaticFileRemoval(false);
        final NetworkInterface ni = new NetworkWorkpile(64000,wi, serverClientSocket,null);
        final Thread niT = new Thread((NetworkWorkpile)ni);
        niT.start();
        ThreadManager.nap(500); //sleep 500ms
        sendOneHundretKiloByte(client);
        ThreadManager.nap(2000); //nap a bit for the test to run inside their related threads.
        Assert.assertTrue(file.exists());
        Assert.assertEquals(100000,file.length());
    }

    private static ServerSocket startServer(final int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);
        socket.setReuseAddress(true);
        return socket;
    }

    private static int findNextOpenPort(final int startPort, final int range){
        //start socket service
        return PortScanner.getNextPort(startPort,range);
    }

    private static void sendOneHundretKiloByte(final Socket clientSocket){
        try(
            final BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream(),64000);
        ){
            final byte[] chunk = ChunkGenerator.generateChunk(10000);
            for(int i = 0; i < 10; i++){
                out.write(chunk);
                out.flush();
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception during sending data to serverSocket.");
        }
        closeConnectionFromClientSide(clientSocket); //close socket at the end
    }

    private static void closeConnectionFromClientSide(final Socket clientSocket){
        try{
            clientSocket.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception during Socket close.");
        }
    }

    final class ClientSender implements Runnable{

        private final Logger log = Logger.getLogger(ClientSender.class.getName());

        @Override
        public void run() {
            try {
                client = new Socket("127.0.0.1", serverPort);
            } catch (final IOException e) {
                log.log(Level.WARNING, "IO Exception in ClientSender");
            }
        }
    }

    final class ServerRunner implements Runnable{

        private final Logger log = Logger.getLogger(ServerRunner.class.getName());

        @Override
        public void run() {
            try {
                serverClientSocket = server.accept();
            } catch (IOException e) {
                log.log(Level.WARNING, "IO Exception while accepting a client on server socket.");
            }
        }
    }

}
