package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.se2_restaurant_management_application.data.UserRepository;
import com.example.se2_restaurant_management_application.data.models.Reservation;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.util.SessionManager;

import java.util.List;

public class AccountViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final LiveData<User> loggedInUserLiveData;
    private final LiveData<List<User>> allUsersLiveData;

    public AccountViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance(application);
        sessionManager = new SessionManager(application);

        loggedInUserLiveData = userRepository.getLoggedInUserLiveData();
        allUsersLiveData = userRepository.getAllUsersLiveData();
        userRepository.getAllUsers();
    }

    // Public method for the fragment to observe the user data
    public LiveData<User> getLoggedInUser() {
        return loggedInUserLiveData;
    }

    // Public method to get all users
    public LiveData<List<User>> getAllUsers() {
        return allUsersLiveData;
    }

    public LiveData<User> getGuestUserById(String userId) {
        return userRepository.getUserFromApiById(userId);
    }
    public LiveData<String> getUpdateUserSuccessLiveData() {
        return userRepository.getUpdateUserSuccessLiveData();
    }

    public void updateUser(User user) {
        userRepository.updateUser(user);
    }
    public void clearUpdateUserSuccess() {

        ((MutableLiveData<String>) userRepository.getUpdateUserSuccessLiveData()).setValue(null);
    }

    public void refreshAllUsers() {
        userRepository.getAllUsers();
    }

    public User getUserForReservation(Reservation reservation) {
        List<User> users = allUsersLiveData.getValue();
        if (users == null || reservation == null) {
            return null;
        }
        for (User user : users) {
            if (user.getId().equals(reservation.getUserId())) {
                return user;
            }
        }
        return null;
    }

    // Getters for the delete LiveData from the repository
    public LiveData<String> getDeleteUserSuccessLiveData() {
        return userRepository.getDeleteUserSuccessLiveData();
    }

    public LiveData<String> getDeleteUserErrorLiveData() {
        return userRepository.getDeleteUserErrorLiveData();
    }

    // Method to clear the event after it's handled
    public void consumeDeleteUserEvents() {
        ((MutableLiveData<String>) userRepository.getDeleteUserSuccessLiveData()).postValue(null);
        ((MutableLiveData<String>) userRepository.getDeleteUserErrorLiveData()).postValue(null);
    }

    public void deleteUser(String username) {
        userRepository.deleteUser(username);
    }

    public void updateUserImage(String userId, String imageUri) {
        userRepository.updateUserImage(userId, imageUri);
    }

    public void logout() {
        userRepository.logout();
    }
}
