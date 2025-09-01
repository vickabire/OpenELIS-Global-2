# Odoo-OpenELIS Integration Documentation

## Overview

The Odoo-OpenELIS integration automates billing workflows by seamlessly
generating invoices in Odoo whenever lab orders are placed in OpenELIS. This
integration eliminates manual billing processes, reduces errors, and provides
real-time financial visibility.

## Architecture

The integration follows a **Service-Oriented Architecture (SOA)** with clear
separation of concerns:

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

---

## Quick Start Guide

This section will help you set up the Odoo-OpenELIS integration in 10 minutes.

### Prerequisites

- OpenELIS Global 2.0+ running
- Odoo 16.0+ running and accessible
- Network connectivity between systems

### Step 1: Configure Environment Variables

Add these to your OpenELIS configuration file or environment:

```properties
org.openelisglobal.odoo.baseUrl=http://your-odoo-server:8069
org.openelisglobal.odoo.database=your_odoo_database
org.openelisglobal.odoo.username=your_odoo_username
org.openelisglobal.odoo.password=your_odoo_password
```

### Step 2: Create Test-Product Mapping

Create file: `/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv`

```csv
loinc_code,product_name,quantity,price_unit
58410-2,CBC Test,1,25.00
24323-8,Basic Metabolic Panel,1,35.00
24331-1,Lipid Panel,1,30.00
```

### Step 3: Restart OpenELIS

```bash
# If using Docker
docker-compose restart openelis

# If using standalone Tomcat
sudo systemctl restart tomcat
```

### Step 4: Verify Integration

Check the health endpoint:

```bash
curl https://your-openelis-server:8443/health/odoo
```

Expected response:

```json
{
  "status": "UP",
  "odoo": "Available"
}
```

### Step 5: Test the Integration

1. Create a new sample in OpenELIS with tests that have LOINC codes
2. Check Odoo for the automatically created invoice
3. Verify patient creation in Odoo

---

## Installation and Configuration

### Prerequisites

- OpenELIS Global 2.0 or later
- Odoo 16.0 or later
- Network connectivity between OpenELIS and Odoo servers

### Environment Variables

#### Required Configuration

| Variable                           | Description        | Example                        | Default |
| ---------------------------------- | ------------------ | ------------------------------ | ------- |
| `org.openelisglobal.odoo.baseUrl`  | Odoo server URL    | `http://odoo.example.com:8069` | -       |
| `org.openelisglobal.odoo.database` | Odoo database name | `openelis_production`          | -       |
| `org.openelisglobal.odoo.username` | Odoo username      | `openelis_user`                | -       |
| `org.openelisglobal.odoo.password` | Odoo password      | `secure_password`              | -       |

#### Optional Configuration

| Variable                                | Description                        | Example                          | Default |
| --------------------------------------- | ---------------------------------- | -------------------------------- | ------- |
| `logging.level.org.openelisglobal.odoo` | Logging level for Odoo integration | `DEBUG`, `INFO`, `WARN`, `ERROR` | `INFO`  |

### CSV Mapping Configuration

#### File Location

The test-to-product mapping file must be located at:

```
/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
```

#### CSV Format

The CSV file must have the following structure:

```csv
loinc_code,product_name,quantity,price_unit
58410-2,Complete Blood Count,1,25.00
24323-8,Basic Metabolic Panel,1,35.00
24331-1,Lipid Panel,1,30.00
```

#### Field Descriptions

| Field          | Type    | Required | Description                             |
| -------------- | ------- | -------- | --------------------------------------- |
| `loinc_code`   | String  | Yes      | LOINC code of the test in OpenELIS      |
| `product_name` | String  | Yes      | Product name in Odoo                    |
| `quantity`     | Decimal | Yes      | Quantity for invoice line (usually 1.0) |
| `price_unit`   | Decimal | Yes      | Unit price in Odoo currency             |

#### CSV Validation Rules

- File must be UTF-8 encoded
- First row must contain headers
- Empty lines are ignored
- Invalid numeric values are logged and skipped
- Duplicate LOINC codes use the last occurrence

### Docker Configuration

#### Docker Compose Example

