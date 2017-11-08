/*
 * File: PerformanceTimerHelper
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-12
 * Type: Class
 */
package de.b4sh.byter.utils.measurements;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.b4sh.byter.utils.io.FileManager;

/**
 * Performance Timer Helper class.
 * Supporting functions are placed here.
 */
public final class PerformanceTimerHelper {

    private static final Logger log = Logger.getLogger(PerformanceTimerHelper.class.getName());

    /**
     * Constructor.
     */
    public PerformanceTimerHelper(){
        //NOP
    }

    /**
     * Write the Evaluation Data to Disc.
     * @param testName test name to use
     * @param fileAddition file addition to use - eg. _network
     * @param pt performance timer with data
     */
    public static void createEvaluationData(final String testName, final String fileAddition, final PerformanceTimer pt){
        //check if evaluation folder is existing
        final String evaluationFolder = FileManager.getNewEvaluationFolder(testName);
        if(!FileManager.isFolderExisting(evaluationFolder)){
            log.log(Level.INFO, "Evaluationfolder " + evaluationFolder + " is not existing, adding one now!");
            if(!FileManager.createFolder(evaluationFolder)){
                log.log(Level.WARNING, "Cannot create folder for evaluation.");
                log.log(Level.WARNING, "Stopping printing evaluation and measurement data due to the error creating the expected folder.");
                return;
            }else{
                log.log(Level.INFO, "Successfully added one new evaluation folder.");
            }
        }
        final File path = new File(evaluationFolder);
        if(pt != null){
            //i dont have any test names currently (something for a later implementation)
            final File measurementDataFile = new File(path, testName + "_ "+ fileAddition +"_measurement.data");
            final File evaluationDataFile = new File(path,testName + "_" + fileAddition + "_eval.data");
            //write to file
            pt.writeMeasurementDataToFile(measurementDataFile);
            pt.writeEvaluationToFile(evaluationDataFile,pt.evaluate());
        }
    }

}
