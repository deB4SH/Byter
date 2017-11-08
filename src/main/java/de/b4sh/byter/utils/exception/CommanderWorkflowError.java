package de.b4sh.byter.utils.exception;

/**
 * Error Enum for the commander work flow.
 */
public enum CommanderWorkflowError {
    NETWORK_IMPLEMENTATION_UNKNOWN("the requested network implementation is unknown. please choose an another"),
    WRITER_IMPLEMENTATION_UNKNOWN("the requested writer implementation is known. please choose an another"),
    WRITERBUFFERSIZE_BELOW_ZERO("the writer buffer size is below or zero"),
    NETWORKBUFFERSIZE_BELOW_ZERO("the network buffer size is below or zero"),
    IO_EXCEPTION("IO Exception from outer space. Please check the Stacktrace for more information.");

    private final String reason;

    CommanderWorkflowError(final String reason) {
        this.reason = reason;
    }

    /**
     * Get the current required reason for a failure.
     * @return String with the error text.
     */
    public String getReason() {
        return "[COMMANDER-WORKFLOW ERROR]\n" + reason;
    }
}
