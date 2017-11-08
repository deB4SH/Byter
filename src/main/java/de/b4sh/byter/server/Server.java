package de.b4sh.byter.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXConnectorServer;

import de.b4sh.byter.CliParameter;
import de.b4sh.byter.example.staticmbean.ExampleStatic;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.io.PortScanner;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.jmx.MBeanHelper;

/**
 * Server Root class.
 * Contains every root method for starting server related components
 */
public final class Server implements Runnable {
    private static final Logger log = Logger.getLogger(Server.class.getName());
    private final CliParameter parameter;
    private final MBeanServer mbs;
    private final NetworkManager networkManager;
    private final MBeanHelper mBeanHelper;
    private JMXConnectorServer connector;
    private int connectorSystemPort;

    /**
     * Server Constructor.
     * Builds the base of the server.
     * @param parameter launch parameter set by the cli
     * @param runDirect decide if the runnable should be started inside the constructor (start long running task)
     */
    public Server(final CliParameter parameter, final boolean runDirect) {
        this.mbs = MBeanServerFactory.createMBeanServer(StringGenerator.nextRandomString(5));
        this.mBeanHelper = new MBeanHelper(this.mbs);
        this.networkManager = new NetworkManager(this.mBeanHelper,10, parameter.jmxPort);
        this.parameter = parameter;

        if (this.parameter.jmx) {
            this.buildExampleStaticMBean();
            this.buildServerJmxController();
            this.buildServerJmxNetworkManager();
        }
        if (runDirect)
            this.run();
    }

    /**
     * Tutorial MBean for master thesis.
     */
    private void buildExampleStaticMBean(){
        try{
            final ExampleStatic exampleStatic = new ExampleStatic("HOST",42);
            this.mBeanHelper.registerStaticElement(exampleStatic,exampleStatic.getObjectName());
        } catch (final MalformedObjectNameException e) {
            log.log(Level.WARNING,"Could not create static example mbean due to Malformed ObjectName Exception");
        }
    }

    /**
     * Build up the service manager.
     */
    private void buildServerJmxNetworkManager() {
        final ServerJmxNetworkManager serverJmxNetworkManager = new ServerJmxNetworkManager("de.b4sh.byter.server", "NetworkManager", this.networkManager);
        this.mBeanHelper.registerElement(serverJmxNetworkManager, serverJmxNetworkManager.getObjectName());
    }

    /**
     * Build the basic Controller for the server component.
     */
    private void buildServerJmxController() {
        final ServerJmxController serverJmxController = new ServerJmxController("de.b4sh.byter.server", "Controller", this);
        this.mBeanHelper.registerElement(serverJmxController,serverJmxController.getObjectName());
    }

    /**
     * Build the jmx connector.
     */
    private void buildJmxConnector() {
        try {
            //check if the passed port over the parameters is free and open to use.
            final int port = PortScanner.getNextPort(parameter.jmxPort,1);
            if(port != -1){ //-1 is returned when the scanned ports are closed.
                this.connector = JmxConnectionHelper.buildAndStartJmxConnector(port,this.mbs);
            }else{
                this.connector = JmxConnectionHelper.buildAndStartJmxConnector(this.parameter.jmxPort,10,this.mbs);
            }
            this.connectorSystemPort = this.connector.getAddress().getPort();
            log.log(Level.INFO,"Started JMX-Service on port: " + this.connector.getAddress().getPort());
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception during creating JmxConnector in Client. Check StackTrace for issues.", e);
        }
    }

    /**
     * Stop the JmxConnector Service.
     * should be called in emergency or shutdown hooks only ! ;-)
     */
    public void stopJmxConnector() {
        try {
            //JmxServerHelper.unregisterEveryUserMBean(this.mbs,"de.b4sh.byter");
            this.connector.stop();
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception during stop of JmxConnector");
        }
    }

    /**
     * Run-Method.
     * starts long running task (jmx connector)
     */
    @Override
    public void run() {
        this.buildJmxConnector();
    }

    /**
     * get the current set jmx-port.
     * @return int with the current used jmx port.
     */
    public int getConnectorSystemPort() {
        return connectorSystemPort;
    }
}