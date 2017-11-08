/*
 * File: CalculateSkippedStepsTest
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-09-14
 * Type: Class
 */
package de.b4sh.byter.learningByDoing;

import org.junit.Assert;
import org.junit.Test;

import de.b4sh.byter.utils.measurements.PerformanceTimer;

public class CalculateSkippedStepsTest {

    /**
     * Checks if 2 is calculated by given parameters.
     * Expected 2!
     */
    @Test
    public void testCalculation(){
        final int bufferSize = 10000000;
        final long byteTarget = 50000000000L;
        final int measurementVolume = 2500;
        final int skipped = PerformanceTimer.calculateSkippedMeasurements(byteTarget,measurementVolume,bufferSize);
        Assert.assertEquals(2,skipped);
    }

    /**
     * Checks if 0 is calculated if all can be tracked.
     * Expected 0!
     */
    @Test
    public void testCalculationDirect133Alike(){
        final int bufferSize = 64000;
        final long byteTarget = 100000000;
        final int measurementVolume = 2500;
        final int skipped = PerformanceTimer.calculateSkippedMeasurements(byteTarget,measurementVolume,bufferSize);
        Assert.assertEquals(0,skipped);
    }

    /**
     * Checks if 0 is calculated if all can be tracked.
     * Expected 12500!
     */
    @Test
    public void testCalculation250GBAt8KB(){
        final int bufferSize = 8000;
        final long byteTarget = 250000000000L;
        final int measurementVolume = 2500;
        final int skipped = PerformanceTimer.calculateSkippedMeasurements(byteTarget,measurementVolume,bufferSize);
        Assert.assertEquals(12500,skipped);
    }

    /**
     * Checks if 0 is calculated if all can be tracked.
     * Expected 0!
     */
    @Test
    public void testCalculation250GBAt16MB(){
        final int bufferSize =  16000000;
        final long byteTarget = 250000000000L;
        final int measurementVolume = 2500;
        final int skipped = PerformanceTimer.calculateSkippedMeasurements(byteTarget,measurementVolume,bufferSize);
        Assert.assertEquals(6,skipped);
    }

}
