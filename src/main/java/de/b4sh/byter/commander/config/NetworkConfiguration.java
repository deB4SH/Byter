package de.b4sh.byter.commander.config;

/**
 * Root Configuration that combines server and client configurations.
 */
public final class NetworkConfiguration {

    private final String testName;
    private final ServerConfiguration serverConfiguration;
    private final ClientConfiguration clientConfiguration;
    private final int measurementVolume;

    /**
     * Constructor for NetworkConfiguration.
     * @param testName the corresponding test name
     * @param serverConfiguration server configuration
     * @param clientConfiguration client configuration
     * @param measurementVolume how many measurements should be taken
     */
    public NetworkConfiguration(final String testName,
                                final ServerConfiguration serverConfiguration,
                                final ClientConfiguration clientConfiguration,
                                final int measurementVolume) {
        this.testName = testName;
        this.serverConfiguration = serverConfiguration;
        this.clientConfiguration = clientConfiguration;
        this.measurementVolume = measurementVolume;

    }

    /**
     * Get the name of this test case.
     * @return String
     */
    public String getTestName() {
        return testName;
    }

    /**
     * get the configuration for the server side.
     * @return ServerConfiguration
     */
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    /**
     * get the configuration for the client side.
     * @return ClientConfiguration
     */
    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    /**
     * Get the measurement volume to aquire.
     * @return int with the measurement volume.
     */
    public int getMeasurementVolume() {
        return measurementVolume;
    }
}
