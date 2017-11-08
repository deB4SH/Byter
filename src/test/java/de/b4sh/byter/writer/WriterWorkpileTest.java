/*
 * File: WriterWorkpileTest
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-09-14
 * Type: Class
 */
package de.b4sh.byter.writer;

import java.io.File;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.b4sh.byter.utils.data.ChunkGenerator;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.writer.WriterType;
import de.b4sh.byter.utils.writer.WriterWorkpile;

public class WriterWorkpileTest {

    private static final Logger log = Logger.getLogger(WriterWorkpileTest.class.getName());
    private static String testSpaceDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "writer_workpile_test";

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
        ThreadManager.nap(1000); //wait a bit for the workpile writer to close up
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDirectory);
    }

    /**
     * Test the WriterBuffered class.
     */
    @Test
    public void testWriterWorkpile(){
        final long dataLength = 1000000L;
        final byte[] chunk = ChunkGenerator.generateChunk(64000);
        final int runs = (int) (dataLength / chunk.length);
        final int edgeSize = (int)(dataLength % chunk.length);
        final byte[] edge = ChunkGenerator.generateChunk(edgeSize);
        final File writeFile = new File(testSpaceDirectory, "workpile.test");
        final WriterWorkpile workpileWriter = new WriterWorkpile(writeFile, WriterType.BufferedWriter,8000);
        workpileWriter.setAutomaticFileRemoval(false);
        for(int i = 0; i < runs; i++){
            workpileWriter.handleData(chunk);
        }
        workpileWriter.handleData(edge);
        workpileWriter.finish();
        ThreadManager.nap(1000); //give the impl. some time to work
        //check filesize
        Assert.assertTrue(writeFile.exists());
        final long resultFileSize = writeFile.length();
        Assert.assertEquals(dataLength,resultFileSize);
    }
}
