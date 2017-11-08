package de.b4sh.byter.commander;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;

import de.b4sh.byter.CliParameter;
import de.b4sh.byter.commander.config.ClientConfiguration;
import de.b4sh.byter.commander.config.ClientConnection;
import de.b4sh.byter.commander.config.ConfigurationHelper;
import de.b4sh.byter.commander.config.DirectConfiguration;
import de.b4sh.byter.commander.config.NetworkConfiguration;
import de.b4sh.byter.commander.config.ServerConfiguration;
import de.b4sh.byter.server.network.NetworkType;
import de.b4sh.byter.utils.data.StringGenerator;
import de.b4sh.byter.utils.exception.CommanderWorkflowError;
import de.b4sh.byter.utils.jmx.JmxClientDiscHelper;
import de.b4sh.byter.utils.jmx.JmxClientNetworkHelper;
import de.b4sh.byter.utils.jmx.JmxConnectionHelper;
import de.b4sh.byter.utils.jmx.JmxServerHelper;
import de.b4sh.byter.utils.jmx.MBeanHelper;
import de.b4sh.byter.utils.writer.WriterType;

/**
 * Commander is the controlling part above the client and server run times.
 * It reads configuration files and runs through the predefined tests.
 */
public final class Commander implements Runnable {
    private static final Logger log = Logger.getLogger(Commander.class.getName());
    private final ConfigurationHelper configurationHelper;
    private final MBeanServer mbs;
    private final CliParameter parameter;
    private JMXConnectorServer connector;
    private int connectorPort;
    private CommanderJmxTestEnvironment commanderTestEnvironment;
    private CommanderJmxCredentials commanderServerCredentials;
    private CommanderJmxCredentials commanderClientCredentials;
    private CommanderJmxController commanderController;
    private MBeanHelper mBeanHelper;
    //junit test vars
    private boolean junitRun;
    private int serverPort;
    private int clientPort;

    /**
     * Constructor for Commander Runtime.
     * @param parameter cli params
     * @param runDirect decide if the class should run direct
     */
    public Commander(final CliParameter parameter, final boolean runDirect) {
        this.configurationHelper = new ConfigurationHelper();
        this.parameter = parameter;
        this.junitRun = false;
        if(parameter.jmx){
            this.mbs = MBeanServerFactory.createMBeanServer(StringGenerator.nextRandomString(5));
            this.mBeanHelper = new MBeanHelper(this.mbs);
            this.buildJmxController();
            //this.buildJmxCredentials();
            this.buildJmxTestEnvironment();
        }else{
            this.mbs = null;
            this.mBeanHelper = null;
        }
        //shall i run this direct?
        if(runDirect)
            this.run();
    }

    /**
     * Run Method.
     */
    @Override
    public void run() {
        buildJmxConnector();
        //do direct based tests first
        final List<DirectConfiguration> directConfigurations = configurationHelper.readDirectConfigurations(parameter.configPath);
        final List<NetworkConfiguration> networkConfigurations = configurationHelper.readNetworkConfigurations(parameter.configPath);
        //direct workflow
        directWorkflow(directConfigurations);
        //do network based tests
        networkWorkflow(networkConfigurations);
        log.log(Level.INFO,"Done with the workflow. Shutting down now!!");
        if(!junitRun)
            System.exit(0);
    }

