package de.b4sh.byter.integration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.b4sh.byter.client.Client;
import de.b4sh.byter.server.Server;
import de.b4sh.byter.server.network.NetworkType;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.jmx.JmxClientNetworkHelper;
import de.b4sh.byter.utils.writer.WriterType;
import de.b4sh.byter.support.ComponentHelper;
import de.b4sh.byter.utils.data.TransformValues;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.jmx.JmxServerHelper;

/**
 * InteractionTest should test if the interaction between the client and server works out as expected.
 * Parameter:
 *  Client: Sending 30MB of data to Server
 *  Server: buffered network, buffered disc, both buffer at 64k
 */
public final class InteractionTest {
    private static final Logger log = Logger.getLogger(InteractionTest.class.getName());
    private static final String JMXSERVERIP = "localhost";
    private static final String testSpaceDir = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "interaction_test";
    private static ExecutorService service;
    private static Server serverObj;
    private static Client clientObj;

    /**
     * Start Method for everything related to this Test-Case.
     */
    @BeforeClass
    public static void startup(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDir);
        //starting executorservice
        service = Executors.newFixedThreadPool(2);
        //start server
        serverObj = ComponentHelper.startServer(service);
        clientObj = ComponentHelper.startClient(service);
    }

    @AfterClass
    public static void endTest(){
        //shut down service
        ComponentHelper.shutdownService(service);
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDir);
    }

    /**
     * Test that issues 30 MegaByte of Data from Client to Server.
     * For the test environment variables, see class java doc.
     */
    @Test
    public void sendThirtyMegaByteDataTest() throws IOException {
        //connect to server
        final int bufferSize = 64000;
        final JMXConnector serverConnection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
        final MBeanServerConnection serverMbs = JmxServerHelper.getMBeanServer(serverConnection);
        final ObjectName serverNetworkManager = JmxServerHelper.findObjectName(serverMbs,"de.b4sh.byter","NetworkManager");
        //connect to client
        final JMXConnector clientConnection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,clientObj.getConnectorSystemPort());
        final MBeanServerConnection clientMbs = JmxClientNetworkHelper.getMBeanServer(clientConnection);
        final ObjectName clientNetworkController = JmxClientNetworkHelper.findObjectName(clientMbs,"de.b4sh.byter","Network");
        //assert both controller objects for setup
        Assert.assertNotNull(clientNetworkController);
        Assert.assertNotNull(serverNetworkManager);
        //set up server
        final String testName = StringGenerator.nextRandomString(10);
        JmxServerHelper.setNetworkManagerFileName(serverMbs,serverNetworkManager,testName);
        JmxServerHelper.setAutomaticFileRemoval(serverMbs,serverNetworkManager,false);
        //buffers
        JmxServerHelper.setNetworkManagerNetworkBufferSize(serverMbs,serverNetworkManager,bufferSize);
        JmxServerHelper.setNetworkManagerWriterBufferSize(serverMbs,serverNetworkManager,bufferSize);
        //types
        JmxServerHelper.setNetworkManagerWriterType(serverMbs,serverNetworkManager, WriterType.BufferedWriter.getKey());
        JmxServerHelper.setNetworkManagerNetworkType(serverMbs,serverNetworkManager, NetworkType.BufferedNetwork.getKey());
        //filepath
        JmxServerHelper.setNetworkManagerFilePath(serverMbs,serverNetworkManager,testSpaceDir);
        JmxServerHelper.startNetworkManagerDirectStoreHandler(serverMbs,serverNetworkManager);
        //wait until server is ready to accept clients
        while(!JmxServerHelper.getServerSocketAccepting(serverMbs,serverNetworkManager)){
            ThreadManager.nap(500);
            log.log(Level.INFO,"Still waiting for server to come up!");
        }
        //set up client
        JmxClientNetworkHelper.changeServerHostAddress(clientMbs,clientNetworkController,"localhost");
        final int serverPort = JmxServerHelper.getNetworkManagerServerSocketPort(serverMbs,serverNetworkManager);
        JmxClientNetworkHelper.changeServerHostPort(clientMbs,clientNetworkController,serverPort);
        long transmitTarget = (long) (30 * TransformValues.MEGABYTE); //30 mega byte transmit target
        JmxClientNetworkHelper.changeTransmitTarget(clientMbs,clientNetworkController,transmitTarget);
        JmxClientNetworkHelper.changeNetworkBufferSize(clientMbs,clientNetworkController,bufferSize);
        JmxClientNetworkHelper.startPlainNetwork(clientMbs,clientNetworkController);
        //check if service is ready
        while(!JmxClientNetworkHelper.getTaskFulfilled(clientMbs,clientNetworkController)){
            ThreadManager.nap(500);
            log.log(Level.INFO,"Task not fulfilled yet.");
        }
        //wait a bit for the server to catch up to this test
        ThreadManager.nap(1000);
        final File testFile = new File(testSpaceDir,testName+".test");
        if(testFile.exists()){
            if(testFile.isFile()){
                if(testFile.length() == transmitTarget){
                    Assert.assertTrue(true);
                }else{
                    log.log(Level.WARNING, "Test file had a different size. Actual Size: " + testFile.length()
                            + " | Desired size: " + transmitTarget);
                    Assert.assertTrue(false);
                }
            }else{
                log.log(Level.WARNING, "Test file exists but is not a file? Did someone create a folder instead?");
                Assert.assertTrue(false);
            }
        }else{
            log.log(Level.WARNING, "Test file does not exist on the given path. Maybe some error in writing?");
            Assert.assertTrue(false);
        }
    }
}
