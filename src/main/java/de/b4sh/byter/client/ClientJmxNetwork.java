package de.b4sh.byter.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import com.udojava.jmx.wrapper.JMXBeanParameter;
import de.b4sh.byter.client.network.NetworkClientInterface;
import de.b4sh.byter.client.network.PlainNetworkClient;
import de.b4sh.byter.utils.io.FileManager;
import de.b4sh.byter.utils.jmx.JmxEntity;
import de.b4sh.byter.utils.measurements.PerformanceTimer;

/**
 * Client Network Controller Class.
 */
@JMXBean(description = "Byter.Client.Network")
public final class ClientJmxNetwork extends JmxEntity {

    private static final Logger log = Logger.getLogger(ClientJmxNetwork.class.getName());
    private String serverIp;
    private int serverPort;
    private int pregeneratedChunkSize;
    private long transmitSize;
    private int networkBufferSize;
    private boolean takeMeasurements;
    private int measurementVolume;
    private String testName;

    //Implementation and Pool
    private ExecutorService networkService;
    private NetworkClientInterface nci;

    /**
     * public constructor with base initialisation.
     * @param packageName the packageName of the entity
     * @param type the desired type
     */
    ClientJmxNetwork(final String packageName, final String type) {
        super(packageName, type);
        this.serverIp = "localhost";
        this.serverPort = 0;
        this.pregeneratedChunkSize = 8192;
        this.transmitSize = 8192;
        this.networkBufferSize = 8192;
        this.takeMeasurements = true;
        this.measurementVolume = 0;
        this.networkService = Executors.newFixedThreadPool(1);
        this.nci = null;
    }

    /**
     * Rebuild the evaluation folder.
     * Setting a new output directory for the evaluation data.     *
     */
    @JMXBeanOperation(name = "RebuildEvaluationFolder", description = "rebuild the evaluation folder")
    public void rebuildEvaluationFolder(){
        FileManager.rebuildEvaluationFolder(this.testName);
    }

    /**
     * Get the current TestName.
     * @return String with TestName
     */
    @JMXBeanAttribute(name = "TestName", description = "the current set test name")
    public String getTestName(){
        return this.testName;
    }

    /**
     * Set a new TestName.
     * @param param new test name to set as String
     */
    @JMXBeanOperation(name = "setTestName", description = "set a new test name")
    public void setTestName(
        @JMXBeanParameter(name = "parameter", description = "new testname to use")final String param
    ){
        this.testName = String.valueOf(param);
    }

    /**
     * Get the current set measurement volume size.
     * The amount of data that should be acquired.
     * @return int with the amount
     */
    @JMXBeanAttribute(name = "MeasurementVolume", description = "amount of data to track")
    public int getMeasurementVolume(){
        return this.measurementVolume;
    }

    /**
     * Set a new measurement volume.
     * Defines how many tracking points should be acquired.
     * @param param amount to use
     */
    @JMXBeanOperation(name = "setMeasurementVolume", description = "set a new amount of measurements to take")
    public void setMeasurementVolume(
        @JMXBeanParameter(name = "parameter", description = "the new amount of data to pass")final int param
    ){
        if(param >= 0)
            this.measurementVolume = param;
    }

    /**
     * Get the current  flag if the application should take measurements in the next test-run.
     * @return flag to use (true|false)
     */
    @JMXBeanAttribute(name = "TakeMeasurements", description = "flag if measurements should be created on the client side")
    public boolean getMeasurementFlag(){
        return this.takeMeasurements;
    }

    /**
     * Set a new flag if the application should take measurements in the next test-run.
     * @param param flag to use as String (jmx invoke transmission)
     */
    @JMXBeanOperation(name ="setTakeMeasurements", description = "set a new flag for taking measurements")
    public void setTakeMeasurements(
        @JMXBeanParameter(name = "parameter", description = "the new flag to pass")final String param
    ){
        final String flag = String.valueOf(param);
        if("true".equals(flag))
            this.takeMeasurements = true;
        else if("false".equals(flag))
            this.takeMeasurements = false;
    }

    /**
     * Get the current set ServerIp.
     * @return hostname of the server or ip as String
     */
    @JMXBeanAttribute(name = "ServerIp", description = "currently set server ip")
    public String getServerIp(){
        return this.serverIp;
    }

    /**
     * Get the current set ServerPort.
     * @return port as int
     */
    @JMXBeanAttribute(name = "ServerPort", description = "currently set server port")
    public int getServerPort(){
        return this.serverPort;
    }

    /**
     * Get the current status of the ExecutorService.
     * Checks if there is a submitted task running.
     * @return status as boolean
     */
    @JMXBeanAttribute(name = "ExecutorStatus", description = "status of the executor service - true if the submitted task is completed and shut down")
    public boolean getExecutorServiceStatus(){
        return this.networkService.isTerminated();
    }

    /**
     * Get the current set target size of the pregenerated chunk array.
     * @return int with the targeted size
     */
    @JMXBeanAttribute(name = "TargetPregenChunkSize", description = "the targeted pregenerated chunk size")
    public int getTargetPregeneratedChunkSize(){
        return this.pregeneratedChunkSize;
    }

    /**
     * Get the actually generated chunk size.
     * @return get the size of the actually generated chunk. -1 if no active implementation running
     */
    @JMXBeanAttribute(name = "ActualGeneratedChunkSize", description = "generated chunk size on network client")
    public int getActualGeneratedChunkSize(){
        if(nci != null)
            return nci.getActualChunkSize();
        else
            return -1;
    }

