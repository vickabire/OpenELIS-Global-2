package org.openelisglobal.odoo.exception;

/**
 * Exception thrown when an operation on the Odoo server fails.
 * This exception is typically thrown when:
 * - Creating or updating records fails
 * - Invalid data is provided for an operation
 * - The server returns an error during an operation
 * - Required fields are missing
 * - Business rules are violated
 *
 * @author OpenELIS
 */
public class OdooOperationException extends RuntimeException {
    
    /**
     * Constructs a new OdooOperationException with the specified detail message.
     *
     * @param message The detail message explaining the operation error
     */
    public OdooOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new OdooOperationException with the specified detail message and cause.
     *
     * @param message The detail message explaining the operation error
     * @param cause The cause of the operation error
     */
    public OdooOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
