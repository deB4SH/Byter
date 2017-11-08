package de.b4sh.byter.jmx;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.b4sh.byter.client.Client;
import de.b4sh.byter.integration.InteractionTest;
import de.b4sh.byter.support.ComponentHelper;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.jmx.JmxClientNetworkHelper;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.jmx.JmxServerHelper;

/**
 * Test for Client Network JMX Component.
 * This test aims to test everything set-able at the Client Network JMX Component.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class JmxClientNetworkTest {

    private static final Logger log = Logger.getLogger(InteractionTest.class.getName());
    private static final String JMXSERVERIP = "localhost";
    private static final String testSpaceDir = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "jmx_client-network";
    private static ExecutorService service;
    private static Client clientObj;

    /**
     * Start Method for everything related to this Test-Case.
     */
    @BeforeClass
    public static void startup(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDir);
        //starting executorservice
        service = Executors.newFixedThreadPool(1);
        //start server
        clientObj = ComponentHelper.startClient(service);
    }

    @AfterClass
    public static void cleanUpAfterTest(){
        FileManager.removeAllFilesInDirectory(testSpaceDir);
    }

    /**
     * read standard attributes at first!
     * @throws IOException ioe
     */
    @Test
    public void t1testClientNetworkStandardAttributes() throws IOException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,clientObj.getConnectorSystemPort());
        MBeanServerConnection mbs = JmxServerHelper.getMBeanServer(connection);
        Assert.assertNotSame(0,mbs.getMBeanCount());
        ObjectName network = JmxServerHelper.findObjectName(mbs,"de.b4sh.byter","Network");
        Assert.assertNotNull(network);
        //assert the attributes
        final boolean executorStatus = JmxClientNetworkHelper.getExecutorServiceStatus(mbs,network);
        Assert.assertEquals(false,executorStatus);
        final int networkBufferSize = JmxClientNetworkHelper.getNetworkBufferSize(mbs,network);
        Assert.assertEquals(8192,networkBufferSize);
        final String hostaddress = JmxClientNetworkHelper.getServerHostAddress(mbs,network);
        Assert.assertEquals("localhost",hostaddress);
        final int hostport = JmxClientNetworkHelper.getServerHostPort(mbs,network);
        Assert.assertEquals(0,hostport);
        final int targetPregenChunkSize = JmxClientNetworkHelper.getTargetPregenChunkSize(mbs,network);
        Assert.assertEquals(8192,targetPregenChunkSize);
        final long transmitTarget = JmxClientNetworkHelper.getTransmitTarget(mbs,network);
        Assert.assertEquals(8192,transmitTarget);
    }

    /**
     * change attributes and read them back afterwards.
     * @throws IOException ioe
     */
    @Test
    public void t2testClientNetworkChangeAttributes() throws IOException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,clientObj.getConnectorSystemPort());
        MBeanServerConnection mbs = JmxServerHelper.getMBeanServer(connection);
        Assert.assertNotSame(0,mbs.getMBeanCount());
        ObjectName network = JmxServerHelper.findObjectName(mbs,"de.b4sh.byter","Network");
        Assert.assertNotNull(network);
        //test things here
        final int networkBufferSizeOld = JmxClientNetworkHelper.getNetworkBufferSize(mbs,network);
        JmxClientNetworkHelper.changeNetworkBufferSize(mbs,network,12345);
        final int networkBufferSizeNew = JmxClientNetworkHelper.getNetworkBufferSize(mbs,network);
        Assert.assertNotEquals(networkBufferSizeNew,networkBufferSizeOld);
        Assert.assertEquals(networkBufferSizeNew,12345);

        final String hostaddressOld = JmxClientNetworkHelper.getServerHostAddress(mbs,network);
        JmxClientNetworkHelper.changeServerHostAddress(mbs,network,"127.0.0.1");
        final String hostaddressNew = JmxClientNetworkHelper.getServerHostAddress(mbs,network);
        Assert.assertNotEquals(hostaddressNew,hostaddressOld);
        Assert.assertEquals(hostaddressNew,"127.0.0.1");

        final int hostportOld = JmxClientNetworkHelper.getServerHostPort(mbs,network);
        JmxClientNetworkHelper.changeServerHostPort(mbs,network,80);
        final int hostportNew = JmxClientNetworkHelper.getServerHostPort(mbs,network);
        Assert.assertNotEquals(hostportNew,hostportOld);
        Assert.assertEquals(80,hostportNew);

        final int targetPregenChunkSizeOld = JmxClientNetworkHelper.getTargetPregenChunkSize(mbs,network);
        JmxClientNetworkHelper.changePregenChunkSize(mbs,network,12345);
        final int targetPregenChunkSizeNew = JmxClientNetworkHelper.getTargetPregenChunkSize(mbs,network);
        Assert.assertNotEquals(targetPregenChunkSizeOld,targetPregenChunkSizeNew);
        Assert.assertEquals(12345,targetPregenChunkSizeNew);

        final long transmitTargetOld = JmxClientNetworkHelper.getTransmitTarget(mbs,network);
        JmxClientNetworkHelper.changeTransmitTarget(mbs,network,12345);
        final long transmitTargetNew = JmxClientNetworkHelper.getTransmitTarget(mbs,network);
        Assert.assertNotEquals(transmitTargetNew,transmitTargetOld);
        Assert.assertEquals(transmitTargetNew,12345);
    }

    @AfterClass
    public static void endTest(){
        service.shutdown();
        ThreadManager.nap(1000); //wait for the service to shutdown properly
        if(!service.isShutdown()){
            service.shutdownNow(); //still running? killing it not softly it is!
        }
        ThreadManager.nap(1000);
    }
}