    /**
     * Get the current active transmission target. How many sweet bytes should be transmitted to the server.
     * @return long
     */
    @JMXBeanAttribute(name = "TransmitTarget", description = "target of payload that should be transmitted to the server")
    public long getTransmitSize(){
        return this.transmitSize;
    }

    /**
     * Set a new measurement state.
     * @param param1 flag to use (false|true)
     */
    @JMXBeanOperation(name = "NewMeasurementState", description = "set a new measurementstate")
    public void newMeasurementState(
        @JMXBeanParameter(name ="measurementState", description = "new measurement state")final String param1
    ){
        final String parameter = String.valueOf(param1); //convert to a real string.
        if("true".equals(parameter))
            this.takeMeasurements = true;
        else if("false".equals(parameter))
            this.takeMeasurements = false;
        log.log(Level.INFO,"ClientJmxNetwork \n NewMeasurementState got a wrong state passed. Passed state: "
                                    + parameter + " | current state: " + this.takeMeasurements);
    }

    /**
     * Set a new ServerIP or hostname.
     * @param param1 new ip or hostname
     *               eg. "127.0.0.1" or "localhost" or "google.de" ;-)
     */
    @JMXBeanOperation(name = "NewServerIp",description = "set a new server ip")
    public void newServerIp(
        @JMXBeanParameter(name = "ip",description = "new ip")final String param1
    ){
        this.serverIp = String.valueOf(param1).toLowerCase(); //yes this is needed, the parameter is not a 'real' String
    }

    /**
     * Set a new ServerPort.
     * @param param1 port to use.
     */
    @JMXBeanOperation(name = "NewServerPort", description = "set a new server port")
    public void newServerPort(
        @JMXBeanParameter(name = "port", description = "new port")final int param1
    ){
        if(param1 > 0)
            this.serverPort = param1;
    }

    /**
     * Set a new transmission target to fulfill.
     * @param param1 amount of bytes that should be transmitted to target
     */
    @JMXBeanOperation(name = "NewTransmitTarget", description = "set a new transmit target")
    public void newTransmitTarget(
        @JMXBeanParameter(name = "transmitTarget", description = "new target to hit")final long param1
    ){
        if(param1 > 0)
            this.transmitSize = param1;
    }

    /**
     * Set a new size for the pregrenerated chunk.
     * @param param1 new chunk size as int
     */
    @JMXBeanOperation(name = "NewPregenChunkSize", description = "set a new size for the pregenerated data")
    public void newPregenChunkSize(
        @JMXBeanParameter(name = "pregenChunk", description = "new pregen size")final int param1
    ){
        if(param1 > 0)
            this.pregeneratedChunkSize = param1;
    }

    /**
     * Start a Plain Network Implementation that sends the information over a single socket in a synchrony way.
     * One block at a time. One after the other.
     */
    @JMXBeanOperation(name = "StartPlainNetwork", description = "starts a new plain network client")
    public void startPlainNetwork(){
        if(nci != null){
            if(nci.isRunning())
                log.log(Level.WARNING,"There is a running Task and you want to start a second one?");
            else{
                if(this.takeMeasurements){
                    final int measurementSteps = PerformanceTimer.calculateSkippedMeasurements(this.transmitSize,this.measurementVolume,this.networkBufferSize);
                    final PerformanceTimer pt = new PerformanceTimer("ClientJmxNetwork-PlainNetworkClient",this.measurementVolume,measurementSteps);
                    nci = new PlainNetworkClient(this.serverIp,this.serverPort,this.networkBufferSize,this.pregeneratedChunkSize,this.transmitSize,pt,testName);
                }else{
                    nci = new PlainNetworkClient(this.serverIp,this.serverPort,this.networkBufferSize,this.pregeneratedChunkSize,this.transmitSize,testName);
                }
            }
        }else{
            if(this.takeMeasurements){
                final PerformanceTimer pt = new PerformanceTimer("ClientJmxNetwork-PlainNetworkClient");
                nci = new PlainNetworkClient(this.serverIp,this.serverPort,this.networkBufferSize,this.pregeneratedChunkSize,this.transmitSize,pt,testName);
            }else{
                nci = new PlainNetworkClient(this.serverIp,this.serverPort,this.networkBufferSize,this.pregeneratedChunkSize,this.transmitSize,testName);
            }
        }
    }

    /**
     * Get the currently set network buffer size for a implementation.
     * @return int with the current set desired buffer size.
     */
    @JMXBeanAttribute(name = "NetworkBufferSize", description = "size of the network buffer")
    public int getNetworkBufferSize(){
        return this.networkBufferSize;
    }

    /**
     * Set a new buffer size for the network implementation.
     * @param param1 new buffer size as int
     */
    @JMXBeanOperation(name = "changeNetworkBufferSize", description = "change the size of the used network buffer")
    public void changeNetworkBufferSize(
        @JMXBeanParameter(name = "newBuffer", description = "the new buffer size")final int param1
    ){
        //negativ and null check
        if(param1 <= 0)
            return;
        //do change
        this.networkBufferSize = param1;
    }

    /**
     * Get the current status of the running task (if there is one running).
     * True if the process is done and the system is ready to get a new one.
     * @return true | false
     */
    @JMXBeanAttribute(name = "LastTaskFulfilled", description = "check if the last task is fulfilled")
    public boolean getLastTaskFulfilled(){
        //check if the task is still running
        if(this.nci != null)
            return !this.nci.isRunning();
        else
            return true;
    }
}
