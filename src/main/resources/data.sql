-- ===================================================================
-- SEED DATA: Categories & Items (Development Profile Only)
-- ===================================================================
-- Users are seeded via DataSeeder.java (BCrypt requires Java encoding)

-- Seed Categories
INSERT INTO categories (name) VALUES ('Electronics');
INSERT INTO categories (name) VALUES ('Furniture');
INSERT INTO categories (name) VALUES ('Office Supplies');

-- Seed Items (linked to categories)
INSERT INTO items (name, sku, price, quantity, category_id) VALUES ('MacBook Pro 16"', 'ELEC-001', 2499.99, 25, 1);
INSERT INTO items (name, sku, price, quantity, category_id) VALUES ('Wireless Mouse', 'ELEC-002', 29.99, 150, 1);
INSERT INTO items (name, sku, price, quantity, category_id) VALUES ('Standing Desk', 'FURN-001', 599.99, 40, 2);
INSERT INTO items (name, sku, price, quantity, category_id) VALUES ('Ergonomic Chair', 'FURN-002', 449.99, 60, 2);
INSERT INTO items (name, sku, price, quantity, category_id) VALUES ('A4 Paper Ream', 'OFF-001', 8.99, 500, 3);
