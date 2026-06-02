CREATE DATABASE IF NOT EXISTS auction_system;
USE auction_system;

DROP TABLE IF EXISTS Auto_Bidding;
DROP TABLE IF EXISTS Bid_History;
DROP TABLE IF EXISTS Bid_Transactions;
DROP TABLE IF EXISTS Auctions;
DROP TABLE IF EXISTS Items;
DROP TABLE IF EXISTS Users;

CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'SELLER', 'BIDDER') NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00
);

CREATE TABLE Items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    starting_price DECIMAL(15,2) NOT NULL,
    category VARCHAR(50),
    item_type VARCHAR(20) NOT NULL DEFAULT 'ELECTRONICS',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    seller_id INT NOT NULL,
    warranty_months INT,
    author VARCHAR(100),
    engine_type VARCHAR(50),
    FOREIGN KEY (seller_id) REFERENCES Users(id)
);

CREATE TABLE Auctions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    current_price DECIMAL(15,2) NOT NULL,
    starting_price DECIMAL(15,2) NOT NULL,
    step_price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') NOT NULL,
    seller_id INT NOT NULL,
    winner_id INT,
    FOREIGN KEY (item_id) REFERENCES Items(id),
    FOREIGN KEY (seller_id) REFERENCES Users(id),
    FOREIGN KEY (winner_id) REFERENCES Users(id)
);

CREATE TABLE Bid_Transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,
    bidder_id INT NOT NULL,
    bid_price DECIMAL(15,2) NOT NULL,
    bid_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES Auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES Users(id)
);

CREATE TABLE Bid_History (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,
    bidder_id INT NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    bid_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES Auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES Users(id)
);

CREATE TABLE Auto_Bidding (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,
    bidder_id INT NOT NULL,
    maxBid DECIMAL(15,2) NOT NULL,
    `increment` DECIMAL(15,2) NOT NULL,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE', 'EXHAUSTED') DEFAULT 'ACTIVE',
    UNIQUE KEY uk_auction_bidder (auction_id, bidder_id),
    CONSTRAINT FK_AutoBid_Auction FOREIGN KEY (auction_id) REFERENCES Auctions(id) ON DELETE CASCADE,
    CONSTRAINT FK_AutoBid_User FOREIGN KEY (bidder_id) REFERENCES Users(id) ON DELETE CASCADE,
    INDEX idx_active_bids (auction_id, status, createdAt)
);