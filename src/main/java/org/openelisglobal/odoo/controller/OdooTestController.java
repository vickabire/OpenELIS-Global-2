package org.openelisglobal.odoo.controller;

import java.util.HashMap;
import java.util.Map;
import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.rest.BaseRestController;
import org.openelisglobal.odoo.client.OdooClient;
import org.openelisglobal.odoo.config.TestProductMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for testing Odoo integration functionality.
 * This controller provides endpoints to verify the connection and configuration.
 */
@Controller
@RequestMapping("/odoo-test")
public class OdooTestController extends BaseRestController {
    
    @Autowired
    private OdooClient odooClient;
    
    @Autowired
    private TestProductMapping testProductMapping;
    
    /**
     * Tests the Odoo connection and returns the status.
     * 
     * @return Map containing connection status and configuration details
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            odooClient.connect();
            response.put("status", "success");
            response.put("message", "Successfully connected to Odoo");
            
            Map<String, Object> config = new HashMap<>();
            config.put("productMappings", testProductMapping.getTestToProductMap());
            config.put("priceMappings", testProductMapping.getTestToPriceMap());
            response.put("configuration", config);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to connect to Odoo: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
