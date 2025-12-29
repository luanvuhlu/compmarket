-- Create addresses table
CREATE TABLE IF NOT EXISTS addresses (
    address_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address_type VARCHAR(20) NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CHECK (address_type IN ('SHIPPING', 'BILLING'))
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    order_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    shipping_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10, 2),
    shipping_address_id BIGINT,
    billing_address_id BIGINT,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (shipping_address_id) REFERENCES addresses(address_id) ON DELETE SET NULL,
    FOREIGN KEY (billing_address_id) REFERENCES addresses(address_id) ON DELETE SET NULL,
    CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'))
);

-- Create order_items table
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
);

-- Create indexes
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
