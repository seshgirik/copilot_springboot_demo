-- Insert test users with passwords (encoded with BCrypt)
-- Password for all users is 'password123'
INSERT INTO users (name, email, phone, password, role) VALUES 
('John Doe', 'john.doe@example.com', '1234567890', '$2a$10$kFN3LhYL8ozkCuLDi0fLouY1JlPnqqGZaWptEvDRLdY.vlBpa2aAC', 'USER'),
('Jane Smith', 'jane.smith@example.com', '0987654321', '$2a$10$kFN3LhYL8ozkCuLDi0fLouY1JlPnqqGZaWptEvDRLdY.vlBpa2aAC', 'USER'),
('Alice Johnson', 'alice.johnson@example.com', '1122334455', '$2a$10$kFN3LhYL8ozkCuLDi0fLouY1JlPnqqGZaWptEvDRLdY.vlBpa2aAC', 'ADMIN'),
('Bob Wilson', 'bob.wilson@example.com', '5566778899', '$2a$10$kFN3LhYL8ozkCuLDi0fLouY1JlPnqqGZaWptEvDRLdY.vlBpa2aAC', 'USER'),
('Charlie Brown', 'charlie.brown@example.com', '9988776655', '$2a$10$kFN3LhYL8ozkCuLDi0fLouY1JlPnqqGZaWptEvDRLdY.vlBpa2aAC', 'USER');

-- Insert test products
INSERT INTO products (name, description, price, quantity) VALUES 
('Laptop', 'High-performance laptop for gaming and work', 999.99, 10),
('Smartphone', 'Latest smartphone with advanced features', 599.99, 25),
('Headphones', 'Wireless noise-cancelling headphones', 199.99, 50),
('Keyboard', 'Mechanical gaming keyboard with RGB lighting', 149.99, 30),
('Mouse', 'Ergonomic wireless mouse', 79.99, 40),
('Monitor', '27-inch 4K gaming monitor', 449.99, 15),
('Tablet', '10-inch tablet for productivity and entertainment', 329.99, 20),
('Webcam', 'HD webcam for video conferencing', 89.99, 35);
