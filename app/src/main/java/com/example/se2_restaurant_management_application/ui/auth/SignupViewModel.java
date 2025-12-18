package com.example.se2_restaurant_management_application.ui.auth;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.se2_restaurant_management_application.data.repository.UserRepository;
import com.example.se2_restaurant_management_application.data.models.User;

public class SignupViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    public LiveData<String> signupSuccessMessage;
    public LiveData<String> signupErrorMessage;

    public SignupViewModel(@NonNull Application application) {
        super(application); // Call the superclass constructor
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
