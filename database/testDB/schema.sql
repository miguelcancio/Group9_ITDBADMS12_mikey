
-- Database for BookMart Online


CREATE DATABASE IF NOT EXISTS bookmartdb;
USE bookmartdb;

-- USERS
-- Roles: Admin, Staff, Customer

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('Admin', 'Staff', 'Customer') NOT NULL DEFAULT 'Customer'
);

-- BOOKS catalog

CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    genre VARCHAR(100),
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0
);


-- CURRENCIES
-- to support PHP, USD, KRW

CREATE TABLE currencies (
    currency_id INT AUTO_INCREMENT PRIMARY KEY,
    currency_code VARCHAR(10) NOT NULL UNIQUE,
    symbol VARCHAR(5),
    exchange_rate_to_php DECIMAL(10,4)
);


-- CART (temporary cart per customer)
-- before checkout / placing order

CREATE TABLE cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    book_id INT,
    quantity INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);


-- ORDERS: summary per order

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


-- ORDER ITEMS: each book in an order

CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    book_id INT,
    quantity INT,
    price_each DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);


-- TRANSACTION LOGS: for payments & audit
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

-- Currencies --
INSERT INTO currencies (currency_code, symbol, exchange_rate_to_php) VALUES
('PHP', '₱', 1.0000),
('USD', '$', 55.0000),
('KRW', '₩', 0.0420);


-- DELETE FROM currencies WHERE currency_code = 'USD'; --
-- DELETE FROM currencies WHERE currency_code = 'KRW'; --

-- Stored Procedures --

DELIMITER $$
CREATE PROCEDURE getAllBooks()
BEGIN
    SELECT * FROM books;
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE getAllUsers()
BEGIN
    SELECT * FROM users;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getBookDetails(IN bookId INT)
BEGIN
    SELECT * FROM books WHERE book_id = bookId;
END $$
DELIMITER ;

 
DELIMITER $$ 
CREATE PROCEDURE getCart(IN user_id INT)
BEGIN
    SELECT c.cart_item_id, b.title, c.quantity, b.price, (b.price * c.quantity) AS total
    FROM cart_items c
    JOIN books b ON c.book_id = b.book_id
    WHERE c.user_id = user_id;
END $$
DELIMITER ;

DELIMITER $$ 
CREATE PROCEDURE addToCart(IN user_id INT, IN book_id INT, IN qty INT)
BEGIN
    DECLARE existingQty INT;
    SELECT quantity INTO existingQty
    FROM cart_items
    WHERE user_id = user_id AND book_id = book_id;
    IF existingQty IS NOT NULL THEN
        UPDATE cart_items
        SET quantity = quantity + qty
        WHERE user_id = user_id AND book_id = book_id;
    ELSE
        INSERT INTO cart_items (user_id, book_id, quantity)
        VALUES (user_id, book_id, qty);
    END IF;
END $$
DELIMITER ;

DELIMITER $$ 
CREATE PROCEDURE getOrders(IN user_id INT)
BEGIN
    SELECT o.*, c.currency_code
    FROM orders o
    JOIN currencies c ON o.currency_id = c.currency_id
    WHERE o.user_id = user_id;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getUserOrderHistory(IN userId INT)
BEGIN
    SELECT o.order_id, o.order_date, o.total_amount, c.currency_code, o.status
    FROM orders o
    JOIN currencies c ON o.currency_id = c.currency_id
    WHERE o.user_id = userId;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE updateBookPrice(IN bookId INT, IN newPrice DECIMAL(10,2))
BEGIN
    UPDATE books
    SET price = newPrice
    WHERE book_id = bookId;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE updateStockQuantity(IN bookId INT, IN newStock INT)
BEGIN
    UPDATE books
    SET stock_quantity = newStock
    WHERE book_id = bookId;
END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE removeFromCart(IN userId INT, IN bookId INT)
BEGIN
    DELETE FROM cart_items
    WHERE user_id = userId AND book_id = bookId;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE clearCart(IN userId INT)
BEGIN
    DELETE FROM cart_items
    WHERE user_id = userId;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE removeBooks(IN bookId INT)
BEGIN
    DELETE FROM books
    WHERE book_id = bookId;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE removeUsers(IN userId INT)