    /**
     * Workflow Method for direct writing use-case.
     * @param configurations
     */
    private void directWorkflow(final List<DirectConfiguration> configurations){
        for(DirectConfiguration rc: configurations){
            //log out once which test is currently running
            this.logCurrentStartingDirectConfiguration(rc);
            //build connector list to all clients
            final ClientConfiguration cc = rc.getClientConfiguration();
            final List<ClientConnection> clientList = rc.getClientConfiguration().getClients();
            final List<JMXConnector> jmxConnectors = buildClientConnectors(clientList);
            final Map<JMXConnector, JmxConnectionStorage> connectionStorage = new HashMap<>();
            //set parameters to all clients
            int connectorId = 0;
            for(JMXConnector connector: jmxConnectors){
                //workflow
                final MBeanServerConnection clientMbs = JmxClientNetworkHelper.getMBeanServer(connector);
                final ObjectName clientDiscController = JmxClientNetworkHelper.findObjectName(clientMbs,"de.b4sh.byter","Disc");
                final ObjectName clientController = JmxClientNetworkHelper.findObjectName(clientMbs,"de.b4sh.byter","Controller");
                //save mbeans
                connectionStorage.put(connector, new JmxConnectionStorage(clientDiscController,clientMbs));
                //set up env.
                JmxClientDiscHelper.setByteTarget(clientMbs,clientDiscController, cc.getTransmitTarget());
                JmxClientDiscHelper.setChunkSize(clientMbs,clientDiscController, cc.getPregeneratedChunkSize());
                JmxClientDiscHelper.setFileName(clientMbs,clientDiscController, rc.getTestName() + "_" + connectorId);
                //increase connectorId for multiple services - if just one is used , wenn one operation into oblivion
                connectorId++;
                //set filepath (transmit realative path and resolv on client side to generate the real path, not the once from the commander)
                //old code: https://gist.github.com/deB4SH/edfe1975e4886ab7efcf99c177348f32
                JmxClientDiscHelper.setOutputPath(clientMbs,clientDiscController, rc.getWriteFilePath());
                //automatic removal flag
                if(this.junitRun){
                    log.log(Level.INFO, "Junit Run. Setting AutomaticFileRemoval for Writer to false.");
                    JmxClientDiscHelper.setAutomaticFileRemoval(clientMbs,clientDiscController,false);
                }
                //set missing parameters
                JmxClientDiscHelper.setWriterBufferSize(clientMbs,clientDiscController,cc.getBufferSize());
                JmxClientDiscHelper.setWriterImplementation(clientMbs,clientDiscController, cc.getIoImplementation());
                //measurement volume
                JmxClientDiscHelper.setMeasurementVolume(clientMbs,clientDiscController,rc.getMeasurementCount());
                //rebuild evaluation path
                JmxClientDiscHelper.rebuildEvaluationFolder(clientMbs,clientDiscController);
            }
            //run all clients
            for(JMXConnector connector: jmxConnectors){
                final MBeanServerConnection mbs = connectionStorage.get(connector).getMbs();
                final ObjectName on = connectionStorage.get(connector).getOn();
                //JmxClientDiscHelper.invokeDiscTest(mbs,on);
                if(rc.getWriterCount() > 1 )
                    JmxClientDiscHelper.startMulitpleWriter(mbs,on,rc.getWriterCount());
                else
                    JmxClientDiscHelper.startWriter(mbs,on);
            }
            //check if the clients are ready
            boolean allConnectorWriterDone = false;
            final List<Boolean> connectorWriterStatusList = new ArrayList<>();
            while(!allConnectorWriterDone){
                connectorWriterStatusList.clear();
                for(JMXConnector connector: jmxConnectors) {
                    final MBeanServerConnection mbs = connectionStorage.get(connector).getMbs();
                    final ObjectName on = connectionStorage.get(connector).getOn();
                    connectorWriterStatusList.add(JmxClientDiscHelper.getActiveWriterDone(mbs,on));
                }
                if(!connectorWriterStatusList.contains(false))
                    allConnectorWriterDone = true;
            }
            //stop the service pool and reset the client env.
            for(JMXConnector connector: jmxConnectors) {
                final MBeanServerConnection mbs = connectionStorage.get(connector).getMbs();
                final ObjectName on = connectionStorage.get(connector).getOn();
                JmxClientDiscHelper.stopServicePool(mbs,on);
            }
        }
    }

    private void logCurrentStartingDirectConfiguration(final DirectConfiguration dc){
        final ClientConfiguration cc = dc.getClientConfiguration();
        log.log(Level.INFO," \nDirectConfiguration Information \n"
                + "Testname: " + dc.getTestName() + "\n"
                + "Writer Implementation: " + cc.getIoImplementation() + "\n"
                + "Buffersize: " + cc.getBufferSize() + "\n"
                + "Bytetarget: " + cc.getTransmitTarget() + "\n"
        );
    }

