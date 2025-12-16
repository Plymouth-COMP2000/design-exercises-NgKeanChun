package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup; // <-- FIX: IMPORT ADDED
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.ui.auth.LoginActivity;
import com.example.se2_restaurant_management_application.ui.auth.LoginViewModel;
import com.example.se2_restaurant_management_application.util.SessionManager;
import com.example.se2_restaurant_management_application.util.SettingsManager;
import com.google.android.material.materialswitch.MaterialSwitch;

public class AccountFragment extends Fragment {

    private AccountViewModel accountViewModel;
    private LoginViewModel loginViewModel;
    private SessionManager sessionManager;
    private SettingsManager settingsManager;
    private User currentUser;

    // UI Components
    private TextView userNameTextView, userPhoneTextView, userFullNameTextView, userEmailTextView;
    private ImageButton logoutButton;
    private ImageView editProfileIcon, profileImageView;
    private MaterialSwitch notificationsSwitch, reservationsConfirmedSwitch, bookingChangesSwitch;
    private Button deleteAccountButton; // New delete button

    private ActivityResultLauncher<String> galleryLauncher;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    galleryLauncher.launch("image/*");
                } else {
                    Toast.makeText(getContext(), "Permission denied. Cannot access photos.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the gallery launcher here
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        if (currentUser != null) {
                            int userId = currentUser.getId();
                            String imageUriString = uri.toString();
                            accountViewModel.updateUserImage(userId, imageUriString);

                            Toast.makeText(getContext(), "Profile picture saved!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guest_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViewModelsAndManagers();
        initializeViews(view);
        setupListeners(view);
        observeUser();
        observeDeleteEvents(); // Observe delete account events
        loadSettings();
        setupSwitchListeners();

        accountViewModel.getUpdateUserSuccessLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                accountViewModel.clearUpdateUserSuccess();
            }
        });
    }

    private void initializeViewModelsAndManagers() {
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        sessionManager = new SessionManager(requireContext());
        settingsManager = new SettingsManager(requireContext());
    }

    private void initializeViews(View view) {
        userNameTextView = view.findViewById(R.id.userNameTextView);
        userPhoneTextView = view.findViewById(R.id.userPhoneTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        editProfileIcon = view.findViewById(R.id.editProfileIcon);
        profileImageView = view.findViewById(R.id.profileImageView);
        userFullNameTextView = view.findViewById(R.id.userFullNameTextView);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        notificationsSwitch = view.findViewById(R.id.notificationsSwitch);
        reservationsConfirmedSwitch = view.findViewById(R.id.reservationsConfirmedSwitch);
        bookingChangesSwitch = view.findViewById(R.id.bookingChangesSwitch);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
    }

    private void setupListeners(View view) {
        editProfileIcon.setOnClickListener(v -> handleImagePick());
        logoutButton.setOnClickListener(v -> logoutUser());
        deleteAccountButton.setOnClickListener(v -> showDeleteConfirmationDialog()); // Set listener for delete button

        ImageButton notificationButton = view.findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_global_to_notifications)
        );
    }

    private void handleImagePick() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*");
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void observeUser() {
        accountViewModel.getLoggedInUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.currentUser = user;
                updateUI(user);
            } else {
                logoutUser();
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
            profileImageView.setImageResource(R.drawable.ic_person);
            profileImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.default_profile_tint)));
        }
    }

    private void observeDeleteEvents() {
        accountViewModel.getDeleteUserSuccessLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_LONG).show();
                logoutUser();
                accountViewModel.consumeDeleteUserEvents();
            }
        });

        accountViewModel.getDeleteUserErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                accountViewModel.consumeDeleteUserEvents();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Yes, Delete", (dialog, which) -> showPasswordVerificationDialog())
                .setNegativeButton("No", null)
                .setIcon(R.drawable.ic_cancel)
                .show();
    }

    private void showPasswordVerificationDialog() {
        if (getContext() == null || currentUser == null) return;

        final EditText passwordInput = new EditText(getContext());
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Enter your password");

        // Use the imported classes
        FrameLayout container = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        passwordInput.setLayoutParams(params);
        container.addView(passwordInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Verify Password")
                .setMessage("Please enter your password to confirm deletion.")
                .setView(container)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String enteredPassword = passwordInput.getText().toString();
                    if (currentUser.getPassword().equals(enteredPassword)) {
                        Toast.makeText(getContext(), "Password verified. Deleting account...", Toast.LENGTH_SHORT).show();
                        accountViewModel.deleteUser(currentUser.getUsername());
                    } else {
                        Toast.makeText(getContext(), "Incorrect password. Account deletion cancelled.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logoutUser() {
        sessionManager.logoutUser();
        if (accountViewModel != null) accountViewModel.logout();
        if (loginViewModel != null) loginViewModel.onLogout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }

    private void loadSettings() {
        notificationsSwitch.setChecked(settingsManager.getBoolean("guest_notifications_allowed", true));
        reservationsConfirmedSwitch.setChecked(settingsManager.getBoolean("guest_reservations_confirmed", true));
        bookingChangesSwitch.setChecked(settingsManager.getBoolean("guest_booking_changes", false));
    }

    private void setupSwitchListeners() {
        notificationsSwitch.setOnCheckedChangeListener((bv, isChecked) -> settingsManager.saveBoolean("guest_notifications_allowed", isChecked));
        reservationsConfirmedSwitch.setOnCheckedChangeListener((bv, isChecked) -> settingsManager.saveBoolean("guest_reservations_confirmed", isChecked));
        bookingChangesSwitch.setOnCheckedChangeListener((bv, isChecked) -> settingsManager.saveBoolean("guest_booking_changes", isChecked));
    }
}
