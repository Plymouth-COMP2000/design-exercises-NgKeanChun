package com.example.se2_restaurant_management_application.ui.auth;

// FIX 1: Import Application and AndroidViewModel
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
// Remove the old 'ViewModel' import

import com.example.se2_restaurant_management_application.data.UserRepository;
import com.example.se2_restaurant_management_application.data.models.User;

// FIX 2: Extend AndroidViewModel instead of ViewModel
public class SignupViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    public LiveData<String> signupSuccessMessage;
    public LiveData<String> signupErrorMessage;

    // FIX 3: Change the constructor to accept Application
    public SignupViewModel(@NonNull Application application) {
        super(application); // Call the superclass constructor
        // FIX 4: Pass the application context to getInstance()
        this.userRepository = UserRepository.getInstance(application);
        this.signupSuccessMessage = userRepository.getSignupSuccessLiveData();
        this.signupErrorMessage = userRepository.getSignupErrorLiveData();
    }

    // Method the SignupActivity will call
    public void signup(User user) {
        userRepository.signupUser(user);
    }

    // Getters for the LiveData
    public LiveData<String> getSignupSuccessMessage() {
        return signupSuccessMessage;
    }

    public LiveData<String> getSignupErrorMessage() {
        return signupErrorMessage;
    }
}