    private List<JMXConnector> buildClientConnectors(final List<ClientConnection> clientList){
        final List<JMXConnector> connectors = new ArrayList<>();
        for(ClientConnection clientConnection: clientList){
            final JMXConnector con;
            if(!this.junitRun){
                if(!clientConnection.getUsername().contentEquals("null")){
                    con = this.buildCLientConnectorWithLogin(clientConnection);
                }else{
                    con = this.buildCLientConnector(clientConnection);
                }
            }else{
                con = this.buildTestEnvironmentConnector(this.clientPort);
            }
            connectors.add(con);
        }
        return connectors;
    }

    private void networkWorkflow(final List<NetworkConfiguration> configurations){
        for(NetworkConfiguration rc: configurations){
            //output some informations which test is currently on
            log.log(Level.INFO, "CURRENT-TEST: " + rc.getTestName());

            //update test environment.
                //this.setTestEnvironMentVariables(sc,cc);

            //update credentials to the new once.
            final ServerConfiguration sc = rc.getServerConfiguration();
            final ClientConfiguration cc = rc.getClientConfiguration();
                //this.setServerCredentials(sc);
                //this.setClientCredentials(cc);

            //connect to both services
            final JMXConnector serverConnection;
            final List<JMXConnector> clientConnections = new ArrayList<>();
            //connect to server
            if(!this.junitRun){
                if(!sc.getUsername().contentEquals("null")){
                    serverConnection = this.buildServerConnectorWithLogin(sc);
                }else{
                    serverConnection = this.buildServerConnector(sc);
                }
            }else{
                serverConnection = this.buildTestEnvironmentConnector(this.serverPort);
            }
            //connect to client
            final List<ClientConnection> clientList = rc.getClientConfiguration().getClients();
            final List<JMXConnector> jmxConnectors = buildClientConnectors(clientList);
            final Map<JMXConnector, JmxConnectionStorage> connectionStorage = new HashMap<>();

            //server mbean
            final MBeanServerConnection serverMbs = JmxServerHelper.getMBeanServer(serverConnection);
            final ObjectName serverNetworkManager = JmxServerHelper.findObjectName(serverMbs,"de.b4sh.byter","NetworkManager");
            final ObjectName serverController = JmxServerHelper.findObjectName(serverMbs,"de.b4sh.byter","Controller");

            //client mbean
            for(JMXConnector connector: jmxConnectors) {
                //workflow
                final MBeanServerConnection mbs = JmxClientNetworkHelper.getMBeanServer(connector); //mbean server
                final ObjectName nmOn = JmxClientNetworkHelper.findObjectName(mbs, "de.b4sh.byter", "Network"); //network manager object name
                //final ObjectName clientController = JmxClientNetworkHelper.findObjectName(clientMbs,"de.b4sh.byter","Controller");
                //save mbeans
                connectionStorage.put(connector, new JmxConnectionStorage(nmOn, mbs));
            }

            //set server parameters
            JmxServerHelper.setNetworkManagerFileName(serverMbs,serverNetworkManager,rc.getTestName());
            JmxServerHelper.setMeasurementVolume(serverMbs,serverNetworkManager,rc.getMeasurementVolume());
            JmxServerHelper.setByteTargetForTest(serverMbs,serverNetworkManager,cc.getTransmitTarget());
            //check if test, if yes deactivate file-removal
            if(this.junitRun){
                log.log(Level.INFO, "JUnit Run. Setting AutomaticFileRemoval to false, so UnitTest can check file size.");
                JmxServerHelper.setAutomaticFileRemoval(serverMbs,serverNetworkManager,false);
            }

            //setting writer buffer to server
            if(sc.getWriteBufferSize() > 0)
                JmxServerHelper.setNetworkManagerWriterBufferSize(serverMbs,serverNetworkManager,sc.getWriteBufferSize());
            else{
                log.log(Level.WARNING, CommanderWorkflowError.WRITERBUFFERSIZE_BELOW_ZERO.getReason());
                this.errorLogConfiguration(rc.getTestName(), "Writerbuffer < 0");
                continue;
            }
            //setting network buffer to server
            if(sc.getNetworkBufferSize() > 0)
                JmxServerHelper.setNetworkManagerNetworkBufferSize(serverMbs,serverNetworkManager,sc.getNetworkBufferSize());
            else{
                log.log(Level.WARNING, CommanderWorkflowError.NETWORKBUFFERSIZE_BELOW_ZERO.getReason());
                this.errorLogConfiguration(rc.getTestName(), "Networkbuffer < 0");
                continue;
            }
            //transmit path
            //old commander side resolved relative path: https://gist.github.com/deB4SH/6268c894d97ad4fb2cefd16a05c49f35
            JmxServerHelper.setNetworkManagerFilePath(serverMbs,serverNetworkManager,sc.getFilePath());
            //check for network implementation
            if(isNetworkImplementationAvailable(sc.getNetworkImplementation()))
                JmxServerHelper.setNetworkManagerNetworkType(serverMbs,serverNetworkManager,sc.getNetworkImplementation());
            else{
                log.log(Level.WARNING, CommanderWorkflowError.NETWORK_IMPLEMENTATION_UNKNOWN.getReason());
                this.errorLogConfiguration(rc.getTestName(), "Unkown Writer Implementation");
                continue;
            }
            //check for writer implementation
            if(isWriterImplementationAvailable(sc.getWriteImplementation()))
                JmxServerHelper.setNetworkManagerWriterType(serverMbs,serverNetworkManager,sc.getWriteImplementation());
            else{
                log.log(Level.WARNING,CommanderWorkflowError.WRITER_IMPLEMENTATION_UNKNOWN.getReason());
                this.errorLogConfiguration(rc.getTestName(), "Unkown Network Implementation");
                continue;
            }
            //rebuild evaluation folder
            JmxServerHelper.rebuildEvaluationFolder(serverMbs,serverNetworkManager);
            //start server
            JmxServerHelper.startNetworkManagerDirectStoreHandler(serverMbs,serverNetworkManager);
            //wait a bit
            while(!JmxServerHelper.getServerSocketAccepting(serverMbs,serverNetworkManager)){
                log.log(Level.INFO,"server socket not ready yet!");
                nap(500);
            }
            //set parameters to clients
            for(JMXConnector connector: jmxConnectors){
                //server specific data
                final MBeanServerConnection mbs = connectionStorage.get(connector).getMbs();
                final ObjectName nmOn = connectionStorage.get(connector).getOn();
                //set general parameter
                JmxClientNetworkHelper.setTakeMeasurements(mbs,nmOn,true);
                JmxClientNetworkHelper.setMeasurementVolume(mbs,nmOn,rc.getMeasurementVolume());
                JmxClientNetworkHelper.setTestName(mbs,nmOn,rc.getTestName());
                //update environment stuff on client
                JmxClientNetworkHelper.changeNetworkBufferSize(mbs,nmOn,cc.getBufferSize());
                JmxClientNetworkHelper.changePregenChunkSize(mbs,nmOn,cc.getPregeneratedChunkSize());
                JmxClientNetworkHelper.changeServerHostAddress(mbs,nmOn,sc.getServerJmxHost());
                final int serverSocketPort = JmxServerHelper.getNetworkManagerServerSocketPort(serverMbs,serverNetworkManager);
                JmxClientNetworkHelper.changeServerHostPort(mbs,nmOn,serverSocketPort);
                JmxClientNetworkHelper.changeTransmitTarget(mbs,nmOn,cc.getTransmitTarget());
                //rebuild evaluation folder
                JmxClientNetworkHelper.rebuildEvaluationFolder(mbs,nmOn);
            }
            //start clients
            for(JMXConnector connector: jmxConnectors) {
                //server specific data
                final MBeanServerConnection mbs = connectionStorage.get(connector).getMbs();
                final ObjectName nmOn = connectionStorage.get(connector).getOn();
                JmxClientNetworkHelper.startPlainNetwork(mbs,nmOn);
            }
            int fulfillmentCounter = 0;
            //check if the client is done with his job
            while(fulfillmentCounter != jmxConnectors.size()){
                for(JMXConnector connector: jmxConnectors) {
                    //server specific data
                    final MBeanServerConnection mbs = connectionStorage.get(connector).getMbs();
                    final ObjectName nmOn = connectionStorage.get(connector).getOn();
                    if (JmxClientNetworkHelper.getTaskFulfilled(mbs, nmOn)) {
                        fulfillmentCounter++;
                    }
                }
                log.log(Level.FINEST, "Client " + connector.getAddress().toString() + " not done yet.");
                nap(500);
            }
            log.log(Level.INFO, "Client transmitted all required data on test: " + rc.getTestName() + ". Now checking if Server-Store is done!");
            boolean isServerDone = false;
            while(!isServerDone){
                isServerDone = JmxServerHelper.isCurrentStoreDone(serverMbs,serverNetworkManager);
                log.log(Level.FINEST, "Server " + sc.getServerJmxHost() +":" + sc.getServerJmxPort() +" not done yet.");
                nap(500);
            }
            log.log(Level.INFO, "Server wrote all required data to disc. Shut down system for test " + rc.getTestName() + " now.");
            //shutdown server
            JmxServerHelper.shutdownHandler(serverMbs,serverNetworkManager);
            //wait for the server to shut down
            while(JmxServerHelper.getNetworkManagerServerSocketPort(serverMbs,serverNetworkManager) != -1){
                log.log(Level.INFO, "server is not ready yet for a new test set. please wait");
                nap(500);
            }
        }
    }

