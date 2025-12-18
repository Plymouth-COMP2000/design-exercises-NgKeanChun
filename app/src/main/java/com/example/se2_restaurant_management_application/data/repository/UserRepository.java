package com.example.se2_restaurant_management_application.data.repository;


import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.se2_restaurant_management_application.data.local.DatabaseHelper;
import com.example.se2_restaurant_management_application.data.models.ApiResponse;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.data.models.UserResponse;
import com.example.se2_restaurant_management_application.data.network.ApiService;
import com.example.se2_restaurant_management_application.data.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final DatabaseHelper dbHelper;
    private final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final ApiService apiService;

    // --- Singleton Implementation ---
    private static volatile UserRepository instance;

    private UserRepository(Application application) {
        this.apiService = RetrofitClient.getApiService();
        this.dbHelper = new DatabaseHelper(application); // Initialize dbHelper
    }

    public static UserRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository(application);
                }
            }
        }
        return instance;
    }

    // --- LiveData Definitions ---
    private final String studentId = "BSSE2506036";
    private final MutableLiveData<String> deleteUserSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> deleteUserErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> updateUserSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> updateUserErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> loggedInUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> loginErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<User> foundUserLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userSearchErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> signupSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> signupErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> allUsersLiveData = new MutableLiveData<>();


    // --- Public Getters for LiveData ---

    public LiveData<String> getDeleteUserSuccessLiveData() { return deleteUserSuccessLiveData; }
    public LiveData<String> getDeleteUserErrorLiveData() { return deleteUserErrorLiveData; }

    public LiveData<String> getUpdateUserSuccessLiveData() { return updateUserSuccessLiveData; }

    public LiveData<String> getUpdateUserErrorLiveData() { return updateUserErrorLiveData; }
    public LiveData<User> getLoggedInUserLiveData() {
        return loggedInUserLiveData;
    }

    public LiveData<String> getLoginErrorLiveData() {
        return loginErrorLiveData;
    }
    public LiveData<List<User>> getAllUsersLiveData() {
        return allUsersLiveData;
    }
    public LiveData<User> getFoundUserLiveData() {
        return foundUserLiveData;
    }

    public LiveData<String> getUserSearchErrorLiveData() {
        return userSearchErrorLiveData;
    }
    public LiveData<String> getSignupSuccessLiveData() {
        return signupSuccessLiveData;
    }

    public LiveData<String> getSignupErrorLiveData() {
        return signupErrorLiveData;
    }

        // --- Repository Public Methods ---

    public LiveData<User> getUserFromApiById(String userId) {
        MutableLiveData<User> foundUserLiveData = new MutableLiveData<>();
        apiService.getAllUsers(studentId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DataTrace", "UserRepository: Network call successful. Looping through " + response.body().getUsers().size() + " users to find ID: " + userId);
                    for (User user : response.body().getUsers()) {
                        // Compare two Strings using the .equals() method
                        if (user.getId().equals(userId)) {
                            Log.d("DataTrace", "UserRepository: MATCH FOUND! User is " + user.getFullName());
                            foundUserLiveData.postValue(user);
                            return; // User found
                        }
                    }
                }
                Log.e("DataTrace", "UserRepository: NO MATCH FOUND after looping. Posting null.");
                foundUserLiveData.postValue(null);
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("DataTrace", "UserRepository: Network call FAILED. Error: " + t.getMessage());
                foundUserLiveData.postValue(null);
            }
        });
        return foundUserLiveData;
    }


    public void getAllUsers() {
        apiService.getAllUsers(studentId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsersLiveData.postValue(response.body().getUsers());
                } else {
                    // Post an empty list or handle the error appropriately
                    allUsersLiveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Post an empty list or handle the error
                allUsersLiveData.postValue(new ArrayList<>());
            }
        });
    }


    public void loginUser(String username, String password) {
        ApiService apiService = RetrofitClient.getApiService();
        String studentId = "BSSE2506036";

        apiService.getAllUsers(studentId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUsers() != null) {
                    User foundUser = null;
                    for (User user : response.body().getUsers()) {
                        if (user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password)) {
                            foundUser = user;
                            break;
                        }
                    }
                    if (foundUser != null) {
                        String localImageUri = getLocalImageUri(foundUser.getId());
                        if (localImageUri != null) {
                            foundUser.setImageUri(localImageUri);
                        }
                        loggedInUserLiveData.postValue(foundUser);
                        loginErrorLiveData.postValue(null);
                    } else {
                        loginErrorLiveData.postValue("Invalid username or password.");
                    }
                } else {
                    loginErrorLiveData.postValue("Login failed: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                loginErrorLiveData.postValue("Network error: " + t.getMessage());
            }
        });
    }
    private String getLocalImageUri(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String imageUri = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_USER_IMAGE_URI},
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{userId},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IMAGE_URI));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imageUri;
    }

    public void signupUser(User user) {
        apiService.createUser(studentId, user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // API successfully created the user
                    signupSuccessLiveData.postValue(response.body().getMessage());
                    signupErrorLiveData.postValue(null); // Clear previous errors
                } else {
                    // API returned an error (e.g., user exists, bad data)
                    String errorMessage = "Signup failed: ";
                    if (response.errorBody() != null) {
                        try {
                            // Try to parse the error response from the API
                            errorMessage += response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage += response.message();
                        }
                    } else {
                        errorMessage += response.message();
                    }
                    signupErrorLiveData.postValue(errorMessage);
                    signupSuccessLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Network failure (e.g., no internet)
                signupErrorLiveData.postValue("Network error: " + t.getMessage());
                signupSuccessLiveData.postValue(null);
            }
        });
    }

    public void findUserByIdentifier(String identifier, boolean isPhone) {
        // Ensure the full user list is loaded and available
        if (allUsersLiveData.getValue() == null) {
            mainThreadHandler.post(() -> userSearchErrorLiveData.setValue("User list not available. Please try again."));
            return;
        }

        databaseWriteExecutor.execute(() -> {
            List<User> userList = allUsersLiveData.getValue();
            User foundUser = null;

            if (userList != null) {
                for (User user : userList) {
                    if (isPhone) {
                        // Search by phone number
                        if (user.getContact() != null && user.getContact().equals(identifier)) {
                            foundUser = user;
                            break;
                        }
                    } else {
                        // Search by email (case-insensitive)
                        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(identifier)) {
                            foundUser = user;
                            break;
                        }
                    }
                }
            }

            User finalFoundUser = foundUser;
            mainThreadHandler.post(() -> {
                if (finalFoundUser != null) {
                    this.foundUserLiveData.setValue(finalFoundUser);
                    this.userSearchErrorLiveData.setValue(null); // Clear any previous errors
                } else {
                    // User was not found
                    this.userSearchErrorLiveData.setValue("Account not found with the provided details.");
                    this.foundUserLiveData.setValue(null);
                }
            });
        });
    }

    public void updateUserImage(String userId, String imageUri) {
        databaseWriteExecutor.execute(() -> {
            Log.d("UserRepository", "updateUserImage called for user ID: " + userId + " with new URI: " + imageUri);

            // Step 1: Save the new image URI to the local database for the correct user ID.
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_ID, userId);
            values.put(DatabaseHelper.COLUMN_USER_IMAGE_URI, imageUri);
            db.replace(DatabaseHelper.TABLE_USERS, null, values);
            Log.d("UserRepository", "Database 'replace' completed for user ID: " + userId);

            // Step 2: Get the currently logged-in user object from our LiveData.
            User currentUserState = loggedInUserLiveData.getValue();

            // Step 3: Check if the current state is valid and belongs to the same user.
            if (currentUserState != null && currentUserState.getId() == userId) {
                // It's the correct user. Create a NEW User object to avoid modifying the existing one in-place.
                User updatedUser = currentUserState; // Start with the existing state
                updatedUser.setImageUri(imageUri);   // Apply the new URI

                // Step 4: Post the updated, trusted object back to the UI.
                mainThreadHandler.post(() -> {
                    Log.d("UserRepository", "Posting updated user object to LiveData for user ID: " + userId);
                    loggedInUserLiveData.setValue(updatedUser); // This triggers UI refresh.
                    updateUserSuccessLiveData.setValue("Profile picture saved.");
                });

            } else {
                // This is a safety log.
                Log.e("UserRepository", "Could not update LiveData: User ID " + userId + " does not match the logged-in state.");
            }
        });
    }




    public void updateUser(User user) {
        updateUserSuccessLiveData.postValue(null);
        updateUserErrorLiveData.postValue(null);

        // Call the NEW ApiService method, passing the username from the User object as the second path parameter.
        // The 'user' object itself is passed as the body.
        apiService.updateUser(studentId, user.getUsername(), user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("UpdateUser", "Success: " + response.body().getMessage());
                    updateUserSuccessLiveData.postValue(response.body().getMessage());
                    getAllUsers(); // Refresh user list after a successful update
                } else {
                    // Keep the enhanced error logging
                    String errorBodyString = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBodyString = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("UpdateUser", "Error parsing error body", e);
                        }
                    }
                    Log.e("UpdateUser", "Update Failed. Code: " + response.code() + ", Message: " + response.message() + ", Error Body: " + errorBodyString);
                    updateUserErrorLiveData.postValue("Update failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("UpdateUser", "Network Failure", t);
                updateUserErrorLiveData.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteUser(String username) {
        // Clear the LiveData immediately to provide quick feedback
        deleteUserSuccessLiveData.postValue(null);
        deleteUserErrorLiveData.postValue(null);

        // Perform the network request
        apiService.deleteUser(studentId, username).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // On success, post the success message
                    deleteUserSuccessLiveData.postValue(response.body().getMessage());

                    // Also delete the user's local data
                    User deletedUser = loggedInUserLiveData.getValue();
                    if(deletedUser != null){
                        databaseWriteExecutor.execute(() -> {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.delete(DatabaseHelper.TABLE_USERS, DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(deletedUser.getId())});
                        });
                    }

                } else {
                    // On failure, construct a detailed error message
                    String errorMsg = "Deletion failed with code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = "Deletion failed: " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("UserRepository", "Error parsing delete user error body", e);
                    }
                    deleteUserErrorLiveData.postValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Handle network failures (e.g., no internet connection)
                deleteUserErrorLiveData.postValue("Network error during deletion: " + t.getMessage());
            }
        });
    }

    public void restoreSession(String userId) {
        // Ensure the list of all users is available.
        List<User> userList = allUsersLiveData.getValue();
        if (userList != null && !userList.isEmpty()) {
            User foundUser = null;
            // Loop through the list to find the user with the matching ID.
            for (User user : userList) {
                if (user.getId() == userId) {
                    foundUser = user;
                    break;
                }
            }

            if (foundUser != null) {
                // User found, post them to the LiveData to restore the session.
                loggedInUserLiveData.postValue(foundUser);
            } else {
                // The saved user ID was not found in the list from the server.
                logout();
            }
        }
    }

    public void logout() {
        loggedInUserLiveData.postValue(null);
        loginErrorLiveData.postValue(null);
        updateUserSuccessLiveData.postValue(null);
        updateUserErrorLiveData.postValue(null);
        foundUserLiveData.postValue(null);
        userSearchErrorLiveData.postValue(null);
    }

}
