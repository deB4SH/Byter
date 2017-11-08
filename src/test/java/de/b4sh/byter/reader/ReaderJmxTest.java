/*
 * File: ReaderJmxTest
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-09-27
 * Type: Class
 */
package de.b4sh.byter.reader;

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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.b4sh.byter.client.Client;
import de.b4sh.byter.support.ComponentHelper;
import de.b4sh.byter.support.TestCaseHelper;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.jmx.JmxClientNetworkHelper;
import de.b4sh.byter.utils.jmx.JmxClientReaderHelper;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.reader.ReaderType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReaderJmxTest {

    private static final Logger log = Logger.getLogger(ReaderJmxTest.class.getName());
    private static final String testSpaceDir = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "reader_jmx_test";
    private static final String JMXSERVERIP = "localhost";
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
        clientObj = ComponentHelper.startClient(service);
    }

    @AfterClass
    public static void endTest(){
        service.shutdown();
        ThreadManager.nap(1000); //wait for the service to shutdown properly
        if(!service.isShutdown()){
            service.shutdownNow(); //still running? killing it not softly it is!
        }
        ThreadManager.nap(1000);
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDir);
    }

    private static JMXConnector getConnection() throws IOException {
        return JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,clientObj.getConnectorSystemPort());
    }

    private static MBeanServerConnection getMbeanServer(final JMXConnector clientConnection){
        return JmxClientNetworkHelper.getMBeanServer(clientConnection);
    }

    private static ObjectName getReaderObjectName(final MBeanServerConnection mbs){
        return JmxClientNetworkHelper.findObjectName(mbs,"de.b4sh.byter","Reader");
    }

    /**
     * Test all base variables of the reader jmx interface
     */
    @Test
    public void t1testStandardVariableOverJMX() throws IOException {
        final JMXConnector connection = getConnection();
        final MBeanServerConnection mbs = getMbeanServer(connection);
        final ObjectName readerObject = getReaderObjectName(mbs);
        //assert path
        final String path = JmxClientReaderHelper.getFilePath(mbs,readerObject);
        Assert.assertEquals(System.getProperty("user.dir"), path);
        //assert filename
        final String filename = JmxClientReaderHelper.getFileName(mbs,readerObject);
        Assert.assertEquals("none", filename);
        //assert chunksize
        final int chunkSize = JmxClientReaderHelper.getChunkSize(mbs,readerObject);
        Assert.assertEquals(8192, chunkSize);
        //assert takeMeasurements
        final String takeMeasurements = JmxClientReaderHelper.getTakeMeasurements(mbs,readerObject);
        final boolean takeMeasurementsAsBoolean = JmxClientReaderHelper.getTakeMeasurementsAsBoolean(mbs,readerObject);
        Assert.assertEquals(true, takeMeasurementsAsBoolean);
        Assert.assertEquals("true", takeMeasurements);
        //assert measurementvolume
        final int measurementVolume = JmxClientReaderHelper.getMeasurementVolume(mbs,readerObject);
        Assert.assertEquals(-1,measurementVolume);
        //assert readertype
        final String readerType = JmxClientReaderHelper.getReaderType(mbs,readerObject);
        final ReaderType readerTypeAsEnum = JmxClientReaderHelper.getReaderTypeAsType(mbs,readerObject);
        Assert.assertEquals(ReaderType.none, readerTypeAsEnum);
        Assert.assertEquals("none", readerType);
    }

    @Test
    public void t2testChangeVariableOverJMX() throws IOException {
        final JMXConnector connection = getConnection();
        final MBeanServerConnection mbs = getMbeanServer(connection);
        final ObjectName readerObject = getReaderObjectName(mbs);
        //change path to test-path
        final String pathOld = JmxClientReaderHelper.getFilePath(mbs,readerObject);
        JmxClientReaderHelper.setFilePath(mbs,readerObject,testSpaceDir);
        final String pathNew = JmxClientReaderHelper.getFilePath(mbs,readerObject);
        Assert.assertEquals(testSpaceDir,pathNew);
        Assert.assertNotEquals(pathOld,pathNew);
        //change fileName
        final String filenameOld = JmxClientReaderHelper.getFileName(mbs,readerObject);
        JmxClientReaderHelper.setFileName(mbs,readerObject,"test-case-name");
        final String filenameNew = JmxClientReaderHelper.getFileName(mbs,readerObject);
        Assert.assertNotEquals(filenameOld,filenameNew);
        Assert.assertEquals("test-case-name",filenameNew);
        //change chunksize
        final int chunkSizeOld = JmxClientReaderHelper.getChunkSize(mbs,readerObject);
        JmxClientReaderHelper.setChunkSize(mbs,readerObject,16000);
        final int chunkSizeNew = JmxClientReaderHelper.getChunkSize(mbs,readerObject);
        Assert.assertEquals(16000,chunkSizeNew);
        Assert.assertNotEquals(chunkSizeOld,chunkSizeNew);
        //change takeMeasurements
        final String takeMeasurementsOld = JmxClientReaderHelper.getTakeMeasurements(mbs,readerObject);
        JmxClientReaderHelper.setTakeMeasurements(mbs,readerObject,false);
        final String takeMeasurementsNew = JmxClientReaderHelper.getTakeMeasurements(mbs,readerObject);
        Assert.assertNotEquals(takeMeasurementsOld,takeMeasurementsNew);
        Assert.assertEquals("false",takeMeasurementsNew);
        //change measurementVolume
        final int measurementVolumeOld = JmxClientReaderHelper.getMeasurementVolume(mbs,readerObject);
        JmxClientReaderHelper.setMeasurementVolume(mbs,readerObject,10);
        final int measurementVolumeNew = JmxClientReaderHelper.getMeasurementVolume(mbs,readerObject);
        Assert.assertNotEquals(measurementVolumeOld,measurementVolumeNew);
        Assert.assertEquals(10,measurementVolumeNew);
        //change readertype
        final String readerTypeOld = JmxClientReaderHelper.getReaderType(mbs,readerObject);
        JmxClientReaderHelper.setReaderType(mbs,readerObject,ReaderType.rafr.getType());
        final String readerTypeNew = JmxClientReaderHelper.getReaderType(mbs,readerObject);
        Assert.assertEquals(ReaderType.rafr.getType(),readerTypeNew);
        Assert.assertNotEquals(readerTypeOld,readerTypeNew);
    }

    @Test
    public void t3testStartSingleReader() throws IOException {
        //create a test case file
        TestCaseHelper.createTestCaseFile(testSpaceDir,"test-file.test",10000,100);
        //connect to client
        final JMXConnector connection = getConnection();
        final MBeanServerConnection mbs = getMbeanServer(connection);
        final ObjectName readerObject = getReaderObjectName(mbs);
        //set params
        JmxClientReaderHelper.setFilePath(mbs,readerObject,testSpaceDir);
        JmxClientReaderHelper.setFileName(mbs,readerObject,"test-file.test");
        JmxClientReaderHelper.setChunkSize(mbs,readerObject,16000);
        JmxClientReaderHelper.setTakeMeasurements(mbs,readerObject,false);
        JmxClientReaderHelper.setMeasurementVolume(mbs,readerObject,-1);
        JmxClientReaderHelper.setReaderType(mbs,readerObject,ReaderType.rafr.getType());
        JmxClientReaderHelper.startSingleReader(mbs,readerObject);
        ThreadManager.nap(2500);//let the reader read a bit :)
        Assert.assertEquals(1,JmxClientReaderHelper.getReaderPoolSize(mbs,readerObject));
        final String response = JmxClientReaderHelper.stopServicePool(mbs,readerObject);
        log.log(Level.INFO, response);
    }

    @Test
    public void t4testStartMultipleReader() throws IOException {
        //create a test case file
        TestCaseHelper.createTestCaseFile(testSpaceDir,"test-multi-file.test",10000,100);
        //connect to client
        final JMXConnector connection = getConnection();
        final MBeanServerConnection mbs = getMbeanServer(connection);
        final ObjectName readerObject = getReaderObjectName(mbs);
        //set params
        JmxClientReaderHelper.setFilePath(mbs,readerObject,testSpaceDir);
        JmxClientReaderHelper.setFileName(mbs,readerObject,"test-multi-file.test");
        JmxClientReaderHelper.setChunkSize(mbs,readerObject,16000);
        JmxClientReaderHelper.setTakeMeasurements(mbs,readerObject,false);
        JmxClientReaderHelper.setMeasurementVolume(mbs,readerObject,-1);
        JmxClientReaderHelper.setReaderType(mbs,readerObject,ReaderType.rafr.getType());
        JmxClientReaderHelper.startMultipleReader(mbs,readerObject,5);
        ThreadManager.nap(2500);//let the reader read a bit :)
        Assert.assertEquals(5,JmxClientReaderHelper.getReaderPoolSize(mbs,readerObject));
        final String response = JmxClientReaderHelper.stopServicePool(mbs,readerObject);
        log.log(Level.INFO, response);
    }
}