-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP,
    gateway_response JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE RESTRICT,
    CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'))
);

-- Create admin_users table
CREATE TABLE IF NOT EXISTS admin_users (
    admin_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    permissions JSONB,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_admin_users_user_id ON admin_users(user_id);
