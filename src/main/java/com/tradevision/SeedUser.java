package com.tradevision;

import com.tradevision.database.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SeedUser {
    public static void main(String[] args) {
        String username = "alisha";
        String email = "alisha@tradevision.com";
        String password = "alisha123";
        
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            
            // Delete existing user if any
            PreparedStatement del = conn.prepareStatement("DELETE FROM Users WHERE username = ?");
            del.setString(1, username);
            del.executeUpdate();
            
            // Hash password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            // Insert user
            String query = "INSERT INTO Users (username, email, password_hash) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // Get the generated user ID using SQLite last_insert_rowid()
                PreparedStatement idStmt = conn.prepareStatement("SELECT last_insert_rowid()");
                java.sql.ResultSet rs = idStmt.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    // Create portfolio for the user
                    PreparedStatement portStmt = conn.prepareStatement("INSERT INTO Portfolio (user_id, balance) VALUES (?, 100000.00)");
                    portStmt.setInt(1, userId);
                    portStmt.executeUpdate();
                }
                
                System.out.println("SUCCESS! User created:");
                System.out.println("Username: " + username);
                System.out.println("Password: " + password);
            }
            
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
