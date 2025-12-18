package com.example.se2_restaurant_management_application.ui.main.accounts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.ui.auth.LoginActivity;
import com.example.se2_restaurant_management_application.ui.auth.LoginViewModel;
import com.example.se2_restaurant_management_application.ui.main.viewmodels.AccountViewModel;
import com.example.se2_restaurant_management_application.util.SessionManager;
import com.example.se2_restaurant_management_application.util.SettingsManager;
import com.google.android.material.materialswitch.MaterialSwitch;

public class StaffAccountFragment extends Fragment {

    private User currentUser;
    private AccountViewModel accountViewModel;
    private LoginViewModel loginViewModel;
    private SessionManager sessionManager;
    private SettingsManager settingsManager;

    // UI Components
    private ImageView profileImageView;
    private ImageView editProfileIcon;
    private TextView userNameTextView;
    private TextView userPhoneTextView;
    private ImageButton logoutButton;
    private TextView userFullNameTextView;
    private TextView userEmailTextView;

    // Staff-specific switches
    private MaterialSwitch notificationsSwitch;
    private MaterialSwitch newReservationsSwitch;
    private MaterialSwitch reservationChangesSwitch;
    private MaterialSwitch cancelledReservationsSwitch;

    // --- Image Launchers ---
    private ActivityResultLauncher<String> galleryLauncher;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // If permission is granted, launch the gallery
                    galleryLauncher.launch("image/*");
                } else {
                    Toast.makeText(getContext(), "Permission denied. Cannot access photos.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the gallery launcher here. This MUST be done in onCreate.
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        if (currentUser != null) {
                            String userId = currentUser.getId();
                            String imageUriString = uri.toString();
                            accountViewModel.updateUserImage(userId, imageUriString);

                            Toast.makeText(getContext(), "Profile picture saved!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

        @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        sessionManager = new SessionManager(requireContext());
        settingsManager = new SettingsManager(requireContext());

        initializeViews(view);
        setupButtonListeners();
        observeUser();
        loadSettings();
        setupSwitchListeners();
    }

    private void initializeViews(View view) {
        userNameTextView = view.findViewById(R.id.userNameTextView);
        userPhoneTextView = view.findViewById(R.id.userPhoneTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        profileImageView = view.findViewById(R.id.profileImageView);
        editProfileIcon = view.findViewById(R.id.editProfileIcon);
        userFullNameTextView = view.findViewById(R.id.userFullNameTextView);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        notificationsSwitch = view.findViewById(R.id.notificationsSwitch);
        newReservationsSwitch = view.findViewById(R.id.newReservationsSwitch);
        reservationChangesSwitch = view.findViewById(R.id.reservationChangesSwitch);
        cancelledReservationsSwitch = view.findViewById(R.id.cancelledReservationsSwitch);
    }

    private void setupButtonListeners() {
        logoutButton.setOnClickListener(v -> performLogout());
        editProfileIcon.setOnClickListener(v -> handleImagePick());

        ImageButton notificationButton = requireView().findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_global_to_notifications)
        );
    }

    private void performLogout() {
        sessionManager.logoutUser();
        accountViewModel.logout();
        loginViewModel.onLogout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void observeUser() {
        accountViewModel.getLoggedInUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.currentUser = user;
                updateUI(user);
            } else {
                performLogout();
            }
        });
    }

    private void updateUI(User user) {
        userNameTextView.setText(user.getUsername());
        userPhoneTextView.setText(user.getContact());
        userFullNameTextView.setText(user.getFullName());
        userEmailTextView.setText(user.getEmail());

        String imageUriString = user.getImageUri();
        if (imageUriString != null && !imageUriString.isEmpty()) {
            profileImageView.setImageURI(Uri.parse(imageUriString));
            profileImageView.setImageTintList(null);
        } else {
            // Fallback to the default placeholder icon
            profileImageView.setImageResource(R.drawable.ic_person);
            // Ensure the placeholder has a tint
            profileImageView.setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.default_profile_tint));
        }
    }

    private void handleImagePick() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*");
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void loadSettings() {
        notificationsSwitch.setChecked(settingsManager.getBoolean("staff_notifications_allowed", true));
        newReservationsSwitch.setChecked(settingsManager.getBoolean("staff_new_reservations", true));
        reservationChangesSwitch.setChecked(settingsManager.getBoolean("staff_reservation_changes", true));
        cancelledReservationsSwitch.setChecked(settingsManager.getBoolean("staff_cancelled_reservations", false));
    }

    private void setupSwitchListeners() {
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> settingsManager.saveBoolean("staff_notifications_allowed", isChecked));
        newReservationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> settingsManager.saveBoolean("staff_new_reservations", isChecked));
        reservationChangesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> settingsManager.saveBoolean("staff_reservation_changes", isChecked));
        cancelledReservationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> settingsManager.saveBoolean("staff_cancelled_reservations", isChecked));
    }
}
