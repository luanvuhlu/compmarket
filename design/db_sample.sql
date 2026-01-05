-- =====================================================
-- INSERT SAMPLE USERS (All Roles)
-- =====================================================
-- Note: Password is 'password123' hashed with BCrypt
-- You'll need to generate proper BCrypt hashes for production

-- Customer Users
INSERT INTO users (email, password_hash, first_name, last_name, phone, is_active, email_verified) VALUES
('customer1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John', 'Doe', '0901234567', true, true),
('customer2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane', 'Smith', '0901234568', true, true),
('customer3@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob', 'Johnson', '0901234569', true, false);

-- Admin User
INSERT INTO users (email, password_hash, first_name, last_name, phone, is_active, email_verified) VALUES
('admin@gearvn.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', '0909999999', true, true);

-- Super Admin User
INSERT INTO users (email, password_hash, first_name, last_name, phone, is_active, email_verified) VALUES
('superadmin@gearvn.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Super', 'Admin', '0909999998', true, true);

-- Assign Roles to Users
-- Customer roles (user_id 1, 2, 3)
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- customer1 -> CUSTOMER
(2, 1), -- customer2 -> CUSTOMER
(3, 1); -- customer3 -> CUSTOMER

-- Admin role (user_id 4)
INSERT INTO user_roles (user_id, role_id) VALUES
(4, 2); -- admin -> ADMIN

-- Super Admin role (user_id 5)
INSERT INTO user_roles (user_id, role_id) VALUES
(5, 3); -- superadmin -> SUPER_ADMIN


-- =====================================================
-- INSERT SAMPLE CATEGORIES
-- =====================================================
INSERT INTO categories (name, description, slug, image_url) VALUES
('Laptops', 'Gaming and Business Laptops', 'laptops', 'https://example.com/images/laptops.jpg'),
('Desktop PCs', 'Custom Built Desktop Computers', 'desktop-pcs', 'https://example.com/images/desktops.jpg'),
('Components', 'PC Hardware Components', 'components', 'https://example.com/images/components.jpg'),
('Peripherals', 'Gaming Peripherals and Accessories', 'peripherals', 'https://example.com/images/peripherals.jpg'),
('Monitors', 'Gaming and Professional Monitors', 'monitors', 'https://example.com/images/monitors.jpg');


-- =====================================================
-- INSERT SAMPLE PRODUCTS
-- =====================================================

-- Laptops
INSERT INTO products (category_id, name, description, sku, price, discount_price, stock_quantity, brand, model, specifications, is_active) VALUES
(1, 'ASUS ROG Strix G15', 'High-performance gaming laptop with RTX 4060, 15.6" 165Hz display', 'LAP-ASUS-001', 1299.99, 1199.99, 15, 'ASUS', 'G513RM', '{"cpu": "AMD Ryzen 7 6800H", "ram": "16GB DDR5", "storage": "512GB NVMe SSD", "gpu": "RTX 4060 6GB"}', true),
(1, 'MSI Katana 15', 'Budget gaming laptop with RTX 4050, 144Hz display', 'LAP-MSI-001', 899.99, 849.99, 20, 'MSI', 'Katana 15 B13V', '{"cpu": "Intel Core i7-13620H", "ram": "16GB DDR5", "storage": "512GB NVMe SSD", "gpu": "RTX 4050 6GB"}', true),
(1, 'Lenovo Legion 5 Pro', 'Premium gaming laptop with RTX 4070, 16" WQXGA display', 'LAP-LEN-001', 1599.99, null, 10, 'Lenovo', 'Legion 5 Pro', '{"cpu": "AMD Ryzen 7 7745HX", "ram": "32GB DDR5", "storage": "1TB NVMe SSD", "gpu": "RTX 4070 8GB"}', true);

