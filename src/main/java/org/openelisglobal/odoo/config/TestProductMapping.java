package org.openelisglobal.odoo.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration class for managing test-to-product mappings and prices in Odoo.
 * This class maintains the mapping between OpenELIS tests and Odoo products,
 * along with their corresponding prices.
 *
 * @author OpenELIS
 * @version 1.0.0
 */
@Configuration
@Getter
@Setter
public class TestProductMapping {
    
    /**
     * Map of test IDs to Odoo product IDs.
     * This mapping is populated from the application-odoo.yml configuration.
     */
    @Value("#{${odoo.test.product.mapping}}")
    private Map<String, Integer> testToProductMap = new HashMap<>();
    
    /**
     * Map of test IDs to their prices.
     * This mapping is populated from the application-odoo.yml configuration.
     */
    @Value("#{${odoo.test.price.mapping}}")
    private Map<String, Double> testToPriceMap = new HashMap<>();
    
    /**
     * Gets the Odoo product ID for a given test ID.
     *
     * @param testId The OpenELIS test ID
     * @return The corresponding Odoo product ID, or null if not found
     */
    public Integer getProductId(String testId) {
        return testToProductMap.get(testId);
    }
    
    /**
     * Gets the price for a given test ID.
     *
     * @param testId The OpenELIS test ID
     * @return The price of the test, or null if not found
     */
    public Double getPrice(String testId) {
        return testToPriceMap.get(testId);
    }
}
