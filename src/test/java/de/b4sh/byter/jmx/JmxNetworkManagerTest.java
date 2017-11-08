package de.b4sh.byter.jmx;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.beust.jcommander.JCommander;
import de.b4sh.byter.CliParameter;
import de.b4sh.byter.server.Server;
import de.b4sh.byter.server.network.NetworkType;
import de.b4sh.byter.support.ComponentHelper;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.jmx.JmxServerHelper;
import de.b4sh.byter.utils.writer.WriterType;

/**
 * Tests for the NetworkManager.
 * This test aims to test everything on the network manager jmx component.
 */
public final class JmxNetworkManagerTest {
    private static final Logger log = Logger.getLogger(JmxNetworkManagerTest.class.getName());
    private static final String JMXSERVERIP = "localhost";
    private static final String testSpaceDir = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "test_network_manager";
    private static ExecutorService service;
    private static Server serverObj;
    private static JCommander jcommander;

    /**
     * Start Method for everything related to this Test-Case.
     */
    @BeforeClass
    public static void startup(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDir);
        //build arguments
        final String[] argv = new String[]{"-s","server"};
        final CliParameter params = new CliParameter();
        jcommander = new JCommander();
        JCommander.newBuilder().addObject(params)
                .build()
                .parse(argv);
        //build service
        service = Executors.newFixedThreadPool(1);
        serverObj = ComponentHelper.startServer(service);
        log.log(Level.INFO, "JmxNetworkManagerTest running on port: " + serverObj.getConnectorSystemPort());
    }

    @AfterClass
    public static void endTest() throws IOException {
        service.shutdown();
        ThreadManager.nap(1000); //wait for the service to shutdown properly
        if(!service.isShutdown()){
            service.shutdownNow(); //still running? killing it not softly it is!
        }
        ThreadManager.nap(1000);
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDir);
    }

    /**
     * Shutdown everything related after tests are done.
     */
    @AfterClass
    public static void shutdown(){
        service.shutdown();
    }

    /**
     * Dummy-Test to see if this test is running!
     */
    @Test
    public void testSomething(){
        log.log(Level.INFO, "JmxNetworkManager test is running!");
        Assert.assertNotEquals(true,false);
    }

    /**
     * Test if it is possible to connect to the jmx component of the server
     */
    @Test
    public void testConnectionToJmx(){
        if(serverObj.getConnectorSystemPort() > 0){
            log.log(Level.FINE,"Found service port. Now trying to connect.");
            //build up a jmx connection
            try {
                final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
                log.log(Level.INFO, "Connection ID: " + connection.getConnectionId());
                Assert.assertNotNull(connection);
            } catch (IOException e) {
                log.log(Level.WARNING,"IO EXCEPTION during connection test!");
                Assert.assertNotNull(null); //let the test fail!
            }
        }
        log.log(Level.FINEST, "DONE: testConnectionToJmx");
    }

    @Test
    public void testMBeanServer() throws IOException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
        //do test
        MBeanServerConnection mbsConnection = JmxServerHelper.getMBeanServer(connection);
        Assert.assertNotNull(mbsConnection);
        if(mbsConnection != null){
            log.log(Level.INFO,"Registered MBeanCount on MBS: " + mbsConnection.getMBeanCount());
        }
        log.log(Level.FINEST, "DONE: testMBeanServer");
    }

    /**
     * Test if there are Controller and NetworkManager registered to MBeanServer
     */
    @Test
    public void testRegisteredObjects() throws IOException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
        //get MBeanServerConnection
        MBeanServerConnection mbsConnection = JmxServerHelper.getMBeanServer(connection);
        //do test
        Assert.assertNotSame(0,mbsConnection.getMBeanCount()); //check if there are beans registered
        //look for all standard mbeans
        List<ObjectName> standardMbeans = JmxServerHelper.findObjectNames(mbsConnection,"de.b4sh.byter");
        Assert.assertNotEquals(0,standardMbeans.size());
        //see if there is a network manager registered to
        ObjectName networkManager = null;
        for(ObjectName on: standardMbeans){
            if(on.getCanonicalName().contains("NetworkManager")){
                networkManager = on;
            }
        }
        Assert.assertNotNull(networkManager);
    }

    /**
     * Test if the NetworkManager announces the standard fields
     * - NetworkType
     * - StorageType
     * - WriterType
     * - WriterBuffer
     * - NetworkBuffer
     * All should be None on a fresh instance.
     * @throws IOException io exption during test
     */
    @Test
    public void testReadNetworkManagerStockAttributeData() throws IOException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
        //get MBeanServerConnection
        MBeanServerConnection mbsConnection = JmxServerHelper.getMBeanServer(connection);
        //check if mbeans are registered
        Assert.assertNotSame(0,mbsConnection.getMBeanCount());
        //do actual test
        ObjectName networkManagerOn = JmxServerHelper.findObjectName(mbsConnection,"de.b4sh.byter","NetworkManager");
        //check NetworkBufferSize
        int networkBufferSize = JmxServerHelper.getNetworkManagerNetworkBufferSize(mbsConnection,networkManagerOn);
        Assert.assertEquals(8192,networkBufferSize);
        //check WriterBufferSize
        int writerBufferSize =  JmxServerHelper.getNetworkManagerWriterBufferSize(mbsConnection,networkManagerOn);
        Assert.assertEquals(8192,writerBufferSize);
        //check NetworkType
        String networkType = JmxServerHelper.getNetworkManagerNetworkType(mbsConnection,networkManagerOn);
        Assert.assertEquals("None",networkType);
        //check StorageType
        String storageType = JmxServerHelper.getNetworkManagerStorageType(mbsConnection,networkManagerOn);
        Assert.assertEquals("None",storageType);
        //check WriterType
        String writerType = JmxServerHelper.getNetworkManagerWriterType(mbsConnection,networkManagerOn);
        Assert.assertEquals("None",writerType);
        //check basic filepath - working directory
        String filePath = JmxServerHelper.getNetworkManagerFilePath(mbsConnection,networkManagerOn);
        Assert.assertEquals(System.getProperty("user.dir"),filePath);
        //server socket port
        int serverSocketPort = JmxServerHelper.getNetworkManagerServerSocketPort(mbsConnection,networkManagerOn);
        Assert.assertEquals(-1,serverSocketPort);
    }

    /**
     * Test if there are all functions available.
     */
    @Test
    public void testStockAvailableOperations() throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
        //get MBeanServerConnection
        MBeanServerConnection mbsConnection = JmxServerHelper.getMBeanServer(connection);
        //check if mbeans are registered
        Assert.assertNotSame(0,mbsConnection.getMBeanCount());
        //do actual test
        ObjectName networkManagerOn = JmxServerHelper.findObjectName(mbsConnection,"de.b4sh.byter","NetworkManager");
        final List<MBeanOperationInfo> functions = JmxServerHelper.getOperations(mbsConnection,networkManagerOn);
        Assert.assertNotEquals(0, functions.size());
    }

    /**
     * Tests if buffer sizes are changeable via invoke function.
     * @throws IOException IOException
     * @throws IntrospectionException IntrospectionException
     * @throws InstanceNotFoundException InstanceNotFoundException
     * @throws ReflectionException ReflectionException
     */
    @Test
    public void testInvokeChangeBufferSize() throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
        //get MBeanServerConnection
        MBeanServerConnection mbsConnection = JmxServerHelper.getMBeanServer(connection);
        //check if mbeans are registered
        Assert.assertNotSame(0,mbsConnection.getMBeanCount());
        //do actual test
        ObjectName networkManagerOn = JmxServerHelper.findObjectName(mbsConnection,"de.b4sh.byter","NetworkManager");
        //change writer buffer size
        int networkBufferSizeOLD = JmxServerHelper.getNetworkManagerNetworkBufferSize(mbsConnection,networkManagerOn);
        int writerBufferSizeOLD =  JmxServerHelper.getNetworkManagerWriterBufferSize(mbsConnection,networkManagerOn);
        JmxServerHelper.setNetworkManagerNetworkBufferSize(mbsConnection,networkManagerOn,1337);
        JmxServerHelper.setNetworkManagerWriterBufferSize(mbsConnection,networkManagerOn,1337);
        int networkBufferSize = JmxServerHelper.getNetworkManagerNetworkBufferSize(mbsConnection,networkManagerOn);
        int writerBufferSize =  JmxServerHelper.getNetworkManagerWriterBufferSize(mbsConnection,networkManagerOn);
        Assert.assertNotEquals(networkBufferSize,networkBufferSizeOLD);
        Assert.assertNotEquals(writerBufferSize,writerBufferSizeOLD);
        Assert.assertEquals(networkBufferSize,1337);
        Assert.assertEquals(writerBufferSize,1337);
    }

    /**
     * Test change functions on server.
     * @throws IOException IOException
     * @throws IntrospectionException IntrospectionException
     * @throws InstanceNotFoundException InstanceNotFoundException
     * @throws ReflectionException ReflectionException
     */
    @Test
    public void testInvokeChangeType() throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
        //get MBeanServerConnection
        MBeanServerConnection mbsConnection = JmxServerHelper.getMBeanServer(connection);
        //check if mbeans are registered
        Assert.assertNotSame(0,mbsConnection.getMBeanCount());
        //get networkManager
        ObjectName networkManagerOn = JmxServerHelper.findObjectName(mbsConnection,"de.b4sh.byter","NetworkManager");
        //list functions
        final List<MBeanOperationInfo> functions = JmxServerHelper.getOperations(mbsConnection,networkManagerOn);
        //do actual test for network
        String networkTypeOld = JmxServerHelper.getNetworkManagerNetworkType(mbsConnection,networkManagerOn);
        String networkResponse = JmxServerHelper.setNetworkManagerNetworkType(mbsConnection,networkManagerOn, NetworkType.BufferedNetwork.getKey());
        Assert.assertNotEquals("",networkResponse);
        log.log(Level.INFO, "TEST: Change Response:" + networkResponse);
        String networkType = JmxServerHelper.getNetworkManagerNetworkType(mbsConnection,networkManagerOn);
        Assert.assertNotEquals(networkType,networkTypeOld);
        //do actual test for writer
        String writerTypeOld = JmxServerHelper.getNetworkManagerWriterType(mbsConnection,networkManagerOn);
        String writerResponse = JmxServerHelper.setNetworkManagerWriterType(mbsConnection,networkManagerOn, WriterType.BufferedWriter.getKey());
        Assert.assertNotEquals("",writerResponse);
        log.log(Level.INFO, "TEST: Change Response:" + writerResponse);
        String writerType = JmxServerHelper.getNetworkManagerWriterType(mbsConnection,networkManagerOn);
        Assert.assertNotEquals(writerType,writerTypeOld);
    }

    @Test
    public void changeFilePathTest() throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
        //get MBeanServerConnection
        MBeanServerConnection mbsConnection = JmxServerHelper.getMBeanServer(connection);
        //check if mbeans are registered
        Assert.assertNotSame(0,mbsConnection.getMBeanCount());
        //get networkManager
        ObjectName networkManagerOn = JmxServerHelper.findObjectName(mbsConnection,"de.b4sh.byter","NetworkManager");
        //list functions
        final List<MBeanOperationInfo> functions = JmxServerHelper.getOperations(mbsConnection,networkManagerOn);
        //do actual test for network
        String filePath = JmxServerHelper.getNetworkManagerFilePath(mbsConnection,networkManagerOn);
        Assert.assertEquals(System.getProperty("user.dir"),filePath);
        //set up test-space if not already done
        String responseChangeOne = JmxServerHelper.setNetworkManagerFilePath(mbsConnection,networkManagerOn,testSpaceDir);
        log.log(Level.INFO,"Reponse-Print: " + responseChangeOne);
        //check if the dir is changed to an existing one
        String filePathChangeOne = JmxServerHelper.getNetworkManagerFilePath(mbsConnection,networkManagerOn);
        Assert.assertEquals(testSpaceDir,filePathChangeOne);
        //change dir inside test space and create an new one
        final String additionDirTest = testSpaceDir + File.separator + StringGenerator.nextRandomString(5);
        String responseChangeTwo = JmxServerHelper.setNetworkManagerFilePathAndCreate(mbsConnection,networkManagerOn,additionDirTest);
        log.log(Level.INFO,"Response-Print: " + responseChangeTwo);
        String filePathChangeTwo = JmxServerHelper.getNetworkManagerFilePath(mbsConnection,networkManagerOn);
        Assert.assertEquals(additionDirTest,filePathChangeTwo);
    }
}