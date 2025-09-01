# Odoo Integration Configuration Reference

This document provides a comprehensive reference for all configuration options available in the Odoo-OpenELIS integration.

## Environment Variables

### Required Configuration

| Variable | Description | Example | Default |
|----------|-------------|---------|---------|
| `org.openelisglobal.odoo.baseUrl` | Odoo server URL | `http://odoo.example.com:8069` | - |
| `org.openelisglobal.odoo.database` | Odoo database name | `openelis_production` | - |
| `org.openelisglobal.odoo.username` | Odoo username | `openelis_user` | - |
| `org.openelisglobal.odoo.password` | Odoo password | `secure_password` | - |

### Optional Configuration

| Variable | Description | Example | Default |
|----------|-------------|---------|---------|
| `logging.level.org.openelisglobal.odoo` | Logging level for Odoo integration | `DEBUG`, `INFO`, `WARN`, `ERROR` | `INFO` |

## CSV Mapping Configuration

### File Location

The test-to-product mapping file must be located at:
```
/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv
```

### CSV Format

The CSV file must have the following structure:

```csv
loinc_code,product_name,quantity,price_unit
58410-2,Complete Blood Count,1,25.00
24323-8,Basic Metabolic Panel,1,35.00
24331-1,Lipid Panel,1,30.00
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `loinc_code` | String | Yes | LOINC code of the test in OpenELIS |
| `product_name` | String | Yes | Product name in Odoo |
| `quantity` | Decimal | Yes | Quantity for invoice line (usually 1.0) |
| `price_unit` | Decimal | Yes | Unit price in Odoo currency |

### CSV Validation Rules

- File must be UTF-8 encoded
- First row must contain headers
- Empty lines are ignored
- Invalid numeric values are logged and skipped
- Duplicate LOINC codes use the last occurrence

## Docker Configuration

### Docker Compose Example

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
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_USER=odoo
      - DB_PASSWORD=odoo_password
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

### Volume Mounts

| Host Path | Container Path | Purpose | Permissions |
|-----------|----------------|---------|-------------|
| `./config/odoo/odoo-test-product-mapping.csv` | `/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv` | Test-product mapping | Read-only |

## Odoo Configuration

### Required Odoo Modules

Ensure these Odoo modules are installed:

- `account` - Accounting module
- `sale` - Sales module (for products)
- `base` - Base module (for partners)

### Odoo User Permissions

The integration user needs the following permissions:

| Model | Access Rights |
|-------|---------------|
| `res.partner` | Create, Read, Write |
| `product.product` | Read |
| `account.move` | Create, Read, Write |
| `account.move.line` | Create, Read, Write |

### Odoo Configuration Steps

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

## Logging Configuration

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

| Category | Description | Example |
|----------|-------------|---------|
| `OdooClient` | XML-RPC communication | Connection attempts, authentication |
| `OdooIntegrationService` | Business logic | Invoice creation, patient lookup |
| `TestProductMapping` | CSV mapping operations | File loading, mapping lookups |
| `SamplePatientUpdateDataCreatedEventListener` | Event handling | Event processing, error handling |

## Security Configuration

### Network Security

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

## Performance Configuration

### Connection Pooling

The integration automatically manages connections:

```java
// Connection is established once and reused
// Automatic reconnection on failures
// Connection timeout: 30 seconds (default)
```

### Asynchronous Processing

Invoice creation is handled asynchronously:

```java
@Async
@EventListener
public void handleSamplePatientUpdateDataCreatedEvent(SamplePatientUpdateDataCreatedEvent event) {
    // Non-blocking invoice creation
}
```

## Monitoring Configuration

### Health Check Endpoint

```bash
# Monitor integration health
curl -k https://openelis-server:8443/health/odoo

# Expected response
{
  "status": "UP",
  "odoo": "Available"
}
```

### Application Metrics

Monitor these key metrics:

- Health check response time
- Invoice creation success rate
- Patient creation success rate
- CSV mapping load time
- Odoo connection status

## Backup Configuration

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

## Troubleshooting Configuration

### Debug Mode

Enable comprehensive debugging:

```properties
# Enable debug logging
logging.level.org.openelisglobal.odoo=DEBUG

# Enable XML-RPC debugging
logging.level.org.apache.xmlrpc=DEBUG
```

### Connection Testing

Test Odoo connectivity:

```bash
# Test basic connectivity
curl -X POST http://odoo-server:8069/xmlrpc/2/common \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?><methodCall><methodName>version</methodName><params></params></methodCall>'

# Test authentication
curl -X POST http://odoo-server:8069/xmlrpc/2/common \
  -H "Content-Type: text/xml" \
  -d '<?xml version="1.0"?><methodCall><methodName>authenticate</methodName><params><param><value><string>database</string></value></param><param><value><string>username</string></value></param><param><value><string>password</string></value></param><param><value><struct></struct></value></param></params></methodCall>'
```

## Configuration Validation

### Validation Script

Create a validation script to check configuration:

```bash
#!/bin/bash
echo "Validating Odoo Integration Configuration..."

# Check environment variables
if [ -z "$ODOO_BASE_URL" ]; then
    echo "ERROR: ODOO_BASE_URL not set"
    exit 1
fi

# Check CSV file exists
if [ ! -f "/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv" ]; then
    echo "ERROR: CSV mapping file not found"
    exit 1
fi

# Check CSV format
if ! head -1 /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv | grep -q "loinc_code,product_name,quantity,price_unit"; then
    echo "ERROR: Invalid CSV header format"
    exit 1
fi

# Test Odoo connectivity
if ! curl -s -o /dev/null -w "%{http_code}" "$ODOO_BASE_URL/xmlrpc/2/common" | grep -q "200"; then
    echo "ERROR: Cannot connect to Odoo server"
    exit 1
fi

echo "Configuration validation passed!"
```

## Related Documentation

- [Odoo Integration Overview](odoo-integration.md)
- [Quick Start Guide](odoo-quickstart.md)
- [Troubleshooting Guide](odoo-integration.md#troubleshooting)
- [OpenELIS Configuration](server-property.md)

---

*This configuration reference covers all aspects of the Odoo-OpenELIS integration. For implementation details, see the main integration documentation.*
