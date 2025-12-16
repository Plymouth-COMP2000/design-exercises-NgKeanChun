package com.example.se2_restaurant_management_application.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // --- THE FIX IS HERE ---
        // We create a SessionManager instance and immediately log out any lingering session
        // from a previous run that was terminated without a proper logout.
        // This guarantees a clean state every single time the app is launched.
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logoutUser(); // This clears the SharedPreferences.

        // Now we can proceed with the normal splash screen delay.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // After clearing the session, the destination is ALWAYS the LoginActivity.
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Finish SplashActivity so it can't be returned to.
        }, 2000); // 2-second delay
    }
}
