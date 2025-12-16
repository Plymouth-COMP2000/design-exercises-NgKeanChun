package com.example.se2_restaurant_management_application.ui.auth;

// FIX 1: Import Application and AndroidViewModel
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
// Remove the old 'ViewModel' import
import com.example.se2_restaurant_management_application.data.UserRepository;
import com.example.se2_restaurant_management_application.data.models.User;

// FIX 2: Extend AndroidViewModel
public class LoginViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final LiveData<User> loggedInUserLiveData;
    private final LiveData<String> loginErrorLiveData;

    // FIX 3: Change the constructor to accept Application
    public LoginViewModel(@NonNull Application application) {
        super(application); // Call the superclass constructor
        // FIX 4: Pass the application context to getInstance()
        this.userRepository = UserRepository.getInstance(application);
        this.loggedInUserLiveData = userRepository.getLoggedInUserLiveData();
        this.loginErrorLiveData = userRepository.getLoginErrorLiveData();
    }

    // Public method to trigger the login process in the repository
    public void login(String username, String password) {
        userRepository.loginUser(username, password);
    }

    public LiveData<User> getLoggedInUserLiveData() {
        return loggedInUserLiveData;
    }

    public LiveData<String> getLoginErrorLiveData() {
        return loginErrorLiveData;
    }

    // This method ensures the ViewModel's state is cleared, preventing the observer from re-triggering.
    public void onLogout() {
        userRepository.logout();
    }
}
