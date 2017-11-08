package de.b4sh.byter.commander.config;

import de.b4sh.byter.server.network.NetworkType;
import de.b4sh.byter.utils.writer.WriterType;

/**
 * POJO Class for configuration serialization.
 */
public final class ServerConfiguration {

    private final String serverJmxHost;
    private final int serverJmxPort;
    private final String username;
    private final String password;
    private final String writeImplementation;
    private final int writeBufferSize;
    private final String networkImplementation;
    private final int networkBufferSize;
    private final String filePath;

    /**
     * Constructor with all needed params.
     * @param serverJmxHost host address of server jmx
     * @param serverJmxPort post of server jmx
     * @param username username to connect to jmx
     * @param password password to connect to jmx
     * @param writeImplementation which writer implementation to choose
     * @param writeBufferSize which buffer size of writer
     * @param networkImplementation which network implementation to choose
     * @param networkBufferSize which network buffer size
     * @param filePath where to save data to
     */
    public ServerConfiguration(final String serverJmxHost, final int serverJmxPort,
                               final String username, final String password, final String writeImplementation,
                               final int writeBufferSize, final String networkImplementation,
                               final int networkBufferSize, final String filePath) {
        this.serverJmxHost = serverJmxHost;
        this.serverJmxPort = serverJmxPort;
        this.username = username;
        this.password = password;
        this.writeImplementation = writeImplementation;
        this.writeBufferSize = writeBufferSize;
        this.networkImplementation = networkImplementation;
        this.networkBufferSize = networkBufferSize;
        this.filePath = filePath;
    }

    /**
     * get current set server jmx host address.
     * @return String
     */
    public String getServerJmxHost() {
        return serverJmxHost;
    }

    /**
     * get current set server jmx port.
     * @return int
     */
    public int getServerJmxPort() {
        return serverJmxPort;
    }

    /**
     * get current set writer implementation.
     * @return String
     */
    public String getWriteImplementation() {
        return writeImplementation;
    }

    /**
     * get writer type based on set implementation.
     * @return corresponding writeType | null (default)
     */
    public WriterType getWriteType(){
        for(WriterType wt: WriterType.values()){
            if(writeImplementation.equals(wt.getKey())){
                return wt;
            }
        }
        return null;
    }

    /**
     * get current write buffer size.
     * @return int
     */
    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    /**
     * get set network implementation.
     * @return String
     */
    public String getNetworkImplementation() {
        return networkImplementation;
    }

    /**
     * get network type based on set implementation.
     * @return network type | null (default)
     */
    public NetworkType getNetworkType(){
        for(NetworkType nt: NetworkType.values()){
            if(networkImplementation.equals(nt.getKey())){
                return nt;
            }
        }
        return null;
    }

    /**
     * get network buffer size.
     * @return int
     */
    public int getNetworkBufferSize() {
        return networkBufferSize;
    }

    /**
     * get the filepath where the data should be saved to.
     * @return String
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * get the username needed to connect to jmx.
     * @return String
     */
    public String getUsername() {
        return username;
    }

    /**
     * get the username needed to connect to jmx.
     * @return String
     */
    public String getPassword() {
        return password;
    }
}
