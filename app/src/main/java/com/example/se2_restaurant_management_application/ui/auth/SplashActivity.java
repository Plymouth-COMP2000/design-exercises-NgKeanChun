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

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.logoutUser(); // This clears the SharedPreferences.

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // After clearing the session, the destination is ALWAYS the LoginActivity.
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Finish SplashActivity so it can't be returned to.
        }, 2000); // 2-second delay
    }
}