-- Desktop PCs
INSERT INTO products (category_id, name, description, sku, price, discount_price, stock_quantity, brand, model, specifications, is_active) VALUES
(2, 'Gaming PC RTX 4060 Ti', 'Mid-range gaming desktop for 1440p gaming', 'PC-CUSTOM-001', 1199.99, 1099.99, 8, 'Custom Build', 'Gaming Pro', '{"cpu": "Intel Core i5-13400F", "ram": "16GB DDR4", "storage": "500GB NVMe + 1TB HDD", "gpu": "RTX 4060 Ti 8GB", "psu": "650W 80+ Gold"}', true),
(2, 'Workstation PC RTX 4000', 'Professional workstation for content creation', 'PC-WORK-001', 2499.99, null, 5, 'Custom Build', 'Pro Station', '{"cpu": "AMD Ryzen 9 7950X", "ram": "64GB DDR5", "storage": "2TB NVMe SSD", "gpu": "RTX 4000 Ada 20GB", "psu": "850W 80+ Platinum"}', true);

-- Components
INSERT INTO products (category_id, name, description, sku, price, discount_price, stock_quantity, brand, model, specifications, is_active) VALUES
(3, 'AMD Ryzen 7 7800X3D', 'High-performance gaming CPU with 3D V-Cache', 'CPU-AMD-001', 449.99, 429.99, 25, 'AMD', 'Ryzen 7 7800X3D', '{"cores": 8, "threads": 16, "base_clock": "4.2 GHz", "boost_clock": "5.0 GHz", "cache": "96MB"}', true),
(3, 'NVIDIA GeForce RTX 4070 Super', 'High-end graphics card for 1440p and 4K gaming', 'GPU-NV-001', 599.99, 579.99, 12, 'NVIDIA', 'RTX 4070 Super', '{"memory": "12GB GDDR6X", "memory_bus": "192-bit", "cuda_cores": 7168, "boost_clock": "2475 MHz"}', true),
(3, 'Corsair Vengeance DDR5 32GB', 'High-speed DDR5 RAM kit (2x16GB)', 'RAM-COR-001', 129.99, 119.99, 30, 'Corsair', 'Vengeance DDR5', '{"capacity": "32GB", "speed": "6000MHz", "cas_latency": "CL30", "modules": 2}', true),
(3, 'Samsung 980 PRO 1TB', 'PCIe 4.0 NVMe M.2 SSD', 'SSD-SAM-001', 109.99, 99.99, 40, 'Samsung', '980 PRO', '{"capacity": "1TB", "interface": "PCIe 4.0 x4", "read_speed": "7000 MB/s", "write_speed": "5000 MB/s"}', true);

-- Peripherals
INSERT INTO products (category_id, name, description, sku, price, discount_price, stock_quantity, brand, model, specifications, is_active) VALUES
(4, 'Logitech G Pro X Superlight', 'Wireless gaming mouse with HERO 25K sensor', 'MOUSE-LOG-001', 149.99, 139.99, 35, 'Logitech', 'G Pro X Superlight', '{"sensor": "HERO 25K", "dpi": "100-25600", "weight": "63g", "battery": "70 hours"}', true),
(4, 'Razer BlackWidow V3 Pro', 'Wireless mechanical gaming keyboard', 'KB-RAZ-001', 229.99, 199.99, 20, 'Razer', 'BlackWidow V3 Pro', '{"switches": "Razer Green Mechanical", "connection": "2.4GHz + Bluetooth", "battery": "200 hours", "rgb": true}', true),
(4, 'HyperX Cloud Alpha Wireless', 'Wireless gaming headset with 300hr battery', 'HEAD-HYP-001', 199.99, 179.99, 18, 'HyperX', 'Cloud Alpha Wireless', '{"driver": "50mm", "frequency": "15Hz-21kHz", "battery": "300 hours", "connection": "2.4GHz"}', true);

-- Monitors
INSERT INTO products (category_id, name, description, sku, price, discount_price, stock_quantity, brand, model, specifications, is_active) VALUES
(5, 'ASUS ROG Swift PG279QM', '27" 1440p 240Hz gaming monitor', 'MON-ASUS-001', 699.99, 649.99, 12, 'ASUS', 'PG279QM', '{"size": "27 inch", "resolution": "2560x1440", "refresh_rate": "240Hz", "panel": "IPS", "response_time": "1ms"}', true),
(5, 'LG UltraGear 27GN950', '27" 4K 144Hz gaming monitor with HDR', 'MON-LG-001', 799.99, 749.99, 10, 'LG', '27GN950', '{"size": "27 inch", "resolution": "3840x2160", "refresh_rate": "144Hz", "panel": "IPS", "hdr": "DisplayHDR 600"}', true),
(5, 'Samsung Odyssey G7', '32" 1440p 240Hz curved gaming monitor', 'MON-SAM-001', 599.99, 549.99, 15, 'Samsung', 'Odyssey G7', '{"size": "32 inch", "resolution": "2560x1440", "refresh_rate": "240Hz", "panel": "VA", "curvature": "1000R"}', true);


