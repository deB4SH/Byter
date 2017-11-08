/*
 * File: ThreadManager
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-08
 * Type: Class
 */
package de.b4sh.byter.utils.io;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ThreadManager hosts a function to nap!
 */
public class ThreadManager {
    private static final Logger log = Logger.getLogger(ThreadManager.class.getName());

    /**
     * Let the Thread sleep a bit.
     * @param napTime time to sleep in ms
     */
    public static void nap(final long napTime){
        try {
            Thread.sleep(napTime);
        } catch (InterruptedException e) {
            log.log(Level.WARNING,"Exception during nap(). See Stacktrace for issue please",e);
        }
    }
}
