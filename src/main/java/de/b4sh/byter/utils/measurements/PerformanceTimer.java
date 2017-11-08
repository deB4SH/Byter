package de.b4sh.byter.utils.measurements;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.b4sh.byter.utils.measurements.evaluators.AverageEvaluator;
import de.b4sh.byter.utils.measurements.evaluators.Evaluator;
import de.b4sh.byter.utils.measurements.evaluators.MaxEvaluator;
import de.b4sh.byter.utils.measurements.evaluators.MidFieldAverageEvaluator;
import de.b4sh.byter.utils.measurements.evaluators.MinEvaluator;

/**
 * PerformanceTimer that manages all measurement data and evaluation of it.
 */
public final class PerformanceTimer {
    private static final Logger log = Logger.getLogger(PerformanceTimer.class.getName());
    //generic data
    private final String classCaller;
    private long globalStartTime;
    private long globalEndTime;
    private int measurementVolume;
    private int measurementStep;
    private int measurementCounter;
    //specific data
    private List<Measurement> data;
    private List<Evaluator> evaluator;

    /**
     * Constructor for PerformanceTimer.
     * Tracks every input in a measurement list. Size is unlimited.
     * @param classCaller the class that created this performance timer.
     */
    public PerformanceTimer(final String classCaller) {
        this.classCaller = classCaller;
        this.data = new ArrayList<>();
        this.evaluator = new ArrayList<>();
        this.addStandardEvaluators();
        this.measurementStep = 0;
        this.measurementVolume = 0;
        this.measurementCounter = 0;
    }

    /**
     * Constructor for PerformanceTimer.
     * Tracks only the given amount of volume.
     * Also capable of skipping the tracking process for steps.
     * So you wont flood the list while writing large files directly to disc. (1TB eg.)
     * @param classCaller the class that created this performance timer.
     * @param measurementSteps the steps between measurements
     * @param measurementVolume the size of the list which contains the actual measurements
     */
    public PerformanceTimer(final String classCaller, final int measurementVolume, final int measurementSteps) {
        this.classCaller = classCaller;
        this.data = new ArrayList<>();
        this.evaluator = new ArrayList<>();
        this.addStandardEvaluators();
        this.measurementStep = measurementSteps;
        this.measurementVolume = measurementVolume;
        this.measurementCounter = 0;
    }

    /**
     * Set a new measurementVolume.
     * @param measurementVolume size of the data list
     */
    public void setMeasurementVolume(final int measurementVolume) {
        this.measurementVolume = measurementVolume;
    }

    /**
     * Set a new stepcount.
     * @param measurementStep how many iterations should be skipped for tracking.
     */
    public void setMeasurementStep(final int measurementStep) {
        this.measurementStep = measurementStep;
    }

    private void addStandardEvaluators(){
        this.addEvaluator(new MaxEvaluator());
        this.addEvaluator(new MinEvaluator());
        this.addEvaluator(new AverageEvaluator());
        this.addEvaluator(new MidFieldAverageEvaluator());
    }

    /**
     * start global time tracking.
     */
    public void startTime(){
        this.globalStartTime = System.nanoTime();
    }

    /**
     * End global time tracking.
     */
    public void endTime(){
        this.globalEndTime = System.nanoTime();
    }

    /**
     * Adds a new measurement with a specific caller.
     * @param start start time of system.nanotime
     * @param end end time of system.nanotime
     * @param datasegmentSize size of the transmitted segment
     * @param caller caller that should be placed inside the measurement
     */
    public void addNewMeasurement(final long start, final long end, final int datasegmentSize, final String caller){
        this.addNewMeasurement(new Measurement(caller,datasegmentSize,start,end));
    }

    /**
     * Adds a new measurement with set clazzcaller.
     * @param start start time of system.nanotime
     * @param end end time of system.nanotime
     * @param datasegmentSize size of the transmitted segment
     */
    public void addNewMeasurement(final long start, final long end, final int datasegmentSize){
        this.addNewMeasurement(new Measurement(this.classCaller,datasegmentSize,start,end));
    }

