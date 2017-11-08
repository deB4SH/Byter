package de.b4sh.byter.utils.exception;

/**
 * Class for throwing an exception for a not implemented function.
 */
public class FunctionNotImplementedException extends RuntimeException {

    /**
     * Constructor for not implemented functions.
     * @param functionName function name that is not implemented
     */
    public FunctionNotImplementedException(final String functionName){
        super("The function " + functionName + " is not implemented yet!");
    }

}
