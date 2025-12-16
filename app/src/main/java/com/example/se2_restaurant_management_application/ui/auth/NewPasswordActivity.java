package com.example.se2_restaurant_management_application.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.User;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;

public class NewPasswordActivity extends AppCompatActivity {

    private NewPasswordViewModel newPasswordViewModel;

    private TextInputEditText newPasswordEditText;
    private TextInputEditText confirmPasswordEditText;
    private TextView reqLength, reqNumber, reqSpecialChar;
    private Button saveButton;

    // FIX 1: We now hold the entire user object
    private User userToUpdate = null;

    private boolean isLengthMet = false, isNumberMet = false, isSpecialCharMet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        // FIX 2: Retrieve the full User object from the intent
        if (getIntent().hasExtra("USER_TO_UPDATE")) {
            userToUpdate = (User) getIntent().getSerializableExtra("USER_TO_UPDATE");
        }

        if (userToUpdate == null) {
            Toast.makeText(this, "Error: User data not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        newPasswordViewModel = new ViewModelProvider(this).get(NewPasswordViewModel.class);
        setupPasswordWatcher();
        setupSaveButtonListener();
        observeViewModel();
    }

    private void initializeViews() {
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        saveButton = findViewById(R.id.saveButton);
        reqLength = findViewById(R.id.requirementLength);
        reqNumber = findViewById(R.id.requirementNumber);
        reqSpecialChar = findViewById(R.id.requirementSpecialChar);
    }

    private void setupSaveButtonListener() {
        saveButton.setOnClickListener(v -> handlePasswordUpdate());
    }

    private void handlePasswordUpdate() {
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill both password fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isLengthMet || !isNumberMet || !isSpecialCharMet) {
            Toast.makeText(this, "Password does not meet requirements.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        // FIX 3: Update the password on the user object we already have
        userToUpdate.setPassword(newPassword);

        Log.d("UpdateUser", "User object being sent: " +
                "id=" + userToUpdate.getId() +
                ", username=" + userToUpdate.getUsername() +
                ", password=" + userToUpdate.getPassword() +
                ", firstname=" + userToUpdate.getFirstName() +
                ", lastname=" + userToUpdate.getLastName() +
                ", email=" + userToUpdate.getEmail() +
                ", contact=" + userToUpdate.getContact() +
                ", usertype=" + userToUpdate.getUserType()
        );

        // FIX 4: Send the complete, modified user object to the ViewModel for updating
        newPasswordViewModel.updateUserPassword(userToUpdate);

        Toast.makeText(this, "Updating password...", Toast.LENGTH_SHORT).show();
    }

    private void observeViewModel() {
        newPasswordViewModel.getUpdateSuccessLiveData().observe(this, successMessage -> {
            if (successMessage != null) {
                Toast.makeText(this, "Password updated successfully! Please log in.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(NewPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        newPasswordViewModel.getUpdateErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupPasswordWatcher() {
        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void validatePassword(String password) {
        isLengthMet = password.length() >= 8;
        updateRequirementView(reqLength, isLengthMet);

        isNumberMet = password.matches(".*\\d+.*");
        updateRequirementView(reqNumber, isNumberMet);

        isSpecialCharMet = password.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*");
        updateRequirementView(reqSpecialChar, isSpecialCharMet);
    }

    private void updateRequirementView(TextView textView, boolean isMet) {
        textView.setTextColor(ContextCompat.getColor(this, isMet ? R.color.status_confirmed_bg : R.color.status_cancelled_bg));
    }
}
