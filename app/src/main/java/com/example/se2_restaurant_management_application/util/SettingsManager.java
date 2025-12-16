package com.example.se2_restaurant_management_application.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREF_NAME = "AppSettings";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    // --- Guest Preference Keys ---
    private static final String KEY_GUEST_NOTIFICATIONS_ALLOWED = "guest_notifications_allowed";
    private static final String KEY_GUEST_RESERVATIONS_CONFIRMED = "guest_reservations_confirmed";
    private static final String KEY_GUEST_BOOKING_CHANGES = "guest_booking_changes";

    // --- Staff Preference Keys ---
    private static final String KEY_STAFF_NOTIFICATIONS_ALLOWED = "staff_notifications_allowed";
    private static final String KEY_STAFF_NEW_RESERVATIONS = "staff_new_reservations";
    private static final String KEY_STAFF_RESERVATION_CHANGES = "staff_reservation_changes";
    private static final String KEY_STAFF_CANCELLED_RESERVATIONS = "staff_cancelled_reservations";


    public SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // --- Generic Save/Load Methods ---
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}
