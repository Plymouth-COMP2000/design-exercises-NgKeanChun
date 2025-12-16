package com.example.se2_restaurant_management_application.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.ui.main.fragments.AccountViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class ForgetPasswordActivity extends AppCompatActivity {

    private ForgetPasswordViewModel forgetPasswordViewModel;
    private AccountViewModel accountViewModel;

    // UI Components
    private TextInputEditText phoneNumberEditText;
    private TextInputEditText emailEditText;
    private TextInputLayout phoneNumberInputLayout;
    private TextInputLayout emailInputLayout;
    private RadioGroup methodRadioGroup;
    private RadioButton radioPhone;
    private Button continueButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Change this back to your original layout file
        setContentView(R.layout.activity_forgot_password);

        // Initialize both ViewModels
        forgetPasswordViewModel = new ViewModelProvider(this).get(ForgetPasswordViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        initializeViews();
        setupListeners();
        observeViewModel();
    }

    private void initializeViews() {
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneNumberInputLayout = findViewById(R.id.phoneNumberInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        methodRadioGroup = findViewById(R.id.methodRadioGroup);
        radioPhone = findViewById(R.id.radioPhone);
        continueButton = findViewById(R.id.ContinueButton);
        backButton = findViewById(R.id.BackButton);
    }

    private void setupListeners() {
        continueButton.setOnClickListener(v -> handleSearch());
        backButton.setOnClickListener(v -> finish());

        // Listener to toggle visibility between phone and email fields
        methodRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioPhone) {
                phoneNumberInputLayout.setVisibility(View.VISIBLE);
                emailInputLayout.setVisibility(View.GONE);
            } else {
                phoneNumberInputLayout.setVisibility(View.GONE);
                emailInputLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleSearch() {
        boolean isPhoneMethod = radioPhone.isChecked();
        String identifier;

        if (isPhoneMethod) {
            identifier = String.valueOf(phoneNumberEditText.getText()).trim();
            if (TextUtils.isEmpty(identifier)) {
                phoneNumberEditText.setError("Phone number cannot be empty");
                return;
            }
        } else {
            identifier = String.valueOf(emailEditText.getText()).trim();
            if (TextUtils.isEmpty(identifier)) {
                emailEditText.setError("Email cannot be empty");
                return;
            }
        }

        Toast.makeText(this, "Searching for account...", Toast.LENGTH_SHORT).show();

        // **THE FIX IS HERE:**
        // We use a one-time observer to wait for the user list to be loaded.
        accountViewModel.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                // Immediately remove the observer so this code doesn't run again.
                accountViewModel.getAllUsers().removeObserver(this);

                if (users == null || users.isEmpty()) {
                    Toast.makeText(ForgetPasswordActivity.this, "Could not retrieve user data. Check connection.", Toast.LENGTH_LONG).show();
                    return;
                }

                // NOW that we are sure the user list is available, we can safely perform the search.
                forgetPasswordViewModel.findUserByIdentifier(identifier, isPhoneMethod);
            }
        });

        // This triggers the fetch from the server. The observer above will catch the result.
        accountViewModel.refreshAllUsers();
    }

    private void observeViewModel() {
        // This observer will now only fire *after* the handleSearch logic is complete.
        forgetPasswordViewModel.getFoundUserLiveData().observe(this, user -> {
            if (user != null) {
                // User was found! Navigate to NewPasswordActivity as requested.
                Toast.makeText(this, "User Found: " + user.getFullName(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ForgetPasswordActivity.this, NewPasswordActivity.class);

                // We pass the full user object so the next screen knows who to update.
                intent.putExtra("USER_TO_UPDATE", user);

                startActivity(intent);

                // --- THE FIX IS HERE ---
                // Immediately consume the event so it doesn't trigger again.
                forgetPasswordViewModel.consumeFoundUserEvent();

                finish(); // Close this screen
            }
        });
    }
}
