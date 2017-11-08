package de.b4sh.byter.commander;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanOperation;

import de.b4sh.byter.utils.jmx.JmxEntity;

/**
 * Class for controlling the Commander over JMX.
 */
@JMXBean(description = "Byter.Commander.Controller")
public final class CommanderJmxController extends JmxEntity {
    //static values
    private static final Logger log = Logger.getLogger(CommanderJmxController.class.getName());
    //jmx based values
    private final Commander commander;

    /**
     * public constructor with base initialisation.
     * @param packageName set the packagename this controller should be available under
     * @param type set the type this controller should be available under
     * @param commander commander to work on
     */
    public CommanderJmxController(final String packageName, final String type, final Commander commander) {
        super(packageName, type);
        this.commander = commander;
    }

    /**
     * function for shutting down the service.
     */
    @JMXBeanOperation(name = "Shutdown", description = "Shutdown the commander")
    public void shutdown(){
        log.log(Level.INFO, "Shutdown Commander JMX Component.");
        this.commander.stopJmxConnector();
    }

}
