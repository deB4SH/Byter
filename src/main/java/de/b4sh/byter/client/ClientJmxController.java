package de.b4sh.byter.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import de.b4sh.byter.utils.jmx.JmxEntity;

/**
 * Jmx-Komponente for Client.
 */
@JMXBean(description = "Byter.Client")
public final class ClientJmxController extends JmxEntity{
    //static values
    private static final Logger log = Logger.getLogger(ClientJmxController.class.getName());
    //jmx based values
    private final Client client;

    /**
     * public constructor with base initialisation.
     * @param packageName set the packagename this controller should be available under
     * @param type set the type this controller should be available under
     * @param client client to work on
     */
    ClientJmxController(final String packageName, final String type, final Client client) {
        super(packageName, type);
        this.client = client;
    }

    /**
     * function for shutting down the service.
     */
    @JMXBeanOperation(name = "Shutdown", description = "Shutdown the client")
    public void shutdown(){
        log.log(Level.INFO, "Shutdown Client JMX Component.");
        this.client.stopJmxConnector();
    }
}
