# Odoo-OpenELIS Integration

## Overview

The Odoo-OpenELIS integration automates billing workflows by seamlessly generating invoices in Odoo whenever lab orders are placed in OpenELIS. This integration eliminates manual billing processes, reduces errors, and provides real-time financial visibility.

## Architecture

The integration follows a **Service-Oriented Architecture (SOA)** with clear separation of concerns:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   OpenELIS      │───▶│   Integration    │───▶│      Odoo       │
│   (LIMS)        │    │     Service      │    │   (ERP)         │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Core Components

1. **OdooClient** - Handles XML-RPC communication with Odoo
2. **OdooIntegrationService** - Business logic for invoice creation
3. **TestProductMapping** - CSV-based test-to-product mapping
4. **Event Handling** - Spring events for asynchronous processing

## Features

- 🔑 **Authentication** with Odoo's XML-RPC endpoint
- 👤 **Automatic patient creation** in Odoo if they don't exist
- 🔍 **Duplicate prevention** using national IDs or names
- 🛡️ **Error resilience** - failures in Odoo don't stop OpenELIS operations
- 📊 **Flexible test mapping** via CSV configuration
- 🔄 **Event-driven workflow** for seamless integration

## Installation

### Prerequisites

- OpenELIS Global 2.0 or later
- Odoo 16.0 or later
- Network connectivity between OpenELIS and Odoo servers

### Configuration

#### 1. Environment Variables

Add the following environment variables to your OpenELIS configuration:

```properties
# Odoo Connection Settings
org.openelisglobal.odoo.baseUrl=http://your-odoo-server:8069
org.openelisglobal.odoo.database=your_odoo_database
org.openelisglobal.odoo.username=your_odoo_username
org.openelisglobal.odoo.password=your_odoo_password
```

#### 2. Test-Product Mapping CSV

Create a CSV file at `/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv` with the following structure:

```csv
loinc_code,product_name,quantity,price_unit
12345-6,Complete Blood Count,1,25.00
78901-2,Basic Metabolic Panel,1,35.00
34567-8,Lipid Panel,1,30.00
```

**CSV Format:**
- `loinc_code`: The LOINC code of the test in OpenELIS
- `product_name`: The product name in Odoo
- `quantity`: Quantity for the invoice line (usually 1)
- `price_unit`: Unit price for the test

#### 3. Docker Compose Configuration

If using Docker, add the CSV mapping volume to your `docker-compose.yml`:

```yaml
volumes:
  - ./volume/odoo/odoo-test-product-mapping.csv:/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
```

## Workflow

### Event-Driven Process

The integration uses Spring events to trigger invoice creation:

```
Sample Created in OpenELIS  
           │  
           ▼  
[ Event Fired: SamplePatientUpdateDataCreated ]  
           │  
           ▼  
OdooIntegrationService → Finds/creates patient in Odoo  
           │  
           ▼  
Maps lab tests → Odoo products  
           │  
           ▼  
💰 Invoice automatically created in Odoo  
```

### Detailed Steps

1. **Sample Creation**: When a sample is created in OpenELIS, a `SamplePatientUpdateDataCreatedEvent` is fired
2. **Patient Lookup**: The service searches for the patient in Odoo using national ID or name
3. **Patient Creation**: If not found, a new partner is created in Odoo
4. **Test Mapping**: Each test in the sample is mapped to an Odoo product using the CSV configuration
5. **Invoice Creation**: An invoice is created with all mapped test products
6. **Error Handling**: Any failures are logged but don't affect OpenELIS operations

## Configuration Details

### Odoo Client Configuration

The `OdooClient` handles all communication with Odoo:

```java
@Component
public class OdooClient {
    @Value("${org.openelisglobal.odoo.baseUrl}")
    private String url;
    
    @Value("${org.openelisglobal.odoo.database}")
    private String database;
    
    @Value("${org.openelisglobal.odoo.username}")
    private String username;
    
    @Value("${org.openelisglobal.odoo.password}")
    private String password;
}
```

### Test Product Mapping

The `TestProductMapping` component loads and manages test-to-product mappings:

```java
@Component
public class TestProductMapping {
    private static final String FIXED_CSV_PATH = "/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv";
    private final Map<String, TestProductInfo> testToProductInfo = new HashMap<>();
}
```

### Integration Service

The `OdooIntegrationService` orchestrates the entire integration process:

```java
@Service
public class OdooIntegrationService {
    public void createInvoice(SamplePatientUpdateData updateData) {
        // 1. Get or create patient partner
        // 2. Map tests to products
        // 3. Create invoice in Odoo
    }
}
```

## Monitoring and Health Checks

### Health Check Endpoint

Monitor the integration status via the health check endpoint:

```bash
curl https://your-openelis-server:8443/health/odoo
```

**Response when healthy:**
```json
{
  "status": "UP",
  "odoo": "Available"
}
```

**Response when unhealthy:**
```json
{
  "status": "DOWN",
  "odoo": "Unavailable"
}
```

### Logging

The integration provides comprehensive logging:

- **Info logs**: Successful operations, patient creation, invoice creation
- **Warning logs**: Missing mappings, fallback operations
- **Error logs**: Connection failures, Odoo errors

### Log Examples

```
INFO  - Successfully created invoice in Odoo with ID: 12345 for sample: ABC123
INFO  - Found existing partner with national ID 123456789: 67890
WARN  - No Odoo product mapping found for test: 98765-4
ERROR - Error creating invoice in Odoo for sample ABC123: Connection timeout
```

