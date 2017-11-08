package de.b4sh.byter.utils.exception;

/**
 * Error Enum for errors inside the server network.
 */
public enum ServerNetworkError {
    IO_EXCEPTION("[CRITICAL]: IO EXCEPTION IN SOCKET HANDLING. SEE STACKTRACE FOR MORE INFORMATION"),
    SOCKET_CLOSED("SOCKET CLOSED EXCEPTION IN NETWORK IMPLEMENTATION RUN METHOD. THIS COULD HAPPEN IN A SCAN OR WRONG NETWORK IO.");

    private final String reason;

    ServerNetworkError(final String reason) {
        this.reason = reason;
    }

    /**
     * Get the current required reason for a failure.
     * @return String with the error text.
     */
    public String getReason() {
        return reason;
    }
}