    /**
     * Log a error message that the current test is skipped due to a error.
     * @param testName
     */
    private void errorLogConfiguration(final String testName){
        log.log(Level.WARNING, "Skipping Test " + testName + " due to an error. Check the logger backlog fore further details.");
    }

    private void errorLogConfiguration(final String testName, final String reason){
        log.log(Level.WARNING, "Skipping Test " + testName + " due to an error. Reason: " + reason + ". Please conside to read the backlog of this logger.");
    }

    /**
     * checks if the given implementation is available in NetworkType Enum.
     * @param impl implementation to check
     * @return true (available) | false (not available)
     */
    private boolean isNetworkImplementationAvailable(final String impl){
        return NetworkType.isImplementationAvailable(impl);
    }

    /**
     * checks if the given implementation is available in WriterType enum.
     * @param impl implementation to check
     * @return true (available) | false (not available)
     */
    private boolean isWriterImplementationAvailable(final String impl){
        return WriterType.isImplementationAvailable(impl);
    }

    /**
     * construct a jmx connector without login data.
     * @param cc client configuration to use
     * @return connected JmxConnector | null
     */
    private JMXConnector buildCLientConnector(final ClientConnection cc){
        try{
            return JmxConnectionHelper.buildJmxMPConnector(cc.getClientJmxHost(),cc.getClientJmxPort());
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during building JMXConnector to Client.");
            return null;
        }
    }

