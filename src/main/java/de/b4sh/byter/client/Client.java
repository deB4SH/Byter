package de.b4sh.byter.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.IntrospectionException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.remote.JMXConnectorServer;

import com.udojava.jmx.wrapper.JMXBeanWrapper;

import de.b4sh.byter.CliParameter;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.io.PortScanner;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;

/**
 * Client Root.
 */
public final class Client implements Runnable{

    private static final Logger log = Logger.getLogger(Client.class.getName());
    private final CliParameter parameter;
    private final MBeanServer mbs;
    private JMXConnectorServer connector;
    private int connectorPort;

    /**
     * Client Constructor.
     * @param parameter start parameter from cli
     * @param runDirect decide if the client should be started direct (long running)
     */
    public Client(final CliParameter parameter, final boolean runDirect){
        this.parameter = parameter;
        if(this.parameter.jmx){
            this.mbs = MBeanServerFactory.createMBeanServer(StringGenerator.nextRandomString(5));
            this.buildClientJmxDisc();
            this.buildClientJmxNetwork();
            this.buildClientJmxReader();
            this.buildClientJmxController();
        }else{
            this.mbs = null;
        }
        if(runDirect)
            this.run();
    }

    /**
     * build jmx controller for client.
     */
    private void buildClientJmxController(){
        try{
            final ClientJmxController clientJmxController = new ClientJmxController("de.b4sh.byter.client","Controller",this);
            final JMXBeanWrapper wrapper = new JMXBeanWrapper(clientJmxController);
            this.mbs.registerMBean(wrapper, clientJmxController.getObjectName());
        } catch (IntrospectionException | NotCompliantMBeanException | MBeanRegistrationException | InstanceAlreadyExistsException e) {
            log.log(Level.WARNING,"Error during initialisation or registration of ClientDiscMBean."
                    + " see Stracktrace for more Information",e);
        }
    }

    /**
     * build controller for direct disc tests.
     */
    private void buildClientJmxDisc(){
        try{
            final ClientJmxDisc clientJmxDisc = new ClientJmxDisc("de.b4sh.byter.client","Disc");
            final JMXBeanWrapper wrapper = new JMXBeanWrapper(clientJmxDisc);
            this.mbs.registerMBean(wrapper,clientJmxDisc.getObjectName());
        } catch (IntrospectionException | NotCompliantMBeanException | MBeanRegistrationException | InstanceAlreadyExistsException e) {
            log.log(Level.WARNING,"Error during initialisation or registration of ClientDiscMBean."
                        + " see Stracktrace for more Information",e);
        }
    }

    private void buildClientJmxReader(){
        try{
            final ClientJmxReader clientJmxReader = new ClientJmxReader("de.b4sh.byter.client","Reader");
            final JMXBeanWrapper wrapper = new JMXBeanWrapper(clientJmxReader);
            this.mbs.registerMBean(wrapper,clientJmxReader.getObjectName());
        } catch (IntrospectionException | NotCompliantMBeanException | MBeanRegistrationException | InstanceAlreadyExistsException e) {
            log.log(Level.WARNING,"Error during initialisation or registration of ClientReaderMBean."
                    + " see Stracktrace for more Information",e);
        }
    }

    /**
     * build controller for network based tests between client and server.
     */
    private void buildClientJmxNetwork(){
        try{
            final ClientJmxNetwork clientJmxDisc = new ClientJmxNetwork("de.b4sh.byter.client","Network");
            final JMXBeanWrapper wrapper = new JMXBeanWrapper(clientJmxDisc);
            this.mbs.registerMBean(wrapper,clientJmxDisc.getObjectName());
        } catch (IntrospectionException | NotCompliantMBeanException | MBeanRegistrationException | InstanceAlreadyExistsException e) {
            log.log(Level.WARNING,"Error during initialisation or registration of ClientNetworkMBean."
                    + " see Stracktrace for more Information",e);
        }
    }

    /**
     * build plain controller.
     */
    private void buildJmxConnector(){
        try {
            //check if the passed port over the parameters is free and open to use.
            final int port = PortScanner.getNextPort(parameter.jmxPort,1);
            if(port != -1){ //-1 is returned when the scanned ports are closed.
                this.connector = JmxConnectionHelper.buildAndStartJmxConnector(port,this.mbs);
            }else{
                this.connector = JmxConnectionHelper.buildAndStartJmxConnector(this.parameter.jmxPort,10,this.mbs);
            }
            this.connectorPort = this.connector.getAddress().getPort();
            log.log(Level.INFO,"Started JMX-Service on port: " + this.connector.getAddress().getPort());
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception during creating JmxConnector in Client. Check StackTrace for issues.",e);
        }
    }

    /**
     * function for stopping jmx connector.
     * should be called for shutdown service
     */
    public void stopJmxConnector(){
        try {
            //JmxClientNetworkHelper.unregisterEveryUserMBean(this.mbs,"de.b4sh.byter");
            connector.stop();
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during stop of JmxConnector");
        }
    }

    /**
     * Starts runnable task.
     * LONG RUNNING TASK! (jmx connector service)
     */
    @Override
    public void run() {
        //LONG RUNNING: start jmx server here
        if(this.parameter.jmx){
            this.buildJmxConnector();
        }
    }

    /**
     * get the port of the connector service.
     * @return int with the currently used port
     */
    public int getConnectorSystemPort() {
        return connectorPort;
    }
}
