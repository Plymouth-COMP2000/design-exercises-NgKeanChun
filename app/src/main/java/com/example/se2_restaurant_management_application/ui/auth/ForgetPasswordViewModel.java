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

// FIX 2: Extend AndroidViewModel instead of ViewModel
public class ForgetPasswordViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final LiveData<User> foundUserLiveData;
    private final LiveData<String> errorLiveData;

    // FIX 3: Change the constructor to accept the Application context
    public ForgetPasswordViewModel(@NonNull Application application) {
        super(application); // Call the superclass constructor
        // FIX 4: Pass the application context to the repository's getInstance method
        this.userRepository = UserRepository.getInstance(application);
        this.foundUserLiveData = userRepository.getFoundUserLiveData();
        this.errorLiveData = userRepository.getUserSearchErrorLiveData();
    }

    /**
     * Finds a user by a given identifier.
     * @param identifier The username or email to search for.
     * @param isPhone true if the identifier is a phone number, false if it's an email.
     */
    public void findUserByIdentifier(String identifier, boolean isPhone) {
        userRepository.findUserByIdentifier(identifier, isPhone);
    }

    /**
     * Resets the found user LiveData to null.
     * This prevents the observer from re-triggering navigation on configuration changes
     * or when returning to the fragment.
     */
    public void consumeFoundUserEvent() {
        // We need to cast the LiveData to MutableLiveData to change its value.
        if (foundUserLiveData instanceof MutableLiveData) {
            ((MutableLiveData<User>) foundUserLiveData).setValue(null);
        }
    }

    // Getters for the Activity to observe the results
    public LiveData<User> getFoundUserLiveData() {
        return foundUserLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }
}
