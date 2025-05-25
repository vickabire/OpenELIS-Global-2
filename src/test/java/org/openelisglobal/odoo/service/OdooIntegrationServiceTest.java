package org.openelisglobal.odoo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openelisglobal.odoo.client.OdooClient;
import org.openelisglobal.patient.action.bean.PatientManagementInfo;
import org.openelisglobal.sample.action.util.SamplePatientUpdateData;

@ExtendWith(MockitoExtension.class)
public class OdooIntegrationServiceTest {

    @Mock
    private OdooClient odooClient;

    @InjectMocks
    private OdooIntegrationService odooIntegrationService;

    private SamplePatientUpdateData updateData;
    private PatientManagementInfo patientInfo;

    @BeforeEach
    void setUp() {
        updateData = new SamplePatientUpdateData(patientInfo.getPatientPK());
        patientInfo = new PatientManagementInfo();
        
        // Setup test data
        patientInfo.setFirstName("John");
        patientInfo.setLastName("Doe");
        patientInfo.setEmail("john.doe@example.com");
        patientInfo.setPrimaryPhone("1234567890");
        
        // Mock Odoo client responses
        when(odooClient.createOrGetPartner(any())).thenReturn(1);
        when(odooClient.createOrder(any())).thenReturn(1);
    }

    @Test
    void testProcessOrder() {
        // Execute
        odooIntegrationService.processOrder(updateData, patientInfo);

        // Verify
        verify(odooClient).createOrGetPartner(any());
        verify(odooClient).createOrder(any());
    }

    @Test
    void testCreateOrGetCustomer() {
        // Execute
        Integer partnerId = odooIntegrationService.createOrGetCustomer(patientInfo);

        // Verify
        assertEquals(1, partnerId);
        verify(odooClient).createOrGetPartner(any());
    }

    @Test
    void testCreateOrderLines() {
        // Setup
        Map<String, Object> expectedLine = new HashMap<>();
        expectedLine.put("product_id", 1);
        expectedLine.put("name", "Test Name");
        expectedLine.put("product_uom_qty", 1);
        expectedLine.put("price_unit", 100.0);

        // Execute
        List<Map<String, Object>> orderLines = odooIntegrationService.createOrderLines(updateData);

        // Verify
        assertNotNull(orderLines);
        // Add more specific assertions based on your implementation
    }
} 