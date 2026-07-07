-- SQLite Database Schema

CREATE TABLE IF NOT EXISTS Users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Assets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(50) NOT NULL -- e.g., Crypto, Stock, Forex
);

CREATE TABLE IF NOT EXISTS Watchlists (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INT NOT NULL,
    asset_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES Assets(id) ON DELETE CASCADE,
    UNIQUE(user_id, asset_id)
);

CREATE TABLE IF NOT EXISTS Portfolio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INT NOT NULL UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 100000.00,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INT NOT NULL,
    asset_id INT NOT NULL,
    order_type VARCHAR(10) NOT NULL, -- BUY, SELL
    quantity DECIMAL(15, 6) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, CLOSED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES Assets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Trades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INT NOT NULL,
    execution_price DECIMAL(15, 2) NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE
);

-- Insert dummy assets for testing
INSERT OR IGNORE INTO Assets (symbol, name, asset_type) VALUES 
('BTC/USD', 'Bitcoin', 'Crypto'),
('ETH/USD', 'Ethereum', 'Crypto'),
('AAPL', 'Apple Inc.', 'Stock'),
('TSLA', 'Tesla Inc.', 'Stock'),
('EUR/USD', 'Euro / US Dollar', 'Forex');