    /**
     * construct a jmx connector with login data.
     * @param cc client configuration to use
     * @return connected JmxConnector | null
     */
    private JMXConnector buildCLientConnectorWithLogin(final ClientConnection cc){
        try{
            return JmxConnectionHelper.buildJmxMPConnector(cc.getClientJmxHost(),cc.getClientJmxPort(),
                    cc.getUsername(),cc.getPassword());
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during building JMXConnector to Client.");
            return null;
        }
    }

    /**
     * construct a jmx connector without login data.
     * @param sc server configuration to use
     * @return connected JmxConnector | null
     */
    private JMXConnector buildServerConnector(final ServerConfiguration sc){
        try{
            return JmxConnectionHelper.buildJmxMPConnector(sc.getServerJmxHost(),sc.getServerJmxPort());
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during building JMXConnector to Server.");
            return null;
        }
    }

    /**
     * construct a jmx connector with login data.
     * @param sc server configuration to use
     * @return connected JMXConnector | null
     */
    private JMXConnector buildServerConnectorWithLogin(final ServerConfiguration sc){
        try{
            return JmxConnectionHelper.buildJmxMPConnector(sc.getServerJmxHost(),sc.getServerJmxPort(),
                    sc.getUsername(),sc.getPassword());
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during building JMXConnector to Server.");
            return null;
        }
    }

    private JMXConnector buildTestEnvironmentConnector(final int port){
        try{
            return JmxConnectionHelper.buildJmxMPConnector("127.0.0.1",port);
        } catch (IOException e) {
            log.log(Level.WARNING,"IO Exception during building JMXConnector to Server.");
            return null;
        }
    }

