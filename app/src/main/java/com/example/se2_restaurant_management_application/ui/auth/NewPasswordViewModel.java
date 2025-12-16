package com.example.se2_restaurant_management_application.ui.auth;

// FIX 1: Import Application and AndroidViewModel
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
// Remove the old 'ViewModel' import

import com.example.se2_restaurant_management_application.data.UserRepository;
import com.example.se2_restaurant_management_application.data.models.User;

// FIX 2: Extend AndroidViewModel
public class NewPasswordViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    // FIX 3: Change the constructor to accept Application
    public NewPasswordViewModel(@NonNull Application application) {
        super(application); // Call the superclass constructor
        // FIX 4: Pass the application context to getInstance()
        this.userRepository = UserRepository.getInstance(application);
    }

    // Getters to observe the result of the update operation
    public LiveData<String> getUpdateSuccessLiveData() {
        return userRepository.getUpdateUserSuccessLiveData();
    }

    public LiveData<String> getUpdateErrorLiveData() {
        return userRepository.getUpdateUserErrorLiveData();
    }

    // --- Core method to update the user's password ---
    public void updateUserPassword(User user) {
        // This existing method in the repository handles the server update
        userRepository.updateUser(user);
    }
}
