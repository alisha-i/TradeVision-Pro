package com.tradevision.utils;

public class SessionManager {
    private static int currentUserId = -1;
    private static String currentUsername = null;

    public static void login(int userId, String username) {
        currentUserId = userId;
        currentUsername = username;
    }

    public static void logout() {
        currentUserId = -1;
        currentUsername = null;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }
    
    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }
}
