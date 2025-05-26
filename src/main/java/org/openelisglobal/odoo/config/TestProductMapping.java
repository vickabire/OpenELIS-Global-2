package org.openelisglobal.odoo.config;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for mapping OpenELIS test IDs to Odoo product IDs and
 * prices. This class maintains the mapping between OpenELIS tests and their
 * corresponding Odoo products and prices, which is used for integration with
 * the Odoo system.
 */
@Configuration
@Getter
@Setter
public class TestProductMapping {

    /**
     * Raw string containing the test-to-product mapping configuration. Format:
     * "testId=productId,testId=productId,..."
     */
    @Value("${odoo.test.product.mapping:#{null}}")
    private String productMappingString;

    /**
     * Raw string containing the test-to-price mapping configuration. Format:
     * "testId=price,testId=price,..."
     */
    @Value("${odoo.test.price.mapping:#{null}}")
    private String priceMappingString;

    /**
     * Map storing the parsed test ID to Odoo product ID mappings. Key: OpenELIS
     * test ID Value: Odoo product ID
     */
    private final Map<String, Integer> testToProductMap = new HashMap<>();

    /**
     * Map storing the parsed test ID to price mappings. Key: OpenELIS test ID
     * Value: Test price
     */
    private final Map<String, Double> testToPriceMap = new HashMap<>();

    /**
     * Initializes the mapping by parsing the configuration strings. This method is
     * called after dependency injection is complete.
     */
    @PostConstruct
    public void init() {
        parseProductMapping();
        parsePriceMapping();
    }

    /**
     * Parses the product mapping string and populates the testToProductMap. The
     * input string should be in the format "testId=productId,testId=productId,..."
     * Invalid entries are silently skipped.
     */
    private void parseProductMapping() {
        if (productMappingString != null && !productMappingString.isEmpty()) {
            String[] entries = productMappingString.split(",");
            for (String entry : entries) {
                String[] kv = entry.split("=");
                if (kv.length == 2) {
                    testToProductMap.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
                }
            }
        }
    }

    /**
     * Parses the price mapping string and populates the testToPriceMap. The input
     * string should be in the format "testId=price,testId=price,..." Invalid
     * entries are silently skipped.
     */
    private void parsePriceMapping() {
        if (priceMappingString != null && !priceMappingString.isEmpty()) {
            String[] entries = priceMappingString.split(",");
            for (String entry : entries) {
                String[] kv = entry.split("=");
                if (kv.length == 2) {
                    testToPriceMap.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                }
            }
        }
    }

    /**
     * Retrieves the Odoo product ID for a given test ID.
     *
     * @param testId The OpenELIS test ID to look up
     * @return The corresponding Odoo product ID, or null if not found
     */
    public Integer getProductId(String testId) {
        return testToProductMap.get(testId);
    }

    /**
     * Retrieves the price for a given test ID.
     *
     * @param testId The OpenELIS test ID to look up
     * @return The price of the test, or null if not found
     */
    public Double getPrice(String testId) {
        return testToPriceMap.get(testId);
    }
}
