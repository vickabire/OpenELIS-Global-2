package org.openelisglobal.odoo.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.odoo.config.OdooConfig;
import org.openelisglobal.odoo.exception.OdooConnectionException;
import org.openelisglobal.odoo.exception.OdooOperationException;
import org.springframework.stereotype.Service;

/**
 * Client class for interacting with the Odoo XML-RPC API. This class handles
 * all direct communication with the Odoo server, including: - Authentication -
 * Creating and updating orders - Managing customer records - Generating
 * invoices
 * <p>
 * The client uses XML-RPC for communication and maintains a session with the
 * Odoo server. All operations are performed using the authenticated user's
 * credentials.
 *
 * @author OpenELIS
 * @version 1.0.0
 */
@Service
public class OdooClient {
    private final String url;
    private final String db;
    private final String username;
    private final String password;
    private Integer uid;
    private final XmlRpcClient client;
    private final OdooConfig config;

    /**
     * Constructs a new OdooClient with the specified configuration. Initializes the
     * XML-RPC client and sets up the connection parameters.
     *
     * @param config The Odoo configuration containing connection details
     * @throws OdooConnectionException if the URL is malformed
     */
    public OdooClient(OdooConfig config) {
        this.config = config;
        this.url = config.getUrl();
        this.db = config.getDatabase();
        this.username = config.getUsername();
        this.password = config.getPassword();

        XmlRpcClientConfigImpl clientConfig = new XmlRpcClientConfigImpl();
        try {
            clientConfig.setServerURL(new URL(url + "/xmlrpc/2/object"));
        } catch (MalformedURLException e) {
            throw new OdooConnectionException("Invalid Odoo server URL", e);
        }
        this.client = new XmlRpcClient();
        this.client.setConfig(clientConfig);
    }

    /**
     * Establishes a connection to the Odoo server and authenticates the user. This
     * method must be called before performing any operations.
     *
     * @throws OdooConnectionException if the connection or authentication fails
     */
    public void connect() {
        try {
            Object[] params = new Object[] { db, username, password, new HashMap<>() };
            uid = (Integer) client.execute("common.authenticate", params);
        } catch (XmlRpcException e) {
            LogEvent.logError("Failed to connect to Odoo", e);
            throw new OdooConnectionException("Failed to connect to Odoo", e);
        }
    }

    /**
     * Creates a new sales order in Odoo.
     *
     * @param orderData The order data to create
     * @return The ID of the created order
     * @throws OdooOperationException if the order creation fails
     */
    public Integer createOrder(Map<String, Object> orderData) {
        try {
            Object[] params = new Object[] { db, uid, password, config.getModels().getSaleOrder(), "create",
                    orderData };
            return (Integer) client.execute("object.execute_kw", params);
        } catch (XmlRpcException e) {
            LogEvent.logError("Failed to create order in Odoo", e);
            throw new OdooOperationException("Failed to create order in Odoo", e);
        }
    }

    /**
     * Updates an existing sales order in Odoo.
     *
     * @param orderId   The ID of the order to update
     * @param orderData The updated order data
     * @throws OdooOperationException if the order update fails
     */
    public void updateOrder(Integer orderId, Map<String, Object> orderData) {
        try {
            Object[] params = new Object[] { db, uid, password, config.getModels().getSaleOrder(), "write",
                    Collections.singletonList(orderId), orderData };
            client.execute("object.execute_kw", params);
        } catch (XmlRpcException e) {
            LogEvent.logError("Failed to update order in Odoo", e);
            throw new OdooOperationException("Failed to update order in Odoo", e);
        }
    }

    /**
     * Retrieves a sales order from Odoo by its ID.
     *
     * @param orderId The ID of the order to retrieve
     * @return The order data as a map
     * @throws OdooOperationException if the order retrieval fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOrder(Integer orderId) {
        try {
            Object[] params = new Object[] { db, uid, password, config.getModels().getSaleOrder(), "read",
                    Collections.singletonList(orderId), Arrays.asList("name", "partner_id", "amount_total", "state") };
            Object[] results = (Object[]) client.execute("object.execute_kw", params);
            return (Map<String, Object>) results[0];
        } catch (XmlRpcException e) {
            LogEvent.logError("Failed to get order from Odoo", e);
            throw new OdooOperationException("Failed to get order from Odoo", e);
        }
    }

    /**
     * Creates a new customer (partner) in Odoo or retrieves an existing one.
     * Searches for an existing customer by email and creates a new one if not
     * found.
     *
     * @param partnerData The customer data to create or search for
     * @return The ID of the created or found customer
     * @throws OdooOperationException if the customer creation/retrieval fails
     */
    @SuppressWarnings("unchecked")
    public Integer createOrGetPartner(Map<String, Object> partnerData) {
        try {
            // Search for existing partner
            Object[] searchParams = new Object[] { db, uid, password, config.getModels().getResPartner(), "search_read",
                    List.of(List.of(Arrays.asList("email", "=", partnerData.get("email")))),
                    Arrays.asList("id", "name", "email") };

            Object[] results = (Object[]) client.execute("object.execute_kw", searchParams);

            if (results.length > 0) {
                Map<String, Object> existingPartner = (Map<String, Object>) results[0];
                return (Integer) existingPartner.get("id");
            }

            // Create new partner if not found
            Object[] createParams = new Object[] { db, uid, password, config.getModels().getResPartner(), "create",
                    partnerData };
            return (Integer) client.execute("object.execute_kw", createParams);
        } catch (XmlRpcException e) {
            LogEvent.logError("Failed to create/get partner in Odoo", e);
            throw new OdooOperationException("Failed to create/get partner in Odoo", e);
        }
    }

    /**
     * Creates an invoice for a sales order in Odoo.
     *
     * @param orderId The ID of the order to create an invoice for
     * @return The ID of the created invoice
     * @throws OdooOperationException if the invoice creation fails
     */
    public Integer createInvoice(Integer orderId) {
        try {
            Object[] params = new Object[] { db, uid, password, config.getModels().getSaleOrder(),
                    "action_invoice_create", Collections.singletonList(orderId) };
            return (Integer) client.execute("object.execute_kw", params);
        } catch (XmlRpcException e) {
            LogEvent.logError("Failed to create invoice in Odoo", e);
            throw new OdooOperationException("Failed to create invoice in Odoo", e);
        }
    }
}