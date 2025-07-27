-- Insert sample users with enhanced security fields
INSERT INTO users (name, email, phone, password, role, failed_login_attempts, account_non_locked) VALUES
('John Doe', 'john@example.com', '1234567890', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER', 0, true),
('Jane Smith', 'jane@example.com', '0987654321', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER', 0, true),
('Admin User', 'admin@example.com', '5555555555', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 0, true);

-- Insert sample products
INSERT INTO products (name, description, price, quantity) VALUES
('Laptop', 'High-performance laptop', 999.99, 10),
('Smartphone', 'Latest smartphone model', 699.99, 15),
('Tablet', '10-inch tablet', 299.99, 20),
('Headphones', 'Wireless noise-canceling headphones', 199.99, 25),
('Mouse', 'Wireless optical mouse', 29.99, 50);
