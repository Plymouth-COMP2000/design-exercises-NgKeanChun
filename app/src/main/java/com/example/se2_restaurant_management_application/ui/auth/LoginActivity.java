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

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // Link to SignupActivity
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
            String username = String.valueOf(usernameEditText.getText()).trim();
            String password = String.valueOf(passwordEditText.getText()).trim();

            if (!username.isEmpty() && !password.isEmpty()) {
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
                Toast.makeText(this, "Login Successful! Welcome " + user.getUsername(), Toast.LENGTH_SHORT).show();
                sessionManager.createLoginSession();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
