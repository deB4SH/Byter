package de.b4sh.byter.utils;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.io.FileManager;

/**
 * Tests related to FileManager.
 * @see de.b4sh.byter.utils.io.FileManager
 */
public final class FileManagerTest {

    private static final Logger log = Logger.getLogger(FileManagerTest.class.getName());
    private static final String testSpaceDir = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "file_manager_test";

    /**
     * Start Method for everything related to this Test-Case.
     */
    @BeforeClass
    public static void startup(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDir);
        //create test-data
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 10; j++){
                final File testFile = new File(testSpaceDir,"test" + i + "-" + j + ".configuration");
                FileManager.createFile(testFile);
            }
        }
        //create unique extensions
        for(int i=0; i < 5; i++){
            final File testFile = new File(testSpaceDir, StringGenerator.nextRandomString(5)+".test"+i);
            FileManager.createFile(testFile);
        }
        //done
    }

    /**
     * Clean up after this test.
     */
    @AfterClass
    public static void endTest(){
        //clean up folder
        FileManager.removeAllFilesInDirectory(testSpaceDir);
    }

    /**
     * Test file listing of all files in path.
     */
    @Test
    public void testFindAllConfiguration(){
        final List<File> fileList = FileManager.getFiles(testSpaceDir);
        Assert.assertNotNull(fileList);
        Assert.assertEquals(35,fileList.size());
    }

    /**
     * Test filtered file listing in path.
     */
    @Test
    public void testFindFiltered(){
        final List<File> fileList = FileManager.getFilesFiltered(testSpaceDir,"0-");
        Assert.assertNotNull(fileList);
        Assert.assertEquals(10,fileList.size());
    }

    /**
     * Test filtered extensions
     */
    @Test
    public void testFindFilteredExtension(){
        final List<File> fileList = FileManager.getFilesFiltered(testSpaceDir,"","test1");
        Assert.assertNotNull(fileList);
        Assert.assertEquals(1,fileList.size());
    }

    @Test
    public void testPathRelativ(){
        final String testPath = "." + File.separator + "test-space" + File.separator + "file_manager_test";
        if(FileManager.isPathRelative(testPath)){
            Assert.assertTrue(true);
        }else{
            log.log(Level.WARNING,"Function isPathRelativ returned falsed on a relative path.");
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testPathTransformation(){
        final String testPath = "." + File.separator + "test-space" + File.separator + "file_manager_test";
        if(FileManager.isPathRelative(testPath)){
            final String absPath = FileManager.transformRelativeToAbsolutPath(testPath);
            Assert.assertEquals(testSpaceDir,absPath);
        }else{
            log.log(Level.WARNING,"Function isPathRelativ returned falsed on a relative path.");
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testPathSelection(){
        final boolean writable = FileManager.isPathWritalbe(testSpaceDir+File.separator+"pathSelc");
        Assert.assertEquals(true,writable);
    }

}