```yaml
version: "3.3"
services:
  openelis:
    image: openelis-global:latest
    environment:
      - org.openelisglobal.odoo.baseUrl=http://odoo:8069
      - org.openelisglobal.odoo.database=openelis_db
      - org.openelisglobal.odoo.username=openelis_user
      - org.openelisglobal.odoo.password=secure_password
      - logging.level.org.openelisglobal.odoo=INFO
    volumes:
      - ./config/odoo/odoo-test-product-mapping.csv:/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv:ro
    depends_on:
      - odoo

  odoo:
    image: odoo:16.0
    environment:
      - HOST=odoo
      - USER=openelis_user
      - PASSWORD=secure_password
    depends_on:
      - postgres

  postgres:
    image: postgres:13
    environment:
      - POSTGRES_DB=openelis_db
      - POSTGRES_USER=odoo
      - POSTGRES_PASSWORD=odoo_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

#### Volume Mounts

| Host Path                                     | Container Path                                                | Purpose              | Permissions |
| --------------------------------------------- | ------------------------------------------------------------- | -------------------- | ----------- |
| `./config/odoo/odoo-test-product-mapping.csv` | `/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv` | Test-product mapping | Read-only   |

### Odoo Configuration

#### Required Odoo Modules

Ensure these Odoo modules are installed:

- `account` - Accounting module
- `sale` - Sales module (for products)
- `base` - Base module (for partners)

#### Odoo User Permissions

The integration user needs the following permissions:

| Model               | Access Rights       |
| ------------------- | ------------------- |
| `res.partner`       | Create, Read, Write |
| `product.product`   | Read                |
| `account.move`      | Create, Read, Write |
| `account.move.line` | Create, Read, Write |

#### Odoo Configuration Steps

1. **Create Integration User**

   ```bash
   # In Odoo, create a new user with limited permissions
   # Username: openelis_user
   # Password: secure_password
   # Groups: Accounting User, Sales User
   ```

2. **Configure Products**

   ```bash
   # Create products in Odoo that match your CSV mapping
   # Product names should match the product_name field in CSV
   # Set appropriate pricing and accounting settings
   ```

3. **Configure Chart of Accounts**
   ```bash
   # Ensure default income account is configured
   # Usually account_id = 1 for invoice lines
   ```

---

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

1. **Sample Creation**: When a sample is created in OpenELIS, a
   `SamplePatientUpdateDataCreatedEvent` is fired
2. **Patient Lookup**: The service searches for the patient in Odoo using
   national ID or name
3. **Patient Creation**: If not found, a new partner is created in Odoo
4. **Test Mapping**: Each test in the sample is mapped to an Odoo product using
   the CSV configuration
5. **Invoice Creation**: An invoice is created with all mapped test products
6. **Error Handling**: Any failures are logged but don't affect OpenELIS
   operations

---

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

---

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

### Log Levels

Configure logging in your application properties:

```properties
# Debug level - shows all integration details
logging.level.org.openelisglobal.odoo=DEBUG

# Info level - shows key operations (default)
logging.level.org.openelisglobal.odoo=INFO

# Warn level - shows only warnings and errors
logging.level.org.openelisglobal.odoo=WARN

