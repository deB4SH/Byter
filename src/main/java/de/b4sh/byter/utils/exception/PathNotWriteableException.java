package de.b4sh.byter.utils.exception;

/**
 * Exception for return that the path is not writable over different invokes.
 */
public class PathNotWriteableException extends Exception{

    /**
     * Constructor for an exception that declares that a path is not writable.
     * @param path path thats not writable
     */
    public PathNotWriteableException(final String path){
        super("The selected path is not writable. Path: " + path);
    }

}