BEGIN
    DELETE FROM users
    WHERE user_id = userId;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE addUsers(IN userName VARCHAR(100), IN userEmail Varchar(100), IN userPass VARCHAR(100))
BEGIN
INSERT INTO users (name, email, password) VALUES
(userName, userEmail, userPass);
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE addBooks(IN bookName VARCHAR(200), IN genre Varchar(100), IN bookPrice DECIMAL(10, 2), IN bookStock INT)
BEGIN
INSERT INTO books (title, genre, price, stock_quantity) VALUES
(bookName, genre, bookPrice, bookStock);
END $$
DELIMITER ;


-- triggers for the database :p --


-- Triggers if theres a price change in the database --
CREATE TABLE price_audit( 
  audit_id INT AUTO_INCREMENT PRIMARY KEY,
  book_id INT,
  old_price DECIMAL(10,2),
  new_price DECIMAL(10,2),
  changed_by_user INT,
  changed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$
CREATE TRIGGER price_change
AFTER UPDATE ON books
FOR EACH ROW
BEGIN
    IF OLD.price != NEW.price THEN
        INSERT INTO price_audit (book_id, old_price, new_price, changed_by_user)
        VALUES (OLD.book_id, OLD.price, NEW.price, NULL);
    END IF;
END $$
DELIMITER ;


-- If the stock changes --
CREATE TABLE IF NOT EXISTS stock_log (
  log_id INT AUTO_INCREMENT PRIMARY KEY,
  book_id INT,
  old_stock INT,
  new_stock INT,
  changed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


DELIMITER $$
CREATE TRIGGER after_order_item_insert
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
  DECLARE current_stock INT;
  SELECT stock_quantity INTO current_stock FROM books WHERE book_id = NEW.book_id;
  INSERT INTO stock_log (book_id, old_stock, new_stock)
  VALUES (NEW.book_id, current_stock + NEW.quantity, current_stock);
END$$
DELIMITER ;

CREATE TABLE user_registration_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    name VARCHAR(100),
    role ENUM('Admin', 'Staff', 'Customer') DEFAULT 'Customer',
    registered_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$
CREATE TRIGGER log_user_registration
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO user_registration_log (user_id, name, role)
    VALUES (NEW.user_id, NEW.name, NEW.role);
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER prevent_negative_stock
BEFORE UPDATE ON books
FOR EACH ROW
BEGIN
    IF NEW.stock_quantity < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stock cannot be negative.';
    END IF;
END $$
DELIMITER ;

CREATE TABLE cart_deletion_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    book_id INT,
    quantity INT,
    deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$
CREATE TRIGGER log_deleted_cart_items
AFTER DELETE ON cart_items
FOR EACH ROW
BEGIN
    INSERT INTO cart_deletion_log (user_id, book_id, quantity)
    VALUES (OLD.user_id, OLD.book_id, OLD.quantity);
END $$
DELIMITER ;

CREATE TABLE role_change_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    old_role ENUM('Admin', 'Staff', 'Customer'),
    new_role ENUM('Admin', 'Staff', 'Customer'),
    changed_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$
CREATE TRIGGER track_user_role_change
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
    IF OLD.role != NEW.role THEN
        INSERT INTO role_change_log (user_id, old_role, new_role)
        VALUES (NEW.user_id, OLD.role, NEW.role);
    END IF;
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER auto_set_default_stock
BEFORE INSERT ON books
FOR EACH ROW
BEGIN
    IF NEW.stock_quantity <= 0 THEN
        SET NEW.stock_quantity = 1;
    END IF;
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER prevent_zero_order_total
BEFORE INSERT ON orders
FOR EACH ROW
BEGIN
    IF NEW.total_amount <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Order total must be greater than zero.';
    END IF;
END $$
DELIMITER ;

CREATE TABLE book_deletion_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    title VARCHAR(200),
    deleted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$
CREATE TRIGGER log_book_deletion
BEFORE DELETE ON books
FOR EACH ROW
BEGIN
    INSERT INTO book_deletion_log (book_id, title)
    VALUES (OLD.book_id, OLD.title);
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER update_order_status_after_payment
AFTER INSERT ON transaction_logs
FOR EACH ROW
BEGIN
    IF NEW.payment_status = 'Paid' THEN
        UPDATE orders
        SET status = 'Paid'
        WHERE order_id = NEW.order_id;
    END IF;
END $$
DELIMITER ;

-- ADMIN-SECURED STORED PROCEDURES AND LOGGING --

DELIMITER $$
CREATE PROCEDURE getAllOrders(IN admin_user_id INT)
BEGIN
    DECLARE adminRole VARCHAR(20);
    SELECT role INTO adminRole FROM users WHERE user_id = admin_user_id;
    IF adminRole = 'Admin' THEN
        SELECT o.*, c.currency_code,
               GROUP_CONCAT(DISTINCT CONCAT(b.title, ' (', oi.quantity, ')') ORDER BY b.title SEPARATOR ', ') AS book_list
        FROM orders o
        JOIN currencies c ON o.currency_id = c.currency_id
        LEFT JOIN order_items oi ON o.order_id = oi.order_id
        LEFT JOIN books b ON oi.book_id = b.book_id
        GROUP BY o.order_id, o.user_id, o.order_date, o.total_amount, o.currency_id, o.status, c.currency_code;
    ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Unauthorized: Only admins can view all orders.';
    END IF;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getAllTransactionLogs(IN admin_user_id INT)
BEGIN
    DECLARE adminRole VARCHAR(20);
    SELECT role INTO adminRole FROM users WHERE user_id = admin_user_id;
    IF adminRole = 'Admin' THEN
        SELECT * FROM transaction_logs;
    ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Unauthorized: Only admins can view transaction logs.';
    END IF;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE updateUserRole(IN admin_user_id INT, IN userId INT, IN newRole VARCHAR(20))
BEGIN
    DECLARE adminRole VARCHAR(20);
    SELECT role INTO adminRole FROM users WHERE user_id = admin_user_id;
    IF adminRole = 'Admin' THEN
        UPDATE users SET role = newRole WHERE user_id = userId;
    ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Unauthorized: Only admins can update user roles.';
    END IF;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE updateBookDetails(
    IN admin_user_id INT,
    IN bookId INT,
    IN title VARCHAR(200),
    IN genre VARCHAR(100),
    IN price DECIMAL(10,2),
    IN stock INT
)
BEGIN
    DECLARE adminRole VARCHAR(20);
    SELECT role INTO adminRole FROM users WHERE user_id = admin_user_id;
    IF adminRole = 'Admin' THEN
        UPDATE books SET title=title, genre=genre, price=price, stock_quantity=stock WHERE book_id=bookId;
    ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Unauthorized: Only admins can update book details.';
    END IF;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE updateExchangeRate(IN admin_user_id INT, IN currencyCode VARCHAR(10), IN newRate DECIMAL(10,4))
BEGIN
    DECLARE adminRole VARCHAR(20);
    SELECT role INTO adminRole FROM users WHERE user_id = admin_user_id;
    IF adminRole = 'Admin' THEN
        UPDATE currencies SET exchange_rate_to_php = newRate WHERE currency_code = currencyCode;
    ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Unauthorized: Only admins can update exchange rates.';
    END IF;
END $$
DELIMITER ;

-- Admin action logging table --
CREATE TABLE IF NOT EXISTS admin_action_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_user_id INT,
    action VARCHAR(100),
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_user_id) REFERENCES users(user_id)
);


-- Create admin MySQL user and give full DB privileges
CREATE USER 'admin'@'%' IDENTIFIED BY 'AdminBookMartPass!';
GRANT ALL PRIVILEGES ON bookmartdb.* TO 'admin'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- Create staff MySQL user and give limited privileges
CREATE USER 'staff'@'%' IDENTIFIED BY 'StaffBookMart1234!';
GRANT SELECT, INSERT, UPDATE ON bookmartdb.books TO 'staff'@'%';
GRANT SELECT, INSERT, UPDATE ON bookmartdb.cart_items TO 'staff'@'%';
FLUSH PRIVILEGES;
REVOKE SELECT, INSERT, UPDATE ON bookmartdb.cart_items FROM 'staff'@'%';
REVOKE SELECT, INSERT, UPDATE ON bookmartdb.transaction_logs FROM 'staff'@'%';
FLUSH PRIVILEGES;