# Error level - shows only errors
logging.level.org.openelisglobal.odoo=ERROR
```

### Log Categories

| Category                                      | Description            | Example                             |
| --------------------------------------------- | ---------------------- | ----------------------------------- |
| `OdooClient`                                  | XML-RPC communication  | Connection attempts, authentication |
| `OdooIntegrationService`                      | Business logic         | Invoice creation, patient lookup    |
| `TestProductMapping`                          | CSV mapping operations | File loading, mapping lookups       |
| `SamplePatientUpdateDataCreatedEventListener` | Event handling         | Event processing, error handling    |

---

## Troubleshooting

### Quick Diagnostic Checklist

Before diving into specific issues, run through this checklist:

- [ ] Odoo server is running and accessible
- [ ] Network connectivity between OpenELIS and Odoo
- [ ] Environment variables are correctly configured
- [ ] CSV mapping file exists and is readable
- [ ] Odoo user has required permissions
- [ ] Health check endpoint returns "UP"

### Common Issues and Solutions

#### 1. Health Check Returns "DOWN"

**Symptoms:**

```json
{
  "status": "DOWN",
  "odoo": "Unavailable"
}
```

**Diagnostic Steps:**

1. **Check Odoo Server Status**

   ```bash
   # Test if Odoo is running
   curl -I http://your-odoo-server:8069

   # Expected: HTTP/1.1 200 OK
   ```

2. **Verify Network Connectivity**

   ```bash
   # Test network connectivity
   telnet your-odoo-server 8069

   # Or use nc
   nc -zv your-odoo-server 8069
   ```

3. **Check Environment Variables**
   ```bash
   # Verify configuration
   grep -E "org\.openelisglobal\.odoo\." /path/to/application.properties
   ```

**Solutions:**

- **Odoo not running**: Start Odoo service
- **Network issues**: Check firewall rules, DNS resolution
- **Wrong credentials**: Update environment variables
- **XML-RPC disabled**: Enable XML-RPC in Odoo configuration

#### 2. CSV Mapping File Not Found

**Symptoms:**

```
ERROR - No CSV mapping file could be loaded from fixed path: /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
```

**Diagnostic Steps:**

1. **Check File Existence**

   ```bash
   ls -la /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
   ```

2. **Check File Permissions**

   ```bash
   # File should be readable by OpenELIS process
   sudo chown tomcat:tomcat /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
   sudo chmod 644 /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
   ```

3. **Verify CSV Format**

   ```bash
   # Check header format
   head -1 /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv

   # Expected: loinc_code,product_name,quantity,price_unit
   ```

**Solutions:**

- **File missing**: Create the CSV file with proper format
- **Wrong permissions**: Fix file ownership and permissions
- **Wrong location**: Ensure file is in correct directory
- **Invalid format**: Fix CSV header and data format

#### 3. No Invoices Created in Odoo

**Symptoms:**

- Samples created in OpenELIS but no invoices appear in Odoo
- No error messages in logs

**Diagnostic Steps:**

1. **Check Application Logs**

   ```bash
   # Look for integration-related logs
   grep -i "odoo" /var/lib/openelis-global/logs/openelis.log

   # Look for specific error messages
   grep -i "invoice" /var/lib/openelis-global/logs/openelis.log
   ```

2. **Verify Test Mappings**

   ```bash
   # Check if tests have LOINC codes
   # In OpenELIS admin interface, verify test configurations
   ```

3. **Test CSV Mapping**
   ```bash
   # Verify LOINC codes in CSV match OpenELIS
   cat /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
   ```

**Solutions:**

- **Missing LOINC codes**: Add LOINC codes to tests in OpenELIS
- **No CSV mappings**: Add test mappings to CSV file
- **Silent failures**: Enable debug logging to see detailed errors
- **Event not firing**: Check if sample creation events are working

#### 4. Patient Creation Failures

**Symptoms:**

```
ERROR - Error creating partner in Odoo for patient: Connection timeout
```

**Diagnostic Steps:**

1. **Check Odoo User Permissions**

   ```bash
   # In Odoo, verify user has partner creation rights
   # Settings > Users & Companies > Users > openelis_user
   ```

2. **Verify Required Fields**

   ```bash
   # Check if patient data has required fields
   # National ID, name, etc.
   ```

3. **Test Manual Partner Creation**
   ```bash
   # Try creating a partner manually in Odoo
   # Verify the process works
   ```

**Solutions:**

- **Permission issues**: Grant partner creation rights to integration user
- **Missing fields**: Ensure patient data is complete
- **Odoo configuration**: Check Odoo partner model requirements
- **Network issues**: Verify stable connection to Odoo

#### 5. Invalid CSV Format Errors

**Symptoms:**

```
ERROR - Invalid numeric value in row for key '12345-6': quantity='abc', price_unit='xyz'
```

**Diagnostic Steps:**

1. **Check CSV Data Types**

   ```bash
   # Verify quantity and price_unit are numeric
   awk -F',' 'NR>1 {print $3, $4}' /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
   ```

2. **Check for Special Characters**

   ```bash
   # Look for hidden characters or encoding issues
   file /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
   ```

3. **Validate CSV Structure**
   ```bash
   # Count fields in each row
   awk -F',' '{print NF}' /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
   ```

**Solutions:**

- **Non-numeric values**: Fix quantity and price_unit fields
- **Encoding issues**: Ensure file is UTF-8 encoded
- **Extra fields**: Remove extra commas or fields
- **Missing fields**: Add required fields to all rows

#### 6. Authentication Failures

**Symptoms:**

```
ERROR - Cannot authenticate to Odoo server
```

**Diagnostic Steps:**

1. **Test Authentication Manually**

   ```bash
   curl -X POST http://odoo-server:8069/xmlrpc/2/common \
     -H "Content-Type: text/xml" \
     -d '<?xml version="1.0"?><methodCall><methodName>authenticate</methodName><params><param><value><string>database</string></value></param><param><value><string>username</string></value></param><param><value><string>password</string></value></param><param><value><struct></struct></value></param></params></methodCall>'
   ```

2. **Verify Credentials**

   ```bash
   # Check environment variables
   echo $ODOO_USERNAME
   echo $ODOO_PASSWORD
   ```

3. **Check Odoo User Status**
   ```bash
   # In Odoo, verify user is active and not locked
   ```

**Solutions:**

- **Wrong credentials**: Update username/password
- **User inactive**: Activate user in Odoo
- **Database name**: Verify correct database name
- **User locked**: Unlock user account in Odoo

### Debug Mode

Enable comprehensive debugging to get detailed information:

#### 1. Enable Debug Logging

Add to your application properties:

```properties
logging.level.org.openelisglobal.odoo=DEBUG
logging.level.org.apache.xmlrpc=DEBUG
```

#### 2. Monitor Logs in Real-Time

```bash
# Follow logs in real-time
tail -f /var/lib/openelis-global/logs/openelis.log | grep -i odoo

