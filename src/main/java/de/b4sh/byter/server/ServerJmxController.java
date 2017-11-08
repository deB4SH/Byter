package de.b4sh.byter.server;

import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import de.b4sh.byter.utils.jmx.JmxEntity;

/**
 * JmxController for server management.
 * functions for server management and control of units.
 */
@JMXBean(description = "Byter.Server")
public final class ServerJmxController extends JmxEntity{
    //static values
    private static final Logger log = Logger.getLogger(ServerJmxController.class.getName());
    private static final String logPre = "[JMXSERVER]:";
    private final Server server;

    /**
     * public constructor with base initialisation.
     * @param server root server object to work with
     * @param packageName package name to use
     * @param type type to use
     */
    public ServerJmxController(final String packageName, final String type, final Server server) {
        super(packageName, type);
        this.server = server;
    }

    /**
     * shutdown function - invokable via jmx.
     * shuts down the whole service
     */
    @JMXBeanOperation(name = "Shutdown", description = "Shutdown the server")
    public void shutdown(){
        this.server.stopJmxConnector();
    }

}
