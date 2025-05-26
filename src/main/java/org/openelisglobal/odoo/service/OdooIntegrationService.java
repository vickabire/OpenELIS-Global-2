package org.openelisglobal.odoo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.common.services.SampleAddService;
import org.openelisglobal.odoo.client.OdooClient;
import org.openelisglobal.odoo.config.TestProductMapping;
import org.openelisglobal.patient.action.bean.PatientManagementInfo;
import org.openelisglobal.sample.action.util.SamplePatientUpdateData;
import org.openelisglobal.test.valueholder.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for integrating OpenELIS with Odoo for order
 * processing and billing. This service handles the transformation of OpenELIS
 * orders into Odoo sales orders and manages the synchronization of customer
 * data between the two systems.
 */
@Service
public class OdooIntegrationService {

    @Autowired
    private OdooClient odooClient;

    @Autowired
    private TestProductMapping testProductMapping;

    /**
     * Processes an order by creating or updating the corresponding records in Odoo.
     * This method handles the complete workflow of order processing including:
     */
    @Transactional
    public void processOrder(SamplePatientUpdateData updateData, PatientManagementInfo patientInfo) {
        try {
            Integer partnerId = createOrGetCustomer(patientInfo);
            List<Map<String, Object>> orderLines = createOrderLines(updateData);

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("partner_id", partnerId);
            orderData.put("order_line", orderLines);
            orderData.put("client_order_ref", updateData.getAccessionNumber());

            Integer orderId = odooClient.createOrder(orderData);
            if (shouldCreateInvoice(updateData)) {
                odooClient.createInvoice(orderId);
            }
        } catch (Exception e) {
            LogEvent.logError("Failed to process order in Odoo", e);
            throw e;
        }
    }

    /**
     * Creates a new customer in Odoo or retrieves an existing one based on the
     * provided patient information. The method searches for an existing customer by
     * email and creates a new one if not found.
     */
    public Integer createOrGetCustomer(PatientManagementInfo patientInfo) {
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("name", patientInfo.getFirstName() + " " + patientInfo.getLastName());
        customerData.put("email", patientInfo.getEmail());
        customerData.put("phone", patientInfo.getPrimaryPhone());

        return odooClient.createOrGetPartner(customerData);
    }

    /**
     * Creates order lines in Odoo format from the OpenELIS test orders. Each test
     * in the sample is converted into an order line with appropriate product
     * mapping and pricing information.
     */
    public List<Map<String, Object>> createOrderLines(SamplePatientUpdateData updateData) {
        List<Map<String, Object>> orderLines = new ArrayList<>();

        for (SampleAddService.SampleTestCollection testCollection : updateData.getSampleItemsTests()) {
            for (Test test : testCollection.tests) {
                Map<String, Object> line = new HashMap<>();
                Integer productId = getOdooProductId(test);
                Double price = getTestPrice(test);

                if (productId != null && price != null) {
                    line.put("product_id", productId);
                    line.put("name", test.getLocalizedName());
                    line.put("product_uom_qty", 1);
                    line.put("price_unit", price);

                    orderLines.add(line);
                } else {
                    LogEvent.logWarn("OdooIntegrationService", "createOrderLines",
                            "Skipping test " + test.getId() + " due to missing product mapping or price");
                }
            }
        }

        return orderLines;
    }

    /**
     * Determines whether an invoice should be created for the order. This method
     * implements the business logic for invoice generation based on payment type,
     * configuration, or other relevant factors.
     */
    private boolean shouldCreateInvoice(SamplePatientUpdateData updateData) {
        // Implement logic to determine if an invoice should be created
        // For example, based on payment type or configuration
        return true;
    }

    /**
     * Maps an OpenELIS test to its corresponding Odoo product ID. This method uses
     * the configured mapping from TestProductMapping.
     */
    private Integer getOdooProductId(Test test) {
        return testProductMapping.getProductId(test.getId());
    }

    /**
     * Retrieves the price for a test from the configured price list. This method
     * uses the configured mapping from TestProductMapping.
     */
    private Double getTestPrice(Test test) {
        return testProductMapping.getPrice(test.getId());
    }
}
