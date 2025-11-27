package com.example.se2_restaurant_management_application;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Find navigation views by their ID
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationView navigationView = findViewById(R.id.navigation_view);

            // Setup navigation based on which view is present
            if (bottomNav != null) { // Portrait layout
                NavigationUI.setupWithNavController(bottomNav, navController);
            } else if (navigationView != null) { // Landscape layout
                NavigationUI.setupWithNavController(navigationView, navController);
            }
        }

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Apply padding to the root view to handle system bars insets
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                // Return the insets to allow the system to continue processing them.
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }
}
