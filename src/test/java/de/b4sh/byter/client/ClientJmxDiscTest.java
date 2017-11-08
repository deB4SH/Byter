/*
 * File: ClientJmxDiscTest
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-04
 * Type: Class
 */
package de.b4sh.byter.client;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.b4sh.byter.support.ComponentHelper;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.data.TransformValues;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.jmx.JmxClientDiscHelper;
import de.b4sh.byter.utils.jmx.JmxClientNetworkHelper;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.writer.WriterBuffered;
import de.b4sh.byter.utils.writer.WriterInterface;
import de.b4sh.byter.utils.writer.WriterType;

/**
 * Tests related to the ClientJmxDisc class and interface
 */
public class ClientJmxDiscTest {

    private static String testSpaceDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "clientjmxdisc";
    private static final String JMXSERVERIP = "localhost";
    private static ExecutorService service;
    private static Client clientObj;

    @BeforeClass
    public static void startTestEnvironment(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDirectory);
        service = Executors.newFixedThreadPool(1);
        clientObj = ComponentHelper.startClient(service);
    }

    @AfterClass
    public static void shutdownTestEnvironment(){
        //shutdown jmx and service
        ComponentHelper.shutdownService(service);
        //clean up folder
        //FileManager.removeAllFilesInDirectory(testSpaceDirectory);
    }

    @Test
    public void testSingleWriter() throws IOException {
        final JMXConnector connection = getConnection();
        final MBeanServerConnection mbs = getMbeanServer(connection);
        final ObjectName writerObject = getWriterObjectName(mbs);
        final String fileName = StringGenerator.nextRandomString(5);
        final File file = new File(testSpaceDirectory, fileName);
        //set parameter to service
        JmxClientDiscHelper.setByteTarget(mbs,writerObject, (long) (10* TransformValues.MEGABYTE));
        JmxClientDiscHelper.setChunkSize(mbs,writerObject,64000);
        JmxClientDiscHelper.setFileName(mbs,writerObject, fileName);
        JmxClientDiscHelper.setOutputPath(mbs,writerObject,testSpaceDirectory);
        JmxClientDiscHelper.setWriterBufferSize(mbs,writerObject,64000);
        JmxClientDiscHelper.setWriterImplementation(mbs,writerObject, WriterType.BufferedWriter.getKey());
        //set run parameter

        //start a single writer
        JmxClientDiscHelper.startWriter(mbs,writerObject);
    }

    /**
     * Test the init writer function
     */
    @Test
    public void testInitWriterFunction(){
        final  ClientJmxDisc clientJmxDisc = new ClientJmxDisc("de.test","test");
        final WriterType testType = WriterType.BufferedWriter;
        final File testFile = new File(testSpaceDirectory,"just_a_random.file");
        final WriterInterface wi = clientJmxDisc.initWriter(testType,testFile,"nase");
        Assert.assertTrue(wi instanceof WriterBuffered);
    }

    private static JMXConnector getConnection() throws IOException {
        return JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,clientObj.getConnectorSystemPort());
    }

    private static MBeanServerConnection getMbeanServer(final JMXConnector clientConnection){
        return JmxClientNetworkHelper.getMBeanServer(clientConnection);
    }

    private static ObjectName getWriterObjectName(final MBeanServerConnection mbs){
        return JmxClientNetworkHelper.findObjectName(mbs,"de.b4sh.byter","Disc");
    }
}

