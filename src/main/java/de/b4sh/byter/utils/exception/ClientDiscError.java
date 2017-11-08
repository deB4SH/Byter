/*
 * File: ClientDiscError
 * Project: Byter
 * Author: deB4SH
 * First-Created: 2017-08-29
 * Type: Class
 */
package de.b4sh.byter.utils.exception;

/**
 * Error Enum for Client Disc.
 */
public enum ClientDiscError {
    NO_WRITER_SELECTED("[CRITICAL]: NO WRITER SELECTED");

    private final String reason;

    ClientDiscError(final String reason) {
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
