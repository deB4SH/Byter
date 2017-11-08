/*
 * File: ConnectionData
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-09
 * Type: Class
 */
package de.b4sh.byter.configurationGenerator;

/**
 * Class for ConnectionData.
 */
public final class ConnectionData {

    private final String serverIp;
    private final int serverPort;
    private final String clientIp;
    private final int clientPort;
    private final String path;

    /**
     * Constructor for ConnectionData.
     * @param serverIp serverip to use
     * @param serverPort serverport to use
     * @param clientIp clientip to use
     * @param clientPort clientport to use
     * @param path path to use
     */
    public ConnectionData(final String serverIp, final int serverPort, final String clientIp, final int clientPort, String path) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.path = path;
    }

    /**
     * get current set path.
     * @return path.
     */
    public String getPath() {
        return path;
    }

    /**
     * get current set server ip.
     * @return host address.
     */
    public String getServerIp() {
        return serverIp;
    }

    /**
     * get current set server port.
     * @return port
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * get current set client ip.
     * @return client address
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * get current set client port.
     * @return port.
     */
    public int getClientPort() {
        return clientPort;
    }
}