# Or use journalctl if using systemd
journalctl -u openelis -f | grep -i odoo
```

#### 3. Test Individual Components

```bash
# Test Odoo connectivity
curl -v http://odoo-server:8069/xmlrpc/2/common

# Test CSV file parsing
head -5 /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv

# Test health endpoint
curl -v https://openelis-server:8443/health/odoo
```

### Recovery Procedures

#### 1. Complete Integration Reset

If the integration is completely broken:

```bash
# 1. Stop OpenELIS
sudo systemctl stop openelis

# 2. Clear any cached connections
# (Restart will handle this)

# 3. Verify Odoo is accessible
curl http://odoo-server:8069

# 4. Verify CSV file
ls -la /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv

# 5. Restart OpenELIS
sudo systemctl start openelis

# 6. Check health
curl https://openelis-server:8443/health/odoo
```

#### 2. CSV Mapping Recovery

If CSV mapping is corrupted:

```bash
# 1. Backup current file
cp /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv \
   /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv.backup

# 2. Restore from backup
cp /backup/odoo-mapping-YYYYMMDD.csv \
   /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv

# 3. Fix permissions
sudo chown tomcat:tomcat /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
sudo chmod 644 /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv

# 4. Restart OpenELIS
sudo systemctl restart openelis
```

#### 3. Odoo Connection Recovery

If Odoo connection is lost:

```bash
# 1. Check Odoo server status
curl http://odoo-server:8069

# 2. Restart Odoo if needed
sudo systemctl restart odoo

# 3. Wait for Odoo to fully start
sleep 30

# 4. Test authentication
# (Use the authentication test from above)

# 5. Restart OpenELIS
sudo systemctl restart openelis
```

---

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

### Network Security Configuration

```bash
# Firewall rules for Odoo communication
# Allow OpenELIS server to connect to Odoo XML-RPC port
iptables -A INPUT -s OPENELIS_IP -p tcp --dport 8069 -j ACCEPT

# Or use UFW
ufw allow from OPENELIS_IP to any port 8069
```

### SSL/TLS Configuration

For production environments, use HTTPS:

```properties
# Use HTTPS for Odoo communication
org.openelisglobal.odoo.baseUrl=https://odoo.example.com:8069
```

### Authentication Security

```bash
# Use strong passwords for integration user
# Consider using API keys or OAuth if supported
# Regularly rotate integration credentials
```

---

## Performance Optimization

### Connection Pooling

The integration automatically manages connections:

```java
// Connection is established once and reused
// Automatic reconnection on failures
// Connection timeout: 30 seconds (default)
```

### Asynchronous Processing

Invoice creation is handled asynchronously to avoid blocking OpenELIS
operations:

```java
@Async
@EventListener
public void handleSamplePatientUpdateDataCreatedEvent(SamplePatientUpdateDataCreatedEvent event) {
    // Non-blocking invoice creation
}
```

### Performance Issues

#### 1. Slow Invoice Creation

**Symptoms:**

- Long delays between sample creation and invoice creation
- Timeout errors

**Solutions:**

- **Network latency**: Optimize network connection
- **Odoo performance**: Check Odoo server performance
- **Connection pooling**: Verify connection reuse
- **Asynchronous processing**: Ensure events are processed asynchronously

#### 2. High Memory Usage

**Symptoms:**

- OpenELIS using excessive memory
- Out of memory errors

**Solutions:**

- **Connection leaks**: Restart OpenELIS to clear connections
- **Large CSV files**: Optimize CSV mapping file size
- **Logging levels**: Reduce debug logging in production

---

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

---

## Deployment Examples

### Docker Compose Setup

```yaml
version: "3.3"
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

