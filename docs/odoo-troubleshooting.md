# Odoo Integration Troubleshooting Guide

This guide provides solutions for common issues encountered when using the Odoo-OpenELIS integration.

## Quick Diagnostic Checklist

Before diving into specific issues, run through this checklist:

- [ ] Odoo server is running and accessible
- [ ] Network connectivity between OpenELIS and Odoo
- [ ] Environment variables are correctly configured
- [ ] CSV mapping file exists and is readable
- [ ] Odoo user has required permissions
- [ ] Health check endpoint returns "UP"

## Common Issues and Solutions

### 1. Health Check Returns "DOWN"

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

### 2. CSV Mapping File Not Found

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

### 3. No Invoices Created in Odoo

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

### 4. Patient Creation Failures

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

### 5. Invalid CSV Format Errors

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

### 6. Authentication Failures

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

## Debug Mode

Enable comprehensive debugging to get detailed information:

### 1. Enable Debug Logging

Add to your application properties:

```properties
logging.level.org.openelisglobal.odoo=DEBUG
logging.level.org.apache.xmlrpc=DEBUG
```

### 2. Monitor Logs in Real-Time

```bash
# Follow logs in real-time
tail -f /var/lib/openelis-global/logs/openelis.log | grep -i odoo

# Or use journalctl if using systemd
journalctl -u openelis -f | grep -i odoo
```

### 3. Test Individual Components

```bash
# Test Odoo connectivity
curl -v http://odoo-server:8069/xmlrpc/2/common

# Test CSV file parsing
head -5 /var/lib/openelis-global/odoo/odoo-test-product-mapping.csv

# Test health endpoint
curl -v https://openelis-server:8443/health/odoo
```

## Performance Issues

### 1. Slow Invoice Creation

**Symptoms:**
- Long delays between sample creation and invoice creation
- Timeout errors

**Solutions:**

- **Network latency**: Optimize network connection
- **Odoo performance**: Check Odoo server performance
- **Connection pooling**: Verify connection reuse
- **Asynchronous processing**: Ensure events are processed asynchronously

### 2. High Memory Usage

**Symptoms:**
- OpenELIS using excessive memory
- Out of memory errors

**Solutions:**

- **Connection leaks**: Restart OpenELIS to clear connections
- **Large CSV files**: Optimize CSV mapping file size
- **Logging levels**: Reduce debug logging in production

## Security Issues

### 1. SSL/TLS Certificate Issues

**Symptoms:**
```
ERROR - SSL handshake failed
```

**Solutions:**

- **Self-signed certificates**: Add certificate to truststore
- **Expired certificates**: Renew SSL certificates
- **Wrong protocol**: Use HTTPS for secure communication

### 2. Authentication Security

**Symptoms:**
- Unauthorized access attempts
- Credential exposure

**Solutions:**

- **Strong passwords**: Use complex passwords
- **Regular rotation**: Rotate credentials regularly
- **Network security**: Restrict access to Odoo server
- **Audit logging**: Enable authentication logging

## Recovery Procedures

### 1. Complete Integration Reset

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

### 2. CSV Mapping Recovery

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

### 3. Odoo Connection Recovery

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

## Monitoring and Alerting

### 1. Health Check Monitoring

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

### 2. Log Monitoring

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

### 3. Performance Monitoring

Monitor integration performance:

```bash
# Check response times
time curl -s -k https://openelis-server:8443/health/odoo > /dev/null

# Monitor log file growth
ls -lh /var/lib/openelis-global/logs/openelis.log
```

## Getting Help

### 1. Collect Diagnostic Information

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

### 2. Contact Support

When contacting support, include:

- Description of the issue
- Steps to reproduce
- Diagnostic information (above)
- Error messages and logs
- Configuration details (without sensitive data)

### 3. Community Resources

- [OpenELIS Documentation](https://openelis-global.org)
- [Odoo Documentation](https://www.odoo.com/documentation)
- [GitHub Issues](https://github.com/DIGI-UW/OpenELIS-Global-2/issues)

## Related Documentation

- [Odoo Integration Overview](odoo-integration.md)
- [Quick Start Guide](odoo-quickstart.md)
- [Configuration Reference](odoo-configuration.md)
- [OpenELIS Troubleshooting](troubleshooting.md)

---

*This troubleshooting guide covers the most common issues with the Odoo-OpenELIS integration. For additional help, refer to the main integration documentation or contact the development team.*
