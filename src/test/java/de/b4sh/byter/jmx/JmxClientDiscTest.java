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
import org.junit.Test;

import de.b4sh.byter.client.Client;
import de.b4sh.byter.support.ComponentHelper;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.data.TransformValues;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.jmx.JmxClientDiscHelper;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.jmx.JmxServerHelper;
import de.b4sh.byter.utils.writer.WriterType;

public final class JmxClientDiscTest {

    private static final Logger log = Logger.getLogger(JmxClientDiscTest.class.getName());
    private static final String JMXSERVERIP = "localhost";
    private static final String testSpaceDir = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "jmx_client-disc";
    private static ExecutorService service;
    private static Client clientObj;

    @BeforeClass
    public static void beforeTest(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDir);
        //starting executorservice
        service = Executors.newFixedThreadPool(1);
        //start server
        clientObj = ComponentHelper.startClient(service);
    }

    @AfterClass
    public static void afterTest(){
        service.shutdown();
        ThreadManager.nap(1000); //wait for the service to shutdown properly
        if(!service.isShutdown()){
            service.shutdownNow(); //still running? killing it not softly it is!
        }
        FileManager.removeAllFilesInDirectory(testSpaceDir);
    }

    @Test
    public void testRoutine() throws IOException {
        //connect to server
        final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,clientObj.getConnectorSystemPort());
        MBeanServerConnection mbs = JmxServerHelper.getMBeanServer(connection);
        Assert.assertNotSame(0,mbs.getMBeanCount());
        ObjectName disc = JmxServerHelper.findObjectName(mbs,"de.b4sh.byter","Disc");
        Assert.assertNotNull(disc);
        //set up env.
        JmxClientDiscHelper.setByteTarget(mbs,disc, (long) (10* TransformValues.MEGABYTE));
        JmxClientDiscHelper.setChunkSize(mbs,disc,64000);
        JmxClientDiscHelper.setFileName(mbs,disc, StringGenerator.nextRandomString(5));
        JmxClientDiscHelper.setOutputPath(mbs,disc,testSpaceDir);
        JmxClientDiscHelper.setWriterBufferSize(mbs,disc,64000);
        JmxClientDiscHelper.setWriterImplementation(mbs,disc, WriterType.BufferedWriter.getKey());
        //start runtime
        JmxClientDiscHelper.invokeDiscTest(mbs,disc);
    }
}