    /**
     * Adds a new measurement to list.
     * @param m fulfilled measurement
     */
    public void addNewMeasurement(final Measurement m){
        if(this.measurementVolume == 0 || this.measurementVolume == -1){ //0 take everything , -1 initial value
            this.data.add(m);
        }
        else if(this.data.size() != measurementVolume){
            if(measurementStep == 0){ //have volume but track everything up to limit
                this.data.add(m);
            }else{
                if((measurementCounter % measurementStep) == 0){ //have volume and a step counter for selective tracking
                    this.data.add(m);
                }
            }
            measurementCounter++;
        }
    }

    /**
     * Add an evaluator to list for later usage.
     * @param e evaluator to add
     */
    public void addEvaluator(final Evaluator e){
        this.evaluator.add(e);
    }

    /**
     * get the collected measurement data.
     */
    public List<Measurement> getData(){
        return this.data;
    }

    /**
     * Evaluate tracked measurements.
     * If the Timer doesn't contain any data and empty HashMap is returned.
     */
    public Map<String, Float> evaluate(){
        final Map<String, Float> responses = new HashMap<>();
        if(this.data.size() > 0){
            for (Evaluator e : this.evaluator){
                responses.putAll(e.evaluate(this));
            }
            return responses;
        }else{
            log.log(Level.INFO,"There was no data to evaluate. ClassCaller: " + this.classCaller);
        }
        return responses;
    }

    /**
     * Write the evaluated data to a file.
     * @param outFile file to write to
     * @param map evaluated map with data from measurements.
     */
    public void writeEvaluationToFile(final File outFile, final Map<String, Float> map){
        final GsonBuilder gsonBuilder = new GsonBuilder();
        //register bigDecimal typer for no scientific value printing
        gsonBuilder.registerTypeAdapter(Float.class,  new JsonSerializer<Float>() {
            @Override
            public JsonElement serialize(final Float src, final Type typeOfSrc, final JsonSerializationContext context) {
                try{
                    final BigDecimal bigValue = BigDecimal.valueOf(src);
                    return new JsonPrimitive(bigValue.toPlainString());
                }catch (final NumberFormatException nfe){
                    log.log(Level.INFO,map.toString());
                    log.log(Level.WARNING, "NumberFormatException during serialize. Number: " + src);
                    return new JsonPrimitive(src);
                }
            }
        });
        final Gson gson = gsonBuilder.setPrettyPrinting().create();
        final String json = gson.toJson(map);
        //write file to disc
        try(
            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))
        ){
            outputStream.write(json.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception writing evaluation to disc",e);
        }
    }

    /**
     * Write all taken data to a csv file at the desired place.
     * @param outFile output file that should be used to store data
     */
    public void writeMeasurementDataToFile(final File outFile){
        try(
            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))
        ){
            //print head row for data
            outputStream.write(csvifyStringList(generateHeadForCsv()).getBytes());
            outputStream.flush();
            //print actual collected data
            for(Measurement mt: this.data){
                outputStream.write(csvifyStringList(mt.getStringListFromMeasurement()).getBytes());
                outputStream.flush();
            }
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception writing measurement data to disc",e);
        }
    }

    /**
     * Build the head-row for the measurement data.
     * @return List with every head needed.
     */
    private List<String> generateHeadForCsv(){
        final List<String> head = new ArrayList<>();
        head.add("StartTime");
        head.add("EndTime");
        head.add("TimeDifference");
        head.add("TimeDifferenceSeconds");
        head.add("DataLength");
        head.add("BytePerSecond");
        head.add("MegabytePerSecond");
        return head;
    }

    /**
     * Create a valid csv line out of a StringList.
     * Usecase: Header of CSV with all tags of each row.
     * @param list a line of a csv
     * @return csvified list
     */
    private String csvifyStringList(final List<String> list){
        final StringBuilder builder = new StringBuilder(1000);
        for(String s: list){
            builder.append(s);
            builder.append(";");//field separator
        }
        builder.append("\n");//line separator
        return builder.toString();
    }

    /**
     * calculate the amount of skipped steps to have a trackpoint amoung the whole dataset.
     * @param byteTarget target of bytes to transmit
     * @param measurementVolume the measurement volume to fulfill (eg. 2500 tracked points)
     * @param bufferSize the buffersize which is used by the implementation
     * @return steps to ignore for a equal detection
     */
    public static int calculateSkippedMeasurements(final long byteTarget, final int measurementVolume, final int bufferSize){
        final int maxTracking = (int) (byteTarget / bufferSize);
        return maxTracking / measurementVolume;
    }
}