---

## Monitoring and Alerting

### Health Check Monitoring

Set up monitoring for the health endpoint:

```bash
#!/bin/bash
# Health check script
HEALTH_URL="https://openelis-server:8443/health/odoo"
EXPECTED_STATUS='"status":"UP"'

if curl -s -k "$HEALTH_URL" | grep -q "$EXPECTED_STATUS"; then
    echo "OK: Odoo integration is healthy"
    exit 0
else
    echo "CRITICAL: Odoo integration is down"
    exit 2
fi
```

### Log Monitoring

Monitor for specific error patterns:

```bash
# Monitor for integration errors
tail -f /var/lib/openelis-global/logs/openelis.log | \
  grep -E "(ERROR.*odoo|CRITICAL.*integration)" | \
  while read line; do
    echo "ALERT: $line"
    # Send alert via email, Slack, etc.
  done
```

### Performance Monitoring

Monitor integration performance:

```bash
# Check response times
time curl -s -k https://openelis-server:8443/health/odoo > /dev/null

# Monitor log file growth
ls -lh /var/lib/openelis-global/logs/openelis.log
```

### Application Metrics

Monitor these key metrics:

- Health check response time
- Invoice creation success rate
- Patient creation success rate
- CSV mapping load time
- Odoo connection status

---

## Backup and Maintenance

### CSV Mapping Backup

```bash
# Backup mapping file
cp /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv \
   /backup/odoo-mapping-$(date +%Y%m%d).csv

# Automated backup script
#!/bin/bash
BACKUP_DIR="/backup/odoo"
DATE=$(date +%Y%m%d_%H%M%S)
cp /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv \
   "$BACKUP_DIR/odoo-mapping-$DATE.csv"
# Keep last 30 days
find "$BACKUP_DIR" -name "odoo-mapping-*.csv" -mtime +30 -delete
```

### Configuration Backup

```bash
# Backup environment configuration
cp /etc/openelis-global/application.properties \
   /backup/config-$(date +%Y%m%d).properties
```

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

---

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

---

## Getting Help

### Collect Diagnostic Information

Before contacting support, collect this information:

```bash
# System information
uname -a
java -version

# OpenELIS version
# (Check application logs or admin interface)

# Configuration
grep -E "org\.openelisglobal\.odoo\." /path/to/application.properties

# Recent logs
tail -100 /var/lib/openelis-global/logs/openelis.log | grep -i odoo

# Health check
curl -s -k https://openelis-server:8443/health/odoo

# CSV file info
ls -la /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
head -5 /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
```

### Contact Support

When contacting support, include:

- Description of the issue
- Steps to reproduce
- Diagnostic information (above)
- Error messages and logs
- Configuration details (without sensitive data)

### Community Resources

- [OpenELIS Documentation](https://openelis-global.org)
- [Odoo Documentation](https://www.odoo.com/documentation)
- [GitHub Issues](https://github.com/DIGI-UW/OpenELIS-Global-2/issues)

---

## Related Documentation

- [OpenELIS Installation Guide](install.md)
- [OpenELIS Configuration](server-property.md)
- [OpenELIS Troubleshooting](troubleshooting.md)
- [Odoo Documentation](https://www.odoo.com/documentation)

---

_This documentation covers the Odoo-OpenELIS integration developed as part of
Google Summer of Code 2025. For more information about the project, visit the
[project repository](https://github.com/DIGI-UW/odoo-openelis-connector)._
