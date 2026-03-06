-- Create database
CREATE DATABASE IF NOT EXISTS oceanviewresortdb;
USE oceanviewresortdb;

-- Staff table for authentication and profile
CREATE TABLE IF NOT EXISTS staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'RECEPTIONIST') NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users (Guests) table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_type VARCHAR(50) NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    description TEXT,
    image_url VARCHAR(255)
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    guest_id INT,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100),
    room_id INT,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'CHECKED_IN', 'CHECKED_OUT') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- Insert sample staff data
INSERT INTO staff (first_name, last_name, email, phone, role, username, password) VALUES 
('System', 'Admin', 'admin@oceanview.com', '123-456-7890', 'ADMIN', 'admin', 'admin123'),
('System', 'Receptionist', 'Receptionist@gmail.com', '0779096345', 'RECEPTIONIST', 'receptionist', 'Receptionist 
123'),


-- Insert sample guest data
INSERT INTO users (first_name, last_name, email, phone, address) VALUES 
('John', 'Doe', 'john@example.com', '555-0101', '123 Beach Ave'),
('Jane', 'Smith', 'jane@example.com', '555-0102', '456 Palm St');

INSERT INTO rooms (room_number, room_type, price_per_night, status) VALUES 
('101', 'Deluxe Single', 150.00, 'AVAILABLE'),
('102', 'Deluxe Double', 250.00, 'AVAILABLE'),
('201', 'Ocean View Suite', 500.00, 'AVAILABLE');

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'ONLINE') NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- MIGRATION: Run this if your 'bookings' table already exists without 'guest_id'
-- ALTER TABLE bookings ADD COLUMN guest_id INT AFTER id;
-- ALTER TABLE bookings ADD FOREIGN KEY (guest_id) REFERENCES users(id) ON DELETE SET NULL;
