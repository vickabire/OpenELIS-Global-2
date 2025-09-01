# Odoo Integration Quick Start Guide

This guide will help you set up the Odoo-OpenELIS integration in 10 minutes.

## Prerequisites

- OpenELIS Global 2.0+ running
- Odoo 16.0+ running and accessible
- Network connectivity between systems

## Step 1: Configure Environment Variables

Add these to your OpenELIS configuration file or environment:

```properties
org.openelisglobal.odoo.baseUrl=http://your-odoo-server:8069
org.openelisglobal.odoo.database=your_odoo_database
org.openelisglobal.odoo.username=your_odoo_username
org.openelisglobal.odoo.password=your_odoo_password
```

## Step 2: Create Test-Product Mapping

Create file: `/var/lib/openelis-global/odoo/odoo-test-product-mapping.csv`

```csv
loinc_code,product_name,quantity,price_unit
58410-2,CBC Test,1,25.00
24323-8,Basic Metabolic Panel,1,35.00
24331-1,Lipid Panel,1,30.00
```

## Step 3: Restart OpenELIS

```bash
# If using Docker
docker-compose restart openelis

# If using standalone Tomcat
sudo systemctl restart tomcat
```

## Step 4: Verify Integration

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

## Step 5: Test the Integration

1. Create a new sample in OpenELIS with tests that have LOINC codes
2. Check Odoo for the automatically created invoice
3. Verify patient creation in Odoo

## Troubleshooting

### Health Check Fails

- Verify Odoo server is running
- Check network connectivity
- Validate credentials
- Ensure XML-RPC is enabled in Odoo

### No Invoices Created

- Check CSV mapping file exists and is readable
- Verify LOINC codes match between OpenELIS and CSV
- Review application logs for errors
- Ensure Odoo user has invoice creation permissions

### Missing Test Mappings

Add missing LOINC codes to the CSV file:

```csv
loinc_code,product_name,quantity,price_unit
YOUR-LOINC-CODE,Your Test Name,1,25.00
```

## Next Steps

- Review the [full documentation](odoo-integration.md) for advanced configuration
- Set up monitoring and alerting
- Configure backup procedures for mapping files
- Train staff on the new automated billing workflow

## Support

For issues or questions:
1. Check the [troubleshooting section](odoo-integration.md#troubleshooting)
2. Review application logs
3. Contact the development team

---

*This integration was developed as part of Google Summer of Code 2025.*
