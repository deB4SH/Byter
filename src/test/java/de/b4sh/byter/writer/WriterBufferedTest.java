package de.b4sh.byter.writer;

import java.io.File;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.b4sh.byter.utils.data.ChunkGenerator;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.measurements.PerformanceTimer;
import de.b4sh.byter.utils.writer.WriterBuffered;
import de.b4sh.byter.utils.writer.WriterInterface;

/**
 * Testclass for the buffered writer.
 */
public class WriterBufferedTest {

    private static final Logger log = Logger.getLogger(WriterBufferedTest.class.getName());
    private static String testSpaceDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "writer_buffered_test";

    /**
     * Initialisation of the test environment.
     */
    @BeforeClass
    public static void createTestEnvironment(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDirectory);
    }

    /**
     * Clean up after test.
     */
    @AfterClass
    public static void cleanTestDirectory(){
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDirectory);
    }

    /**
     * Test the WriterBuffered class.
     */
    @Test
    public void testWriterBuffered(){
        final File file = new File(testSpaceDirectory, "buffered.txt");
        final byte[] byteArray = ChunkGenerator.generateChunk(10000);
        final WriterInterface wi = new WriterBuffered(8192,file);
        wi.setAutomaticFileRemoval(false);
        for(int i = 0; i < 10; i++){
            wi.handleData(byteArray);
        }
        wi.finish();
        //check if the file is there and has the correct size
        Assert.assertTrue(file.exists());
        Assert.assertEquals(100000,file.length());
    }

    @Test
    public void testWriterBufferedWithPerformance(){
        final File file = new File(testSpaceDirectory, "bufferedPerformance.txt");
        final byte[] byteArray = ChunkGenerator.generateChunk(10000);
        final PerformanceTimer pt = new PerformanceTimer("JunitTest");
        final WriterInterface wi = new WriterBuffered(8192,file,pt);
        wi.setAutomaticFileRemoval(false);
        for(int i = 0; i < 10; i++){
            wi.handleData(byteArray);
        }
        wi.finish();
        //check if the file is there and has the correct size
        Assert.assertTrue(file.exists());
        Assert.assertEquals(100000,file.length());
    }

}
