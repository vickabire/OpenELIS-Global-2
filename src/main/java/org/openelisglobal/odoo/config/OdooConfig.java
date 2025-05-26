package org.openelisglobal.odoo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Odoo integration settings. This class manages the
 * Odoo configuration properties including: - Odoo server connection (URL,
 * database, credentials) - Model mappings for various Odoo entities
 * <p>
 * The configuration is populated through Spring's property injection mechanism
 * based on the properties defined in the application-odoo.yml file or any
 * configuration file i.e application.properties, common.properties.
 */
@Configuration
@Getter
@Setter
public class OdooConfig {
    @Value("${odoo.url}")
    private String url;

    @Value("${odoo.database}")
    private String database;

    @Value("${odoo.username}")
    private String username;

    @Value("${odoo.password}")
    private String password;

    private Models models = new Models();

    /**
     * Inner class representing the Odoo model mappings. Contains the technical
     * names of various Odoo models used in the integration.
     */
    @Getter
    @Setter
    public static class Models {
        /**
         * The technical name of the sales order model.
         */
        @Value("${odoo.models.sale_order}")
        private String saleOrder = "sale.order";

        /**
         * The technical name of the account move model.
         */
        @Value("${odoo.models.account_move}")
        private String accountMove = "account.move";

        /**
         * The technical name of the partner model.
         */
        @Value("${odoo.models.res_partner}")
        private String resPartner = "res.partner";

        /**
         * The technical name of the product model.
         */
        @Value("${odoo.models.product_product}")
        private String productProduct = "product.product";
    }
}
