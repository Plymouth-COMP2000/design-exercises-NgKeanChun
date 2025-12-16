package com.example.se2_restaurant_management_application.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "AppSession";
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Clears all session data. This will be called on logout
     * or whenever we need to ensure the user is logged out.
     */
    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    /**
     * This method is no longer needed for automatic login,
     * but we can keep a simple version of it to track if a user
     * is active within a single app session (before termination).
     */
    public void createLoginSession() {
        editor.putBoolean("isLoggedIn", true);
        editor.commit();
    }

    /**
     * Checks if a user is logged in *during the current session*.
     * This will be false on every fresh app start.
     */
    public boolean isLoggedIn() {
        return pref.getBoolean("isLoggedIn", false);
    }
}
