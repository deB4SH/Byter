package de.b4sh.byter.utils;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.measurements.PerformanceTimer;

/**
 * Test the performanceTimer and its standard evaluators.
 */
public final class PerformanceTimerTest {

    private static final Logger log = Logger.getLogger(PerformanceTimerTest.class.getName());
    private static String testSpaceDirectory = System.getProperty("user.dir") + File.separator + "test-space" + File.separator + "performance_timer_test";

    @BeforeClass
    public static void createTestEnvironment(){
        //set up test-space if not already done
        FileManager.createFolder(testSpaceDirectory);
    }

    @AfterClass
    public static void cleanTestDirectory(){
        //clean up folder
        //FileManager.removeAllFilesInDirectory(testSpaceDirectory);
    }

    /**
     * Test the evaluators and test some values.
     */
    @Test
    public void testEvaluators(){
        final PerformanceTimer timer = new PerformanceTimer(PerformanceTimerTest.class.getName());
        this.addTestMeasurements(timer,10);
        Map<String, Float> map = timer.evaluate();
        Assert.assertNotNull(map);
        Assert.assertEquals(8,map.size());
        //"lowestPackageSize" -> "640000.0"
        Assert.assertEquals(640000,map.get("lowestPackageSize"),0);
        //"highestPackageSize" -> "640000.0"
        Assert.assertEquals(640000,map.get("highestPackageSize"),0);
        //"avgMegaBytesPerSecond" -> "63.999302"
        //accept a delta of 0.05 due to rounding failures or different processing time consumption
        Assert.assertEquals(63.999,map.get("avgMegaBytesPerSecond"),0.05);
        //"quantil25AvgMegaBytesPerSecond" -> "30.475918"
        //accept a delta of 2 due to rounding failures or different processing time consumption
        //or just a slow vm that cant keep up ;)
        Assert.assertEquals(30,map.get("quantil25AvgMegaBytesPerSecond"),2);
    }

    /**
     * This test checks if the list is filled up to measurementVolume and if the tracking is selective.
     * only each 10th measurement is tracked.
     */
     @Test
     public void testSelectiveTracking(){
        final PerformanceTimer timer = new PerformanceTimer(PerformanceTimerTest.class.getName(),100,10);
        this.addTestMeasurements(timer,1100);
        Assert.assertEquals(100,timer.getData().size());
     }

     @Test
     public void testSelectiveTrackingWithoutSteps(){
         final PerformanceTimer timer = new PerformanceTimer(PerformanceTimerTest.class.getName(),100,0);
         this.addTestMeasurements(timer,150);
         Assert.assertEquals(100,timer.getData().size());
     }

     @Test
     public void testDataExport(){
         final PerformanceTimer timer = new PerformanceTimer(PerformanceTimerTest.class.getName(),500,10);
         this.addTestMeasurements(timer,1000);
         final File outFile = new File(testSpaceDirectory, "dataDump.csv");
         timer.writeMeasurementDataToFile(outFile);

     }

     @Test
     public void testTrackingAtTwoHundretFiftyGB(){
         final long target = 250000000000L;
         final int bufferSize = 16000000;
         final int measurementTarget = 2500;
         final int skipped = PerformanceTimer.calculateSkippedMeasurements(target,measurementTarget,bufferSize);
         final int runsToFulFill = (int) (target / bufferSize);
         final PerformanceTimer pt = new PerformanceTimer("PerformanceTimer Test Junit",measurementTarget,skipped);
         this.addTestMeasurements(pt,runsToFulFill);
         Assert.assertEquals(2500,pt.getData().size());
     }


    private void addTestMeasurements(final PerformanceTimer timer, final int count){
        for(int i = 0; i < count; i++)
            timer.addNewMeasurement(System.nanoTime(), System.nanoTime()+10000000,640000);
    }
}
