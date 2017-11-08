package de.b4sh.byter.utils.measurements;

import java.util.ArrayList;
import java.util.List;

import de.b4sh.byter.utils.data.TransformValues;

/**
 * Measurement Class keeps data from single measurement points for later usage.
 */
public final class Measurement {

    private final String caller;
    private final int dataLength;
    private final long startTime;
    private final long endTime;
    private final long timeIntervalInNanoSecond;
    private final double timeIntervalInSecond;
    private final double bytePerSecound;
    private final float megaBytePerSecond;

    /**
     * Constructor for Measurement.
     * @param caller caller for this measurement.
     * @param dataLength data length that was transmitted
     * @param startTime start time of this transmitted data
     * @param endTime end time of this transmitted data
     */
    public Measurement(final String caller, final int dataLength, final long startTime, final long endTime) {
        this.caller = caller;
        this.dataLength = dataLength;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeIntervalInNanoSecond = endTime - startTime;
        this.timeIntervalInSecond = this.timeIntervalInNanoSecond * TransformValues.nanoSecondToSecond;
        this.bytePerSecound = this.dataLength / this.timeIntervalInSecond;
        this.megaBytePerSecond = (float) ((this.dataLength * TransformValues.byteToMEGABYTE) / this.timeIntervalInSecond);
    }

    /**
     * create a StringList from the taken measurement.
     * @return a string list with the measured data.
     */
    List<String> getStringListFromMeasurement(){
        final List<String> data = new ArrayList<>();
        data.add(objectToString(startTime));
        data.add(objectToString(endTime));
        data.add(objectToString(timeIntervalInNanoSecond));
        data.add(objectToString(timeIntervalInSecond));
        data.add(objectToString(dataLength));
        data.add(objectToString(bytePerSecound));
        data.add(objectToString(megaBytePerSecond));
        return data;
    }

    /**
     * Transform elements to String object.
     * @param object object to transform
     * @return String with content of passed object
     */
    private String objectToString(final Object object){
        if (object instanceof Float)
            return String.format("%.10f",object);
        else
            return String.valueOf(object);
    }

    /**
     * get the caller.
     * @return String
     */
    public String getCaller() {
        return caller;
    }

    /**
     * get data length.
     * @return int
     */
    public int getDataLength() {
        return dataLength;
    }

    /**
     * get start time.
     * (System.nanoTime)
     * @return long
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * get end time.
     * (System.nanoTime)
     * @return long
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * get the calculated time between start and end.
     * time is in nano seconds.
     * @return long
     */
    public long getTimeIntervalInNanoSecond() {
        return timeIntervalInNanoSecond;
    }

    /**
     * get the calculated time between start and end.
     * time is in seconds.
     * @return float
     */
    public double getTimeIntervalInSecond() {
        return timeIntervalInSecond;
    }

    /**
     * get the speed on the data transmission. this value is calculated.
     * bytes/seconds
     * @return float
     */
    public double getBytePerSecound() {
        return bytePerSecound;
    }

    /**
     * get the speed on the data transmission. tis value is calculated.
     * megabytes/seconds
     * @return float
     */
    public float getMegaBytePerSecond() {
        return megaBytePerSecond;
    }
}
