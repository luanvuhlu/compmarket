-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    category_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_category_id BIGINT,
    slug VARCHAR(255) NOT NULL UNIQUE,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    product_id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    discount_price DECIMAL(10, 2),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    brand VARCHAR(100),
    model VARCHAR(100),
    specifications JSONB,
    images JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    search_vector tsvector,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE RESTRICT
);

-- Create indexes for products
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_products_search_vector ON products USING GIN(search_vector);

-- Create trigger to update search_vector
CREATE OR REPLACE FUNCTION products_search_vector_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.brand, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER products_search_vector_trigger
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION products_search_vector_update();

-- Create indexes for categories
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_parent_id ON categories(parent_category_id);
