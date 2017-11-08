package de.b4sh.byter.utils.data;

/**
 * Class for storing transformation values that are used in many different tasks.
 */
public final class TransformValues {
    //general length generation 1*[..]
    public static final double KILOBYTE = 1 * Math.pow(10,3);
    public static final double MEGABYTE = 1 * Math.pow(10,6);
    public static final double GIGABYTE = 1 * Math.pow(10,9);
    //from byte-length to [..]
    public static final double byteToKILOBYTE = 1 * Math.pow(10,3);
    public static final double byteToMEGABYTE = 1 * Math.pow(10,-6);
    //from time to time ..
    public static final double nanoSecondToSecond = 1 * Math.pow(10,-9);
    public static final double milliSecondToSecond = 1 * Math.pow(10,-3);
    //private constructor for checkstyle annoyance ;)
    private TransformValues(){
        //nop
    }
}
