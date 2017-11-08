package de.b4sh.byter.commander;

import java.util.logging.Logger;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;

import de.b4sh.byter.utils.jmx.JmxEntity;

/**
 * Class for announcing the currently running test parameters.
 */
@JMXBean(description = "Byter.Commander.Environment")
public final class CommanderJmxTestEnvironment extends JmxEntity {
    private static final Logger log = Logger.getLogger(CommanderJmxTestEnvironment.class.getName());
    //generic
    private String testName;
    //server
    private String serverWriterImplementation;
    private String serverNetworkImplementation;
    private int serverWriterBufferSize;
    private int serverNetworkBufferSize;
    //client
    private String clientNetworkImplementation;
    private int clientNetworkBufferSize;
    private int clientPregeneratedArraySize;

    /**
     * public constructor with base initialisation.
     * @param packageName set the packagename this controller should be available under
     * @param type set the type this controller should be available under
     */
    public CommanderJmxTestEnvironment(final String packageName, final String type) {
        super(packageName, type);
        this.testName = "null";
        this.serverNetworkImplementation = "null";
        this.serverWriterImplementation = "null";
        this.serverNetworkBufferSize = -1;
        this.serverWriterBufferSize = -1;
        this.clientNetworkBufferSize = -1;
        this.clientPregeneratedArraySize = -1;
        this.clientNetworkImplementation = "null";
    }

    /**
     * Set the current test name.
     * @param testName test name to identify which test is running
     */
    public void setTestName(final String testName) {
        this.testName = testName;
    }

    /**
     * Set a writer implementation which is currently used in this test. server side.
     * @param serverWriterImplementation writer implementation used
     */
    public void setServerWriterImplementation(final String serverWriterImplementation) {
        this.serverWriterImplementation = serverWriterImplementation;
    }

    /**
     * Set a network implementation which is currently used in this test. server side.
     * @param serverNetworkImplementation implementation that is used
     */
    public void setServerNetworkImplementation(final String serverNetworkImplementation) {
        this.serverNetworkImplementation = serverNetworkImplementation;
    }

    /**
     * set the currently used writer buffer size in this test. server side.
     * @param serverWriterBufferSize writer buffer size
     */
    public void setServerWriterBufferSize(final int serverWriterBufferSize) {
        this.serverWriterBufferSize = serverWriterBufferSize;
    }

    /**
     * set the currently used network buffer size in this test. server side.
     * @param serverNetworkBufferSize buffer size
     */
    public void setServerNetworkBufferSize(final int serverNetworkBufferSize) {
        this.serverNetworkBufferSize = serverNetworkBufferSize;
    }

    /**
     * set the currently used network implementation on the client side.
     * @param clientNetworkImplementation network implementation.
     */
    public void setClientNetworkImplementation(final String clientNetworkImplementation) {
        this.clientNetworkImplementation = clientNetworkImplementation;
    }

    /**
     * set the currently network buffer size used at the client side.
     * @param clientNetworkBufferSize buffer size.
     */
    public void setClientNetworkBufferSize(final int clientNetworkBufferSize) {
        this.clientNetworkBufferSize = clientNetworkBufferSize;
    }

    /**
     * set the currently used size of the pre generated array at the client side.
     * @param clientPregeneratedArraySize size of the array.
     */
    public void setClientPregeneratedArraySize(final int clientPregeneratedArraySize) {
        this.clientPregeneratedArraySize = clientPregeneratedArraySize;
    }

    /**
     * receive the currently active test name over jmx.
     * @return String
     */
    @JMXBeanAttribute(name = "TestName", description = "current running test")
    public String getTestName(){
        return this.testName;
    }

    /**
     * receive the currently used writer implementation inside this test at the server side.
     * @return String
     */
    @JMXBeanAttribute(name = "ServerWriterImplementation", description = "current server writer for this test")
    public String getServerWriterImplementation() {
        return serverWriterImplementation;
    }

    /**
     * receive the currently used network implementation inside this test at the server side.
     * @return String
     */
    @JMXBeanAttribute(name = "ServerNetworkImplementation", description = "current server network for this test")
    public String getServerNetworkImplementation() {
        return serverNetworkImplementation;
    }

    /**
     * receive the currently used buffer size of the writer inside this test at the server side.
     * @return int
     */
    @JMXBeanAttribute(name = "ServerWriterBufferSize", description = "current size of the server writer buffer")
    public int getServerWriterBufferSize() {
        return serverWriterBufferSize;
    }

    /**
     * receive the currently used buffer size of the network inside this test at the server side.
     * @return int
     */
    @JMXBeanAttribute(name = "ServerNetworkBufferSize", description = "current size of the server network buffer")
    public int getServerNetworkBufferSize() {
        return serverNetworkBufferSize;
    }

    /**
     * receive the currently used network implementation on the client side.
     * @return String
     */
    @JMXBeanAttribute(name = "CientNetworkImplementation", description = "current client network implementation ")
    public String getClientNetworkImplementation() {
        return clientNetworkImplementation;
    }

    /**
     * receive the currently used network buffer size on the client side.
     * @return int
     */
    @JMXBeanAttribute(name = "ClientNetworkBufferSize", description = "current size of the client network buffer")
    public int getClientNetworkBufferSize() {
        return clientNetworkBufferSize;
    }

    /**
     * receive the currently used size of the pre generated array.
     * @return int
     */
    @JMXBeanAttribute(name = "ClientPregenChunkSize", description = "current size of the pregenerated chunk array")
    public int getClientPregeneratedArraySize() {
        return clientPregeneratedArraySize;
    }
}
