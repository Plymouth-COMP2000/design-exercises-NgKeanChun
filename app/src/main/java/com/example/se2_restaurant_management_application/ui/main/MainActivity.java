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
import androidx.navigation.NavGraph;
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
            NavigationView navigationView = findViewById(R.id.navigation_view);

            String userRole = getIntent().getStringExtra("usertype");
            boolean isStaff = "Staff".equalsIgnoreCase(userRole);
            int graphResId = isStaff ? R.navigation.staff_nav_graph : R.navigation.guest_nav_graph;
            navController.setGraph(graphResId);

            int menuResId = isStaff ? R.menu.staff_bottom_nav_menu : R.menu.guest_bottom_nav_menu;
            if (bottomNav != null) {
                bottomNav.getMenu().clear();
                bottomNav.inflateMenu(menuResId);
                NavigationUI.setupWithNavController(bottomNav, navController);
            }
            if (navigationView != null) {
                navigationView.getMenu().clear();
                navigationView.inflateMenu(menuResId);
                NavigationUI.setupWithNavController(navigationView, navController);
            }

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.navigation_menu);
            topLevelDestinations.add(R.id.navigation_reservations);
            topLevelDestinations.add(R.id.navigation_account);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int visibility = topLevelDestinations.contains(destination.getId()) ? View.VISIBLE : View.GONE;
                if (bottomNav != null) bottomNav.setVisibility(visibility);
                if (navigationView != null) navigationView.setVisibility(visibility);
            });
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
