-- =====================================
-- Database for BookMart Online
-- =====================================

CREATE DATABASE IF NOT EXISTS bookmartdb;
USE bookmartdb;

-- =====================================
-- USERS
-- Roles: Admin, Staff, Customer
-- =====================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('Admin', 'Staff', 'Customer') NOT NULL DEFAULT 'Customer'
);

-- =====================================
-- BOOKS catalog
-- =====================================
CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    genre VARCHAR(100),
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0
);

-- =====================================
-- CURRENCIES
-- to support PHP, USD, KRW
-- =====================================
CREATE TABLE currencies (
    currency_id INT AUTO_INCREMENT PRIMARY KEY,
    currency_code VARCHAR(10) NOT NULL UNIQUE,
    symbol VARCHAR(5),
    exchange_rate_to_php DECIMAL(10,4)
);

-- =====================================
-- CART (temporary cart per customer)
-- before checkout / placing order
-- =====================================
CREATE TABLE cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    book_id INT,
    quantity INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- =====================================
-- ORDERS: summary per order
-- =====================================
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2),
    currency_id INT,
    status VARCHAR(50) DEFAULT 'Pending',
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (currency_id) REFERENCES currencies(currency_id)
);

-- =====================================
-- ORDER ITEMS: each book in an order
-- =====================================
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    book_id INT,
    quantity INT,
    price_each DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- =====================================
-- TRANSACTION LOGS: for payments & audit
-- =====================================
CREATE TABLE transaction_logs (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    payment_method VARCHAR(50),
    payment_status VARCHAR(50),
    amount DECIMAL(10,2),
    timestambooksusersp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);


-- Users
INSERT INTO users (name, email, password, role) VALUES
('Jericho', 'jericho@email.com', 'jericho123', 'Admin'),
('AC', 'AC@email.com', 'AC12345', 'Staff'),
('Luis', 'Luis@email.com', 'Luis12345', 'Staff'),
('Miguel', 'Miguel@email.com', 'Miguel12345', 'Staff'),
('JohnDoe', 'JohnDoe@bookmart.com', 'JohnDoe12345', 'Customer');

-- Books
INSERT INTO books (title, genre, price, stock_quantity) VALUES
('Noli Me Tangere', 'Classic', 299.00, 20),
('El Filibusterismo', 'Classic', 280.00, 15),
('Psychology 101', 'Academic', 500.00, 10),
('One Piece Vol.1', 'Manga', 250.00, 50),
('Detective Conan', 'Mystery', 230.00, 40),
('Romance Novel', 'Romance', 350.00, 25);

-- Currencies
INSERT INTO currencies (currency_code, symbol, exchange_rate_to_php) VALUES
('PHP', '₱', 1.0000),
('USD', '$', 55.0000),
('KRW', '₩', 0.0420);