    /**
     * update variables on the test environment.
     * @param sc the new ServerConfiguration
     * @param cc the new ClientConfiguration
     */
    private void setTestEnvironMentVariables(final ServerConfiguration sc, final ClientConfiguration cc){
        commanderTestEnvironment.setClientNetworkBufferSize(cc.getBufferSize());
        commanderTestEnvironment.setClientNetworkImplementation(cc.getIoImplementation());
        commanderTestEnvironment.setClientPregeneratedArraySize(cc.getPregeneratedChunkSize());
        commanderTestEnvironment.setServerNetworkBufferSize(sc.getNetworkBufferSize());
        commanderTestEnvironment.setServerNetworkImplementation(sc.getNetworkImplementation());
        commanderTestEnvironment.setServerWriterBufferSize(sc.getWriteBufferSize());
        commanderTestEnvironment.setServerWriterImplementation(sc.getWriteImplementation());
    }

    /**
     * build the jmx object for the test environment.
     */
    private void buildJmxTestEnvironment(){
        commanderTestEnvironment = new CommanderJmxTestEnvironment("de.b4sh.byter","TestEnvironment");
        this.mBeanHelper.registerElement(commanderTestEnvironment,commanderTestEnvironment.getObjectName());
    }

    /**
     * build the jmx object for the credentials.
     */
    private void buildJmxCredentials(){
        commanderServerCredentials = new CommanderJmxCredentials("de.b4sh.byter","ServerCredentials");
        this.mBeanHelper.registerElement(commanderServerCredentials,commanderServerCredentials.getObjectName());
        commanderClientCredentials = new CommanderJmxCredentials("de.b4sh.byter","ClientCredentials");
        this.mBeanHelper.registerElement(commanderClientCredentials,commanderClientCredentials.getObjectName());
    }

    /**
     * build the jmx object for the controller.
     * mainly to shutdown the commander.
     */
    private void buildJmxController(){
        commanderController = new CommanderJmxController("de.b4sh.byter","Controller",this);
        this.mBeanHelper.registerElement(commanderController,commanderController.getObjectName());
    }

    /**
     * build plain controller.
     */
    private void buildJmxConnector(){
        try {
            this.connector = JmxConnectionHelper.buildAndStartJmxConnector(this.parameter.jmxPort,10,this.mbs);
            this.connectorPort = this.connector.getAddress().getPort();
            log.log(Level.INFO,"Started JMX-Service on port: " + this.connector.getAddress().getPort());
        } catch (IOException e) {
            log.log(Level.WARNING, "IO Exception during creating JmxConnector in Commander. Check StackTrace for issues.",e);
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
     * get the port of the connector service.
     * @return int with the currently used port
     */
    public int getConnectorSystemPort() {
        return connectorPort;
    }

    /**
     * Let the Thread sleep a bit.
     * @param napTime time to sleep in ms
     */
    private static void nap(final long napTime){
        try {
            Thread.sleep(napTime);
        } catch (final InterruptedException e) {
            log.log(Level.WARNING,"Exception during nap(). See Stacktrace for issue please",e);
        }
    }

    /**
     * Set test run flags.
     * @param serverPort server port that is used
     * @param clientPort client port that is used
     */
    public void setTest(final int serverPort, final int clientPort) {
        this.junitRun = true;
        this.serverPort = serverPort;
        this.clientPort = clientPort;
    }

    /**
     * Unset the test run flags.
     */
    public void unsetTest(){
        this.junitRun = false;
    }

    /**
     * Class to keep a ObjectName and corresponding MBeanServerConnection after setting up every parameter.
     * Reduce the need to search for these objects again. Mainly to start the test scenario afterwards.
     */
    final class JmxConnectionStorage{
        final ObjectName on;
        final MBeanServerConnection mbs;

        JmxConnectionStorage(final ObjectName on, final MBeanServerConnection mbs) {
            this.on = on;
            this.mbs = mbs;
        }

        public ObjectName getOn() {
            return on;
        }

        public MBeanServerConnection getMbs() {
            return mbs;
        }
    }
}
