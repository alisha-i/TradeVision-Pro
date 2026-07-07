package com.tradevision.database;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    private static final String URL = "jdbc:sqlite:tradevision.db";

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(URL);
            System.out.println("SQLite database connection established.");
            
            // Auto-initialize schema
            initializeSchema();
            
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
    }
    
    private void initializeSchema() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;"); // SQLite needs this to enforce foreign keys
            
            // Read schema.sql
            try (var in = getClass().getResourceAsStream("/database/schema.sql");
                 var reader = new BufferedReader(new InputStreamReader(in))) {
                
                String sql = reader.lines().collect(Collectors.joining("\n"));
                String[] commands = sql.split(";");
                for (String command : commands) {
                    if (!command.trim().isEmpty()) {
                        stmt.execute(command);
                    }
                }
                
                // Seed default assets
                stmt.execute("INSERT OR IGNORE INTO Assets (symbol, name, asset_type) VALUES ('XAUUSD', 'Gold / US Dollar', 'FOREX')");
                stmt.execute("INSERT OR IGNORE INTO Assets (symbol, name, asset_type) VALUES ('BTCUSD', 'Bitcoin / US Dollar', 'CRYPTO')");
                stmt.execute("INSERT OR IGNORE INTO Assets (symbol, name, asset_type) VALUES ('AAPL', 'Apple Inc.', 'STOCK')");
                stmt.execute("INSERT OR IGNORE INTO Assets (symbol, name, asset_type) VALUES ('EURUSD', 'Euro / US Dollar', 'FOREX')");
                stmt.execute("INSERT OR IGNORE INTO Assets (symbol, name, asset_type) VALUES ('TSLA', 'Tesla Inc.', 'STOCK')");

                System.out.println("Database schema and default assets initialized successfully.");
            } catch (Exception ex) {
                System.err.println("Failed to read schema.sql: " + ex.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Failed to initialize schema!");
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        } else {
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DatabaseManager();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public double getUserBalance(int userId) {
        String query = "SELECT balance FROM Portfolio WHERE user_id = ?";
        try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean updateUserBalance(int userId, double newBalance) {
        String query = "UPDATE Portfolio SET balance = ? WHERE user_id = ?";
        try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean executeTrade(int userId, String symbol, String type, double quantity, double price) {
        String getAssetId = "SELECT id FROM Assets WHERE symbol = ?";
        try (java.sql.PreparedStatement stmt = connection.prepareStatement(getAssetId)) {
            stmt.setString(1, symbol);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.err.println("Asset not found!");
                return false;
            }
            int assetId = rs.getInt("id");

            // Calculate total cost
            double totalCost = quantity * price;

            // Check balance for BUY
            if (type.equalsIgnoreCase("BUY")) {
                double currentBalance = getUserBalance(userId);
                if (currentBalance < totalCost) {
                    System.err.println("Insufficient funds!");
                    return false;
                }
            }

            // Execute in transaction
            connection.setAutoCommit(false);
            try {
                // Update Portfolio (simulate holding by just deducting for now, in a real app you'd add to positions)
                String updatePortfolio = type.equalsIgnoreCase("BUY") 
                    ? "UPDATE Portfolio SET balance = balance - ? WHERE user_id = ?"
                    : "UPDATE Portfolio SET balance = balance + ? WHERE user_id = ?";
                try (java.sql.PreparedStatement portStmt = connection.prepareStatement(updatePortfolio)) {
                    portStmt.setDouble(1, totalCost);
                    portStmt.setInt(2, userId);
                    portStmt.executeUpdate();
                }

                // Insert Order
                String insertOrder = "INSERT INTO Orders (user_id, asset_id, order_type, quantity, price, status) VALUES (?, ?, ?, ?, ?, 'CLOSED')";
                try (java.sql.PreparedStatement orderStmt = connection.prepareStatement(insertOrder)) {
                    orderStmt.setInt(1, userId);
                    orderStmt.setInt(2, assetId);
                    orderStmt.setString(3, type.toUpperCase());
                    orderStmt.setDouble(4, quantity);
                    orderStmt.setDouble(5, price);
                    orderStmt.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<com.tradevision.models.OrderModel> getTradeHistory(int userId) {
        java.util.List<com.tradevision.models.OrderModel> history = new java.util.ArrayList<>();
        String query = "SELECT o.id, a.symbol, o.order_type, o.quantity, o.price, o.created_at " +
                       "FROM Orders o JOIN Assets a ON o.asset_id = a.id " +
                       "WHERE o.user_id = ? ORDER BY o.created_at DESC";
        try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            java.sql.ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                history.add(new com.tradevision.models.OrderModel(
                    rs.getInt("id"),
                    rs.getString("created_at"),
                    rs.getString("symbol"),
                    rs.getString("order_type"),
                    rs.getDouble("quantity"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        DatabaseManager.getInstance();
    }
}
