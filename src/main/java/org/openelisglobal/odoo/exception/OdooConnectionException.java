package org.openelisglobal.odoo.exception;

/**
 * Exception thrown when there is a problem connecting to the Odoo server. This
 * exception is typically thrown when: - The Odoo server is unreachable -
 * Authentication fails - The connection times out - The server returns an error
 * during connection
 */
public class OdooConnectionException extends RuntimeException {

    /**
     * Constructs a new OdooConnectionException with the specified detail message.
     */
    public OdooConnectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new OdooConnectionException with the specified detail message
     * and cause.
     */
    public OdooConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
