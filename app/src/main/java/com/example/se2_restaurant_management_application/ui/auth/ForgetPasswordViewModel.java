package com.example.se2_restaurant_management_application.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.se2_restaurant_management_application.data.repository.UserRepository;
import com.example.se2_restaurant_management_application.data.models.User;

public class ForgetPasswordViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final LiveData<User> foundUserLiveData;
    private final LiveData<String> errorLiveData;


    public ForgetPasswordViewModel(@NonNull Application application) {
        super(application); // Call the superclass constructor
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
