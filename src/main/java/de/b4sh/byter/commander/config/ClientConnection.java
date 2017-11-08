/*
 * File: ClientConnection
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-10-06
 * Type: Class
 */
package de.b4sh.byter.commander.config;

/**
 * POJO Class for client connection parameter.
 */
public final class ClientConnection {

    private final String clientJmxHost;
    private final int clientJmxPort;
    private final String username;
    private final String password;

    /**
     * Client Connection Data Class.
     * @param clientJmxHost client jmx host address
     * @param clientJmxPort client jmx host port
     * @param username username to login
     * @param password password to login
     */
    public ClientConnection(final String clientJmxHost, final int clientJmxPort,
                            final String username, final String password) {
        this.clientJmxHost = clientJmxHost;
        this.clientJmxPort = clientJmxPort;
        this.username = username;
        this.password = password;
    }

    /**
     * get the current set client jmx host.
     * @return host address
     */
    public String getClientJmxHost() {
        return clientJmxHost;
    }

    /**
     * get the current set client jmx port.
     * @return port
     */
    public int getClientJmxPort() {
        return clientJmxPort;
    }

    /**
     * get the current set username.
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * get the current set password.
     * @return password.
     */
    public String getPassword() {
        return password;
    }
}
