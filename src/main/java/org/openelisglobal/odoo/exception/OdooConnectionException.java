package org.openelisglobal.odoo.exception;

/**
 * Exception thrown when there is a problem connecting to the Odoo server.
 * This exception is typically thrown when:
 * - The Odoo server is unreachable
 * - Authentication fails
 * - The connection times out
 * - The server returns an error during connection
 *
 * @author OpenELIS
 */
public class OdooConnectionException extends RuntimeException {
    
    /**
     * Constructs a new OdooConnectionException with the specified detail message.
     *
     * @param message The detail message explaining the connection error
     */
    public OdooConnectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new OdooConnectionException with the specified detail message and cause.
     *
     * @param message The detail message explaining the connection error
     * @param cause The cause of the connection error
     */
    public OdooConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
