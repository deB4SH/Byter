/*
 * File: JUnitFileWriter
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-31
 * Type: Class
 */
package de.b4sh.byter.support;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JUnitFileWriter {

    private static final Logger log = Logger.getLogger(JUnitFileWriter.class.getName());

    public static void writeToFile(final String content, final File file){
        try(
            final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))
        ){
            outputStream.write(content.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during write of a String to a File. Check Stacktrace for issues.",e);
        }
    }

}
