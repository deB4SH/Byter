package de.b4sh.byter.commander.config;

import java.util.List;

/**
 * POJO Class for Client Configurations.
 */
public final class ClientConfiguration {

    private final List<ClientConnection> clients;
    private final int bufferSize;
    private final String ioImplementation;
    private final int pregeneratedChunkSize;
    private final long transmitTarget;

    /**
     * Constructor.
     * @param clients list of clients to connect to
     * @param bufferSize the size of the buffer
     * @param ioImplementation network implementation to use
     * @param pregeneratedChunkSize the size of the pre-generated chunk
     * @param transmitTarget how many data should be transmitted
     */
    public ClientConfiguration(final List<ClientConnection> clients, final int bufferSize,
                               final String ioImplementation, final int pregeneratedChunkSize,
                               final long transmitTarget) {
        this.clients = clients;
        this.bufferSize = bufferSize;
        this.ioImplementation = ioImplementation;
        this.pregeneratedChunkSize = pregeneratedChunkSize;
        this.transmitTarget = transmitTarget;
    }

    /**
     * Get all clients that should be available or connected to.
     * @return client list.
     */
    public List<ClientConnection> getClients() {
        return clients;
    }

    /**
     * Get the set network buffer size.
     * @return int
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Get the set network implementation.
     * @return String
     */
    public String getIoImplementation() {
        return ioImplementation;
    }

    /**
     * Get the set size for the pre generated chunk size.
     * @return int
     */
    public int getPregeneratedChunkSize() {
        return pregeneratedChunkSize;
    }

    /**
     * get the target of data that should be transmitted.
     * @return transmitTarget as long
     */
    public long getTransmitTarget() {
        return transmitTarget;
    }
}