-- =====================================================
-- INSERT SAMPLE PRODUCT SPECIFICATIONS (EAV Pattern)
-- =====================================================

-- Product 1: ASUS ROG Strix G15 Laptop
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor'), 'AMD Ryzen 7 6800H', NULL, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), NULL, NULL, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_size'), NULL, 16, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), NULL, NULL, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_capacity'), NULL, 512, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), NULL, NULL, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'graphics_card'), 'NVIDIA RTX 4060 6GB', NULL, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_size'), NULL, 15.6, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), NULL, NULL, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), NULL, NULL, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 24, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), NULL, NULL, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'weight'), NULL, 2.3, NULL),
(1, (SELECT attribute_id FROM attribute_definitions WHERE name = 'wireless'), 'Wi-Fi 6E, Bluetooth 5.2', NULL, NULL);

-- Update enum values for Product 1
UPDATE product_specifications SET value_string = 'amd' WHERE product_id = 1 AND attribute_id = (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand');
UPDATE product_specifications SET value_string = 'ddr5' WHERE product_id = 1 AND attribute_id = (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type');
UPDATE product_specifications SET value_string = 'nvme' WHERE product_id = 1 AND attribute_id = (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type');
UPDATE product_specifications SET value_string = '1920x1080' WHERE product_id = 1 AND attribute_id = (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution');
UPDATE product_specifications SET value_string = 'windows_11' WHERE product_id = 1 AND attribute_id = (SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system');
UPDATE product_specifications SET value_string = 'black' WHERE product_id = 1 AND attribute_id = (SELECT attribute_id FROM attribute_definitions WHERE name = 'color');

-- Product 2: MSI Katana 15 Laptop
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor'), 'Intel Core i7-13620H', NULL, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), 'intel', NULL, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_size'), NULL, 16, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'ddr5', NULL, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_capacity'), NULL, 512, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'nvme', NULL, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'graphics_card'), 'NVIDIA RTX 4050 6GB', NULL, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_size'), NULL, 15.6, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '1920x1080', NULL, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'windows_11', NULL, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 24, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'weight'), NULL, 2.2, NULL),
(2, (SELECT attribute_id FROM attribute_definitions WHERE name = 'wireless'), 'Wi-Fi 6, Bluetooth 5.1', NULL, NULL);

-- Product 3: Lenovo Legion 5 Pro Laptop
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor'), 'AMD Ryzen 7 7745HX', NULL, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), 'amd', NULL, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_size'), NULL, 32, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'ddr5', NULL, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_capacity'), NULL, 1024, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'nvme', NULL, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'graphics_card'), 'NVIDIA RTX 4070 8GB', NULL, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_size'), NULL, 16, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '2560x1600', NULL, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'windows_11', NULL, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 24, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'gray', NULL, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'weight'), NULL, 2.5, NULL),
(3, (SELECT attribute_id FROM attribute_definitions WHERE name = 'wireless'), 'Wi-Fi 6E, Bluetooth 5.2', NULL, NULL);

-- Product 4: Gaming PC RTX 4060 Ti
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor'), 'Intel Core i5-13400F', NULL, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), 'intel', NULL, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_size'), NULL, 16, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'ddr4', NULL, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_capacity'), NULL, 500, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'hybrid', NULL, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'graphics_card'), 'NVIDIA RTX 4060 Ti 8GB', NULL, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'windows_11', NULL, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 36, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ports'), 'USB 3.2, USB-C, HDMI 2.1, DisplayPort 1.4', NULL, NULL),
(4, (SELECT attribute_id FROM attribute_definitions WHERE name = 'wireless'), 'Wi-Fi 6, Bluetooth 5.2', NULL, NULL);

