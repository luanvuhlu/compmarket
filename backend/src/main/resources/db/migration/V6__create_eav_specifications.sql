-- Create attribute_definitions table
CREATE TABLE IF NOT EXISTS attribute_definitions (
    attribute_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    data_type VARCHAR(20) NOT NULL CHECK (data_type IN ('STRING', 'NUMERIC', 'BOOLEAN', 'ENUM')),
    unit VARCHAR(50),
    is_filterable BOOLEAN NOT NULL DEFAULT true,
    is_searchable BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create attribute_options table (for ENUM type attributes)
CREATE TABLE IF NOT EXISTS attribute_options (
    option_id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL,
    option_value VARCHAR(255) NOT NULL,
    display_label VARCHAR(255) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attribute_id) REFERENCES attribute_definitions(attribute_id) ON DELETE CASCADE,
    UNIQUE (attribute_id, option_value)
);

-- Create product_specifications table (EAV pattern)
CREATE TABLE IF NOT EXISTS product_specifications (
    spec_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    attribute_id BIGINT NOT NULL,
    value_string VARCHAR(500),
    value_numeric DECIMAL(15, 4),
    value_boolean BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (attribute_id) REFERENCES attribute_definitions(attribute_id) ON DELETE CASCADE,
    UNIQUE (product_id, attribute_id)
);

-- Create indexes for performance
CREATE INDEX idx_product_specifications_product_id ON product_specifications(product_id);
CREATE INDEX idx_product_specifications_attribute_id ON product_specifications(attribute_id);
CREATE INDEX idx_product_specifications_value_numeric ON product_specifications(value_numeric) WHERE value_numeric IS NOT NULL;
CREATE INDEX idx_product_specifications_value_string ON product_specifications(value_string) WHERE value_string IS NOT NULL;
CREATE INDEX idx_attribute_definitions_filterable ON attribute_definitions(is_filterable) WHERE is_filterable = true;
CREATE INDEX idx_attribute_options_attribute_id ON attribute_options(attribute_id);

-- Insert common attribute definitions for computer products
INSERT INTO attribute_definitions (name, display_name, data_type, unit, is_filterable, is_searchable, sort_order) VALUES
    ('processor', 'Processor', 'STRING', NULL, true, true, 1),
    ('processor_brand', 'Processor Brand', 'ENUM', NULL, true, false, 2),
    ('ram_size', 'RAM Size', 'NUMERIC', 'GB', true, false, 3),
    ('ram_type', 'RAM Type', 'ENUM', NULL, true, false, 4),
    ('storage_capacity', 'Storage Capacity', 'NUMERIC', 'GB', true, false, 5),
    ('storage_type', 'Storage Type', 'ENUM', NULL, true, false, 6),
    ('graphics_card', 'Graphics Card', 'STRING', NULL, true, true, 7),
    ('screen_size', 'Screen Size', 'NUMERIC', 'inches', true, false, 8),
    ('screen_resolution', 'Screen Resolution', 'ENUM', NULL, true, false, 9),
    ('operating_system', 'Operating System', 'ENUM', NULL, true, false, 10),
    ('warranty', 'Warranty', 'NUMERIC', 'months', false, false, 11),
    ('color', 'Color', 'ENUM', NULL, true, false, 12),
    ('weight', 'Weight', 'NUMERIC', 'kg', false, false, 13),
    ('ports', 'Ports', 'STRING', NULL, false, false, 14),
    ('wireless', 'Wireless Connectivity', 'STRING', NULL, false, true, 15);

-- Insert attribute options for ENUM types
INSERT INTO attribute_options (attribute_id, option_value, display_label, sort_order) VALUES
    -- Processor Brand options
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), 'intel', 'Intel', 1),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), 'amd', 'AMD', 2),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), 'apple', 'Apple', 3),
    
    -- RAM Type options
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'ddr4', 'DDR4', 1),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'ddr5', 'DDR5', 2),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'lpddr4', 'LPDDR4', 3),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'lpddr5', 'LPDDR5', 4),
    
    -- Storage Type options
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'ssd', 'SSD', 1),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'nvme', 'NVMe SSD', 2),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'hdd', 'HDD', 3),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'hybrid', 'Hybrid (SSD + HDD)', 4),
    
    -- Screen Resolution options
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '1920x1080', 'Full HD (1920x1080)', 1),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '2560x1440', 'QHD (2560x1440)', 2),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '3840x2160', '4K UHD (3840x2160)', 3),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '2560x1600', 'WQXGA (2560x1600)', 4),
    
    -- Operating System options
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'windows_11', 'Windows 11', 1),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'windows_10', 'Windows 10', 2),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'macos', 'macOS', 3),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'linux', 'Linux', 4),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'chrome_os', 'Chrome OS', 5),
    
    -- Color options
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', 'Black', 1),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'silver', 'Silver', 2),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'gray', 'Gray', 3),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'white', 'White', 4),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'blue', 'Blue', 5),
    ((SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'red', 'Red', 6);

-- Add comment to explain the EAV pattern
COMMENT ON TABLE product_specifications IS 'EAV (Entity-Attribute-Value) table for flexible product specifications';
COMMENT ON TABLE attribute_definitions IS 'Defines available attributes with data types, units, and filtering options';
COMMENT ON TABLE attribute_options IS 'Predefined options for ENUM type attributes';
