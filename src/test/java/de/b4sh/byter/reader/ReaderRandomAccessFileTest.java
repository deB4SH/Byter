package de.b4sh.byter.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.b4sh.byter.support.TestCaseHelper;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.reader.ReaderInterface;
import de.b4sh.byter.utils.reader.ReaderRandomAccessFile;

public class ReaderRandomAccessFileTest {

    private static final Logger log = Logger.getLogger(ReaderRandomAccessFileTest.class.getName());
    private static String testSpaceDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "reader_raf_test";

    @BeforeClass
    public static void createTestEnvironment(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDirectory);
        TestCaseHelper.createTestCaseFile(testSpaceDirectory,"pregen.file",10000,100);
        TestCaseHelper.createTestCaseFile(testSpaceDirectory,"pregenTwo.file",15030,100);
        TestCaseHelper.createTestCaseFile(testSpaceDirectory,"pregenThree.file",1337,123);
    }

    @AfterClass
    public static void cleanTestDirectory(){
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDirectory);
    }

    @Test
    public void testOneReaderRAF() throws FileNotFoundException {
        final File file = new File(testSpaceDirectory, "pregen.file");
        final ReaderInterface ri = new ReaderRandomAccessFile(10000,file);
        //blockcounter that checks if there are the correct amount of blocks read
        int blockCounter = 0;
        while(ri.hasNextBlock()){
            ri.readNextBlock();
            blockCounter++;
        }
        ri.finish();
        log.log(Level.INFO,"Block read: " + blockCounter);
        Assert.assertEquals(100,blockCounter);
    }

    @Test
    public void testTwoReaderRAF() throws FileNotFoundException {
        final File file = new File(testSpaceDirectory, "pregenTwo.file");
        final ReaderInterface ri = new ReaderRandomAccessFile(10000,file);
        //blockcounter that checks if there are the correct amount of blocks read
        int blockCounter = 0;
        while(ri.hasNextBlock()){
            ri.readNextBlock();
            blockCounter++;
        }
        ri.finish();
        log.log(Level.INFO,"Block read: " + blockCounter);
        Assert.assertEquals(151,blockCounter);
    }

    @Test
    public void testThreeReaderRAF() throws FileNotFoundException {
        final File file = new File(testSpaceDirectory, "pregenThree.file");
        final ReaderInterface ri = new ReaderRandomAccessFile(10000,file);
        //blockcounter that checks if there are the correct amount of blocks read
        int blockCounter = 0;
        while(ri.hasNextBlock()){
            ri.readNextBlock();
            blockCounter++;
        }
        ri.finish();
        log.log(Level.INFO,"Block read: " + blockCounter);
        Assert.assertEquals(17,blockCounter);
    }

}
