package de.b4sh.byter.support;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import com.beust.jcommander.JCommander;
import de.b4sh.byter.CliParameter;
import de.b4sh.byter.client.Client;
import de.b4sh.byter.commander.Commander;
import de.b4sh.byter.server.Server;
import de.b4sh.byter.utils.exception.FunctionNotImplementedException;
import de.b4sh.byter.utils.io.ThreadManager;
import de.b4sh.byter.utils.jmx.JmxClientNetworkHelper;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.jmx.JmxServerHelper;

/**
 * JUnit Helper Class for starting the Server, Client and Commander Component of this application.
 */
public final class ComponentHelper {
    private static final Logger log = Logger.getLogger(ComponentHelper.class.getName());
    private static final String JMXSERVERIP = "localhost";

    /**
     * Start Server as Thread inside a ExecutorService.
     * @param service service to start in
     * @return running Server object
     */
    public static Server startServer(final ExecutorService service){
        //build arguments
        final String[] argv = new String[]{"-s","server"};
        final CliParameter params = new CliParameter();
        JCommander jcommander = new JCommander();
        JCommander.newBuilder().addObject(params)
                .build()
                .parse(argv);
        //build server runnable
        final Server serverObj = new Server(params,false);
        final Thread serverThread = new Thread(serverObj);
        serverThread.setDaemon(true);
        log.log(Level.INFO, "done building server and deamon thread object");
        //start inside service
        service.execute(serverThread);
        log.log(Level.INFO,"done submitting daemon thread to service pool");
        ThreadManager.nap(3000);
        return serverObj;
    }

    /**
     * Start Client as Thread inside a ExecutorService.
     * @param service service to start in
     * @return running Client object
     */
    public static Client startClient(final ExecutorService service){
        //build arguments
        final String[] argv = new String[]{"-s","client"};
        final CliParameter params = new CliParameter();
        JCommander jcommander = new JCommander();
        JCommander.newBuilder().addObject(params)
                .build()
                .parse(argv);
        //build client runnable
        final Client clientObj = new Client(params,false);
        final Thread clientThread = new Thread(clientObj);
        clientThread.setDaemon(true);
        log.log(Level.INFO, "done building client and deamon thread object");
        service.execute(clientThread);
        log.log(Level.INFO,"done submitting daemon thread to service pool");
        ThreadManager.nap(3000);
        return clientObj;
    }

    public static Commander startCommander(final ExecutorService service){
        //build arguments
        final String configurationDirectory = System.getProperty("user.dir") + File.separator + "configurations" + File.separator + "test";
        final String[] argv = new String[]{"-s","commander","-cfg",configurationDirectory};
        final CliParameter params = new CliParameter();
        JCommander jcommander = new JCommander();
        JCommander.newBuilder().addObject(params)
                .build()
                .parse(argv);
        //build client runnable
        final Commander commander = new Commander(params,false);
        final Thread commanderThread = new Thread(commander);
        commanderThread.setDaemon(true);
        log.log(Level.INFO, "done building commander and deamon thread object");
        service.execute(commanderThread);
        log.log(Level.INFO,"done submitting daemon thread to service pool");
        ThreadManager.nap(3000);
        return commander;
    }

    /**
     * Shutdown the jmx part of the server. Should also shut down everything else inside the server.
     * @param serverObj running server object
     */
    public static void shutdownServer(final Server serverObj){
        try{
            final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,serverObj.getConnectorSystemPort());
            MBeanServerConnection mbs = JmxServerHelper.getMBeanServer(connection);
            ObjectName controller = JmxServerHelper.findObjectName(mbs,"de.b4sh.byter","Controller");
            JmxServerHelper.shutdownServerService(mbs,controller);
        } catch (IOException e) {
            log.log(Level.WARNING,"Could not shutdown Server via JMX! IO Exception.");
        }
    }

    /**
     * Shutdown the jmx part of the client. Should also shut down everything else inside the client.
     * @param client
     */
    public static void shutdownClient(final Client client){
        try{
            final JMXConnector connection = JmxConnectionHelper.buildJmxMPConnector(JMXSERVERIP,client.getConnectorSystemPort());
            MBeanServerConnection mbs = JmxServerHelper.getMBeanServer(connection);
            ObjectName controller = JmxServerHelper.findObjectName(mbs,"de.b4sh.byter","Controller");
            JmxClientNetworkHelper.shutdownClientService(mbs,controller);
        } catch (IOException e) {
            log.log(Level.WARNING,"Could not shutdown Client via JMX! IO Exception.");
        }
    }

    /**
     * Shutdown the jmx part of the commander. Should also shut down everything inside.
     * @param commander
     */
    public static void shutdownCommander(final Commander commander){
        //TODO: implement
        throw new FunctionNotImplementedException("shutdownCommander");
    }

    /**
     * shuts down the whole exececutor service that keeps the current services alive :(
     */
    public static void shutdownService(final ExecutorService service){
        service.shutdown();
        ThreadManager.nap(1000); //wait for the service to shutdown properly
        if(!service.isShutdown()){
            service.shutdownNow(); //still running? killing it not softly it is!
        }
        ThreadManager.nap(1000);
    }
}
