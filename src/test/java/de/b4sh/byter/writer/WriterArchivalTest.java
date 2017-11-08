package de.b4sh.byter.writer;

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
import de.b4sh.byter.utils.writer.WriterArchival;
import de.b4sh.byter.utils.writer.WriterInterface;

public class WriterArchivalTest {

    private static final Logger log = Logger.getLogger(WriterArchivalTest.class.getName());
    private static String testSpaceDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "writer_archival_test";

    @BeforeClass
    public static void createTestEnvironment(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDirectory);
        //create test dummy file
        TestCaseHelper.createTestCaseFile(testSpaceDirectory,"pregen.file",1000000,10);
    }

    @AfterClass
    public static void cleanTestDirectory(){
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDirectory);
    }

    @Test
    public void testWriterArchival() throws FileNotFoundException {
        //check if the file is created
        final File file = new File(testSpaceDirectory,"pregen.file");
        Assert.assertTrue(file.exists());
        log.log(Level.INFO,"found test file with size: " + file.length());
        log.log(Level.INFO,"now starting with read back and write over archival alike writer");
        final ReaderInterface ri = new ReaderRandomAccessFile(64000,file);
        final File dataFile = new File(testSpaceDirectory, "data.file");
        final File indexFile = new File(testSpaceDirectory, "index.file");
        final WriterInterface wi = new WriterArchival(dataFile,indexFile,8192);
        wi.setAutomaticFileRemoval(false);
        while(ri.hasNextBlock()){
            wi.handleData(ri.readNextBlock());
        }
        ri.finish();
        wi.finish();
        //basic assertion
        Assert.assertTrue(dataFile.exists());
        Assert.assertTrue(indexFile.exists());
        //statistics
        final StringBuilder sb = new StringBuilder(1000).append("\n");
        sb.append("DataFileLength: ").append(dataFile.length()).append("\n");
        sb.append("IndexFileLength: ").append(indexFile.length()).append("\n");
        log.log(Level.INFO,sb.toString());
        //assertions
        Assert.assertEquals(file.length(),dataFile.length());
    }
}
