package de.b4sh.byter.commander.config;

/**
 * Root Configuration that combines server and client configurations.
 */
public final class DirectConfiguration {

    private final String testName;
    private final ClientConfiguration clientConfiguration;
    private final int measurementCount;
    private final int writerCount;
    private final String writeFilePath;

    /**
     * Constructor for NetworkConfiguration.
     * @param testName the corresponding test name
     * @param clientConfiguration client configuration
     * @param measurementCount the count of how many measurements should be taken
     * @param writeFilePath path to write test files to
     * @param writerCount how many writer should be started
     */
    public DirectConfiguration(final String testName,
                               final ClientConfiguration clientConfiguration,
                               final int measurementCount,
                               final int writerCount,
                               final String writeFilePath) {
        this.testName = testName;
        this.clientConfiguration = clientConfiguration;
        this.measurementCount = measurementCount;
        this.writerCount = writerCount;
        this.writeFilePath = writeFilePath;
    }

    /**
     * Get the name of this test case.
     * @return String
     */
    public String getTestName() {
        return testName;
    }

    /**
     * get the configuration for the client side.
     * @return ClientConfiguration
     */
    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    /**
     * Get the count of measurements that should be taken in the test.
     * @return int with the count
     */
    public int getMeasurementCount() {
        return measurementCount;
    }

    /**
     * Get the current set write file path.
     * @return path to write data to
     */
    public String getWriteFilePath() {
        return writeFilePath;
    }

    /**
     * get the desired writer count.
     * @return writer to instance as int
     */
    public int getWriterCount() {
        return writerCount;
    }
}
