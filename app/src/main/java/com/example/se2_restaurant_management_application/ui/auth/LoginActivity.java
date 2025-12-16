package com.example.se2_restaurant_management_application.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.se2_restaurant_management_application.ui.main.MainActivity;
import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.data.models.ApiResponse;
import com.example.se2_restaurant_management_application.data.network.ApiService;
import com.example.se2_restaurant_management_application.data.network.RetrofitClient;
import com.example.se2_restaurant_management_application.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    // FIX 1: Change the variable name for clarity
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView signUpTextView;
    private SessionManager sessionManager;
    private TextView forgotPasswordTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());
        // The auto-login check is now removed.

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Initialize UI components
        // FIX 2: Find the EditText by its ID. We'll assume the ID is now usernameEditText
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView); // Make sure this ID exists in your XML
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // Link to SignupActivity ---
        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Link to ForgetPasswordActivity
        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            // FIX 3: Get text from usernameEditText and rename the variable
            String username = String.valueOf(usernameEditText.getText()).trim();
            String password = String.valueOf(passwordEditText.getText()).trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                // FIX 4: Pass the username to the login method
                loginViewModel.login(username, password);
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            }
        });

        observeViewModel();
    }

    private void observeViewModel() {
        loginViewModel.getLoggedInUserLiveData().observe(this, user -> {
            if (user != null) {
                // FIX 5: Use getUsername() for the welcome message
                Toast.makeText(this, "Login Successful! Welcome " + user.getUsername(), Toast.LENGTH_SHORT).show();

                // This line is correct and uses the int ID.
                sessionManager.createLoginSession();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // Your User model has getUserType(), not getRole(). Use the correct method.
                intent.putExtra("usertype", user.getUserType());

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        loginViewModel.getLoginErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