-- Product 5: Workstation PC RTX 4000
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor'), 'AMD Ryzen 9 7950X', NULL, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), 'amd', NULL, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_size'), NULL, 64, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'ddr5', NULL, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_capacity'), NULL, 2048, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'nvme', NULL, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'graphics_card'), 'NVIDIA RTX 4000 Ada 20GB', NULL, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'operating_system'), 'windows_11', NULL, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 36, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ports'), 'USB 3.2, USB-C, Thunderbolt 4, HDMI 2.1, DisplayPort 1.4', NULL, NULL),
(5, (SELECT attribute_id FROM attribute_definitions WHERE name = 'wireless'), 'Wi-Fi 6E, Bluetooth 5.3', NULL, NULL);

-- Product 6: AMD Ryzen 7 7800X3D (CPU Component)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(6, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor'), 'AMD Ryzen 7 7800X3D', NULL, NULL),
(6, (SELECT attribute_id FROM attribute_definitions WHERE name = 'processor_brand'), 'amd', NULL, NULL),
(6, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 36, NULL);

-- Product 7: NVIDIA GeForce RTX 4070 Super (GPU Component)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(7, (SELECT attribute_id FROM attribute_definitions WHERE name = 'graphics_card'), 'NVIDIA RTX 4070 Super 12GB GDDR6X', NULL, NULL),
(7, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 36, NULL),
(7, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ports'), 'HDMI 2.1, 3x DisplayPort 1.4a', NULL, NULL);

-- Product 8: Corsair Vengeance DDR5 32GB (RAM Component)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(8, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_size'), NULL, 32, NULL),
(8, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ram_type'), 'ddr5', NULL, NULL),
(8, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 60, NULL),
(8, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL);

-- Product 9: Samsung 980 PRO 1TB (SSD Component)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(9, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_capacity'), NULL, 1024, NULL),
(9, (SELECT attribute_id FROM attribute_definitions WHERE name = 'storage_type'), 'nvme', NULL, NULL),
(9, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 60, NULL);

-- Product 10: Logitech G Pro X Superlight (Mouse)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(10, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 24, NULL),
(10, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL),
(10, (SELECT attribute_id FROM attribute_definitions WHERE name = 'weight'), NULL, 0.063, NULL),
(10, (SELECT attribute_id FROM attribute_definitions WHERE name = 'wireless'), '2.4GHz Wireless', NULL, NULL);

-- Product 11: Razer BlackWidow V3 Pro (Keyboard)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(11, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 24, NULL),
(11, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL),
(11, (SELECT attribute_id FROM attribute_definitions WHERE name = 'wireless'), '2.4GHz + Bluetooth 5.0', NULL, NULL);

-- Product 12: HyperX Cloud Alpha Wireless (Headset)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(12, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 24, NULL),
(12, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL),
(12, (SELECT attribute_id FROM attribute_definitions WHERE name = 'wireless'), '2.4GHz Wireless', NULL, NULL);

-- Product 13: ASUS ROG Swift PG279QM (Monitor)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(13, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_size'), NULL, 27, NULL),
(13, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '2560x1440', NULL, NULL),
(13, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 36, NULL),
(13, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL),
(13, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ports'), 'HDMI 2.0, DisplayPort 1.4, USB 3.0 Hub', NULL, NULL);

-- Product 14: LG UltraGear 27GN950 (Monitor)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(14, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_size'), NULL, 27, NULL),
(14, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '3840x2160', NULL, NULL),
(14, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 36, NULL),
(14, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'white', NULL, NULL),
(14, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ports'), 'HDMI 2.1, DisplayPort 1.4, USB 3.0 Hub', NULL, NULL);

-- Product 15: Samsung Odyssey G7 (Monitor)
INSERT INTO product_specifications (product_id, attribute_id, value_string, value_numeric, value_boolean) VALUES
(15, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_size'), NULL, 32, NULL),
(15, (SELECT attribute_id FROM attribute_definitions WHERE name = 'screen_resolution'), '2560x1440', NULL, NULL),
(15, (SELECT attribute_id FROM attribute_definitions WHERE name = 'warranty'), NULL, 36, NULL),
(15, (SELECT attribute_id FROM attribute_definitions WHERE name = 'color'), 'black', NULL, NULL),
(15, (SELECT attribute_id FROM attribute_definitions WHERE name = 'ports'), 'HDMI 2.0, DisplayPort 1.4, USB 3.0 Hub', NULL, NULL);