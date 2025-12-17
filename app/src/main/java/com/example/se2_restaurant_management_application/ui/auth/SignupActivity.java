package com.example.se2_restaurant_management_application.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.ui.main.fragments.AccountViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private SignupViewModel signupViewModel;

    private AccountViewModel accountViewModel;

    // Input Fields
    private TextInputEditText usernameEditText;
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText contactEditText;
    private TextInputEditText passwordEditText;

    // Requirement TextViews
    private TextView reqLength, reqNumber, reqSpecialChar;

    private Button signupButton;
    private Button backButton;

    // Requirement tracking booleans
    private boolean isLengthMet = false;
    private boolean isNumberMet = false;
    private boolean isSpecialCharMet = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupViewModel = new ViewModelProvider(this).get(SignupViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        initializeViews();
        signupButton.setOnClickListener(v -> handleSignup());
        backButton.setOnClickListener(v -> finish());
        setupPasswordWatcher();
        observeViewModel();
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        contactEditText = findViewById(R.id.contactEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupButton = findViewById(R.id.SignupButton);
        backButton = findViewById(R.id.BackButton);

        reqLength = findViewById(R.id.passwordRequirementLength);
        reqNumber = findViewById(R.id.passwordRequirementNumber);
        reqSpecialChar = findViewById(R.id.passwordRequirementSpecialChar);
    }

    private void handleSignup() {
        String username = usernameEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // --- Initial validation for empty fields and password requirements ---
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(contact) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isLengthMet || !isNumberMet || !isSpecialCharMet) {
            Toast.makeText(this, "Please ensure the password meets all requirements.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Asynchronous Uniqueness Check ---
        Toast.makeText(this, "Checking user details...", Toast.LENGTH_SHORT).show();
        accountViewModel.getAllUsers().observe(this, new androidx.lifecycle.Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                accountViewModel.getAllUsers().removeObserver(this);

                if (users == null) {
                    Toast.makeText(SignupActivity.this, "Could not verify user data. Please check your connection.", Toast.LENGTH_LONG).show();
                    return;
                }

                boolean isUnique = true;
                for (User existingUser : users) {
                    if (existingUser.getUsername().equalsIgnoreCase(username)) {
                        Toast.makeText(SignupActivity.this, "This username is already taken.", Toast.LENGTH_SHORT).show();
                        isUnique = false;
                        break;
                    }
                    if (existingUser.getEmail().equalsIgnoreCase(email)) {
                        Toast.makeText(SignupActivity.this, "This email address is already in use.", Toast.LENGTH_SHORT).show();
                        isUnique = false;
                        break;
                    }
                    if (existingUser.getContact().equals(contact)) {
                        Toast.makeText(SignupActivity.this, "This contact number is already registered.", Toast.LENGTH_SHORT).show();
                        isUnique = false;
                        break;
                    }
                }

                // If the loop completed and the data is still unique, proceed with signup.
                if (isUnique) {
                    String userType = "guest";
                    User newUser = new User(username, password, firstName, lastName, email, contact, userType);
                    signupViewModel.signup(newUser);
                }
            }
        });

        // Finally, trigger the refresh in the repository. The observer above will catch the result.
        accountViewModel.refreshAllUsers();
    }


    private void setupPasswordWatcher() {
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
        if (isMet) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.status_confirmed_bg));
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.status_cancelled_bg));
        }
    }

    private void observeViewModel() {
        signupViewModel.getSignupSuccessMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, "Signup Successful! Please log in.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        signupViewModel.getSignupErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Signup Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