## Troubleshooting

### Common Issues

#### 1. Connection Failures

**Symptoms**: Health check returns "DOWN" status

**Solutions**:
- Verify Odoo server is running and accessible
- Check network connectivity between OpenELIS and Odoo
- Validate credentials in environment variables
- Ensure Odoo XML-RPC is enabled

#### 2. Missing Test Mappings

**Symptoms**: Warnings about missing product mappings

**Solutions**:
- Add missing LOINC codes to the CSV mapping file
- Verify CSV file format and location
- Check file permissions on the CSV file

#### 3. Patient Creation Failures

**Symptoms**: Errors creating patients in Odoo

**Solutions**:
- Verify Odoo user has permissions to create partners
- Check required fields in Odoo partner model
- Review patient data quality in OpenELIS

### Debug Mode

Enable debug logging by adding to your logging configuration:

```properties
logging.level.org.openelisglobal.odoo=DEBUG
```

## Security Considerations

### Authentication

- Use dedicated Odoo user account for integration
- Implement least-privilege access in Odoo
- Regularly rotate integration passwords

### Data Protection

- Ensure patient data is transmitted securely
- Implement appropriate data retention policies
- Consider data anonymization for testing

### Network Security

- Use HTTPS for Odoo communication
- Implement firewall rules to restrict access
- Consider VPN for secure communication

## Performance Optimization

### Connection Pooling

The integration uses connection pooling to optimize Odoo communication:

```java
@Configuration
public class OdooConnectionConfig {
    @Bean
    public OdooConnection odooConnection(OdooClient odooClient) {
        // Connection pooling and error handling
    }
}
```

### Asynchronous Processing

Invoice creation is handled asynchronously to avoid blocking OpenELIS operations:

```java
@Async
@EventListener
public void handleSamplePatientUpdateDataCreatedEvent(SamplePatientUpdateDataCreatedEvent event) {
    // Process invoice creation asynchronously
}
```

## API Reference

### Health Check Controller

```java
@RestController
public class HealthCheckController {
    @GetMapping("/health/odoo")
    public ResponseEntity<Map<String, Object>> odooHealth() {
        // Returns integration status
    }
}
```

### Odoo Integration Service

```java
@Service
public class OdooIntegrationService {
    public void createInvoice(SamplePatientUpdateData updateData);
    private Integer getOrCreatePatientPartner(SamplePatientUpdateData updateData);
    private List<Map<String, Object>> createInvoiceLines(SamplePatientUpdateData updateData);
}
```

## Deployment Examples

### Docker Compose Setup

```yaml
version: '3.3'
services:
  openelis:
    image: openelis-global:latest
    environment:
      - org.openelisglobal.odoo.baseUrl=http://odoo:8069
      - org.openelisglobal.odoo.database=openelis_db
      - org.openelisglobal.odoo.username=openelis_user
      - org.openelisglobal.odoo.password=secure_password
    volumes:
      - ./odoo-mapping.csv:/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
    depends_on:
      - odoo
  
  odoo:
    image: odoo:16.0
    environment:
      - HOST=odoo
      - USER=openelis_user
      - PASSWORD=secure_password
```

### Standalone Deployment

For standalone deployments, ensure the CSV mapping file is accessible:

```bash
# Create mapping directory
sudo mkdir -p /var/lib/openelis-global/odoo

# Copy mapping file
sudo cp odoo-test-product-mapping.csv /var/lib/openelis-global/odoo/

# Set permissions
sudo chown tomcat:tomcat /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
sudo chmod 644 /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
```

## Best Practices

### CSV Mapping Management

1. **Version Control**: Keep CSV mappings in version control
2. **Validation**: Validate CSV format before deployment
3. **Backup**: Maintain backups of mapping configurations
4. **Documentation**: Document any custom mappings

### Error Handling

1. **Graceful Degradation**: Integration failures shouldn't affect OpenELIS
2. **Retry Logic**: Implement retry mechanisms for transient failures
3. **Alerting**: Set up monitoring for integration failures
4. **Logging**: Maintain comprehensive logs for troubleshooting

### Performance

1. **Connection Pooling**: Use connection pooling for Odoo communication
2. **Asynchronous Processing**: Handle invoice creation asynchronously
3. **Batch Processing**: Consider batch operations for high-volume scenarios
4. **Caching**: Cache frequently accessed data

## Support and Maintenance

### Regular Maintenance

- Monitor integration health daily
- Review logs for errors and warnings
- Update test mappings as needed
- Verify Odoo connectivity

### Updates and Upgrades

- Test integration after OpenELIS updates
- Verify compatibility with Odoo upgrades
- Update mapping files for new tests
- Review and update documentation

### Getting Help

For issues with the Odoo integration:

1. Check the troubleshooting section above
2. Review application logs for error details
3. Verify configuration settings
4. Contact the development team with detailed error information

## Related Documentation

- [OpenELIS Installation Guide](install.md)
- [OpenELIS Configuration](server-property.md)
- [OpenELIS Troubleshooting](troubleshooting.md)
- [Odoo Documentation](https://www.odoo.com/documentation)

---

*This documentation covers the Odoo-OpenELIS integration developed as part of Google Summer of Code 2025. For more information about the project, visit the [project repository](https://github.com/DIGI-UW/odoo-openelis-connector).*
