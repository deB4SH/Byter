package de.b4sh.byter.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;

import com.beust.jcommander.JCommander;
import de.b4sh.byter.CliParameter;
import de.b4sh.byter.server.Server;
import de.b4sh.byter.utils.io.PortScanner;

/**
 * Test for testing some functions of the jmxmp interface packed into the glassfish maven package.
 * 2017-07-26: mute this test - i forked the whole jmxmp and muted the noisy part in a new bundle
 */
public final class JmxMpConnectionExceptionTest {
    private static final Logger log = Logger.getLogger(JmxMpConnectionExceptionTest.class.getName());

    /**
     * This test is just there to proof that there always drops an exception if someone connects
     * to the jmxmp and closes the socket right after the connection.
     */
    //@Test
    public void testIfExceptionDrops(){
        final String[] argv = {"-s","server","-jp","62000"};
        CliParameter params = new CliParameter();
        JCommander jcommander = new JCommander();
        JCommander.newBuilder().addObject(params)
                .build()
                .parse(argv);
        //should start a server on port 62000
        Thread t1 = new Thread(new Server(params,true));
        //now try to scan ports
        PortScanner.getNextPort(62000,2);
        log.log(Level.INFO,"HELLO STACKTRACE MY OLD FRIEND ;)");
        Assert.assertNotEquals(false,true);
    }

}
