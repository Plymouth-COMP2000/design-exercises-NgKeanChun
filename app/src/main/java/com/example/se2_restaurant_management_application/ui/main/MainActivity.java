package com.example.se2_restaurant_management_application.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.se2_restaurant_management_application.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications permission denied.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        askNotificationPermission();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationView navigationView = findViewById(R.id.navigation_view); // Side navigation for landscape
            String userRole = getIntent().getStringExtra("usertype");

            if ("Staff".equalsIgnoreCase(userRole)) {
                navController.setGraph(R.navigation.staff_nav_graph);
                if (bottomNav != null) { // Portrait
                    bottomNav.getMenu().clear();
                    bottomNav.inflateMenu(R.menu.staff_bottom_nav_menu);
                }
                if (navigationView != null) { // Landscape
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.staff_bottom_nav_menu);
                }
            } else { // Guest
                navController.setGraph(R.navigation.guest_nav_graph);
                if (bottomNav != null) { // Portrait
                    bottomNav.getMenu().clear();
                    bottomNav.inflateMenu(R.menu.guest_bottom_nav_menu);
                }
                if (navigationView != null) { // Landscape
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.guest_bottom_nav_menu);
                }
            }

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.navigation_menu);
            topLevelDestinations.add(R.id.navigation_reservations);
            topLevelDestinations.add(R.id.navigation_account);

            if (bottomNav != null) { // Handle visibility for Portrait layout
                NavigationUI.setupWithNavController(bottomNav, navController);

                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    if (topLevelDestinations.contains(destination.getId())) {
                        bottomNav.setVisibility(View.VISIBLE);
                    } else {
                        bottomNav.setVisibility(View.GONE);
                    }
                });

            } else if (navigationView != null) { // Handle visibility for Landscape layout
                NavigationUI.setupWithNavController(navigationView, navController);

                // listener to show/hide the side navigation view.
                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    if (topLevelDestinations.contains(destination.getId())) {
                        navigationView.setVisibility(View.VISIBLE);
                    } else {
                        navigationView.setVisibility(View.GONE);
                    }
                });
            }
        }

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    private void askNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
