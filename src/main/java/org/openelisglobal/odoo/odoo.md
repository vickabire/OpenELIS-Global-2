# OpenELIS-Global Odoo Integration

## Overview

The OpenELIS-Global Odoo integration enables seamless synchronization of laboratory test data between OpenELIS and Odoo's accounting and inventory management systems. This integration allows laboratories to automatically create sales orders, track inventory, and manage billing for laboratory tests through Odoo's robust ERP system. The integration is designed to be configurable and maintainable, with clear separation of concerns between the two systems.

The integration operates on a push-based model, where OpenELIS initiates the synchronization process when tests are completed or results are verified. This ensures that Odoo's system always has the most up-to-date information about laboratory operations while maintaining data integrity and consistency across both systems.

## Components

### Test Product Mapping
The `TestProductMapping` class serves as the cornerstone of the integration, maintaining the relationship between OpenELIS tests and their corresponding Odoo products. It uses a configuration-based approach where each OpenELIS test is mapped to an Odoo product ID and price. This mapping is stored in the application properties and loaded at runtime, allowing for easy updates without code changes. The class provides methods to retrieve product IDs and prices for tests, which are used when creating sales orders in Odoo.

### Odoo Client Configuration
The Odoo client configuration manages the connection parameters and authentication details required to communicate with the Odoo server. This includes the server URL, database name, username, and API key. The configuration is externalized through application properties, making it easy to switch between different Odoo environments (development, staging, production) without code changes.

### XML-RPC Communication
The integration uses Odoo's XML-RPC API for all communication between OpenELIS and Odoo. This protocol allows for secure, remote procedure calls to create and update records in Odoo. The XML-RPC client is configured to handle authentication, connection pooling, and error handling, ensuring reliable communication between the systems.

### Sales Order Creation
When a test is completed in OpenELIS, the integration creates a corresponding sales order in Odoo. This process includes mapping the test to the correct product, setting the appropriate price, and including all necessary customer and billing information. The sales order creation is transactional, ensuring that either both systems are updated or neither is, maintaining data consistency.

### Inventory Management
The integration tracks laboratory supplies and reagents through Odoo's inventory management system. When tests are performed, the system automatically updates the inventory levels in Odoo, triggering reorder points when necessary. This ensures that the laboratory always has the necessary supplies on hand while maintaining accurate inventory records.

### Error Handling and Logging
A comprehensive error handling and logging system ensures that any issues during the integration process are properly recorded and can be investigated. The system includes automatic retry mechanisms for transient failures and detailed logging of all integration activities. This makes it easier to diagnose and resolve any issues that may arise during operation.

### Configuration Management
The integration's configuration is managed through external properties files, allowing for easy updates without code changes. This includes mappings between OpenELIS and Odoo entities, connection parameters, and business rules. The configuration is version-controlled and can be different for each environment.

## Usage

To use the Odoo integration:

1. Configure the Odoo connection parameters in `application.properties`:
```properties
odoo.server.url=http://your-odoo-server
odoo.database.name=your_database
odoo.username=your_username
odoo.api.key=your_api_key
```

2. Set up the test-to-product mappings:
```properties
odoo.test.product.mapping=test1=1,test2=2,test3=3
odoo.test.price.mapping=test1=100.0,test2=150.0,test3=200.0
```

3. Enable the integration in OpenELIS by setting:
```properties
odoo.integration.enabled=true
```

## Security Considerations

- All communication with Odoo is done over HTTPS
- API keys are stored securely and never logged
- Access to Odoo is restricted to specific IP addresses
- All sensitive data is encrypted in transit
- Regular security audits are performed on the integration

## Troubleshooting

Common issues and their solutions:

1. Connection Issues
   - Verify Odoo server is accessible
   - Check network connectivity
   - Validate credentials

2. Mapping Issues
   - Ensure test IDs exist in both systems
   - Verify product mappings are correct
   - Check price mappings are up to date

3. Data Synchronization Issues
   - Check logs for specific error messages
   - Verify data consistency between systems
   - Ensure proper error handling is in place

## Maintenance

Regular maintenance tasks:

1. Monitor integration logs for errors
2. Update test-to-product mappings as needed
3. Review and update security configurations
4. Perform regular testing of the integration
5. Keep Odoo API client libraries up to date

## Future Enhancements

Planned improvements:

1. Real-time inventory synchronization
2. Automated reordering of supplies
3. Enhanced reporting capabilities
4. Support for batch operations
5. Improved error recovery mechanisms 
