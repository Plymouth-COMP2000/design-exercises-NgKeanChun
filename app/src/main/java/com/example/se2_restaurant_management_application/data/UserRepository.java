package com.example.se2_restaurant_management_application.data;

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
    private static volatile UserRepository instance;

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

    private UserRepository(Application application) {
        this.apiService = RetrofitClient.getApiService();
        this.dbHelper = new DatabaseHelper(application);
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

    // --- Public Getters for LiveData ---
    public LiveData<String> getDeleteUserSuccessLiveData() { return deleteUserSuccessLiveData; }
    public LiveData<String> getDeleteUserErrorLiveData() { return deleteUserErrorLiveData; }
    public LiveData<String> getUpdateUserSuccessLiveData() { return updateUserSuccessLiveData; }
    public LiveData<String> getUpdateUserErrorLiveData() { return updateUserErrorLiveData; }
    public LiveData<User> getLoggedInUserLiveData() { return loggedInUserLiveData; }
    public LiveData<String> getLoginErrorLiveData() { return loginErrorLiveData; }
    public LiveData<List<User>> getAllUsersLiveData() { return allUsersLiveData; }
    public LiveData<User> getFoundUserLiveData() { return foundUserLiveData; }
    public LiveData<String> getUserSearchErrorLiveData() { return userSearchErrorLiveData; }
    public LiveData<String> getSignupSuccessLiveData() { return signupSuccessLiveData; }
    public LiveData<String> getSignupErrorLiveData() { return signupErrorLiveData; }

    // --- Repository Public Methods ---
    public void getAllUsers() {
        apiService.getAllUsers(studentId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsersLiveData.postValue(response.body().getUsers());
                } else {
                    allUsersLiveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                allUsersLiveData.postValue(new ArrayList<>());
            }
        });
    }

    public void loginUser(String username, String password) {
        apiService.getAllUsers(studentId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUsers() != null) {
                    List<User> userList = response.body().getUsers();
                    User foundUser = null;

                    for (User user : userList) {
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
                        loggedInUserLiveData.postValue(null);
                    }
                } else {
                    loginErrorLiveData.postValue("Login failed: " + response.message());
                    loggedInUserLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                loginErrorLiveData.postValue("Network error: " + t.getMessage());
                loggedInUserLiveData.postValue(null);
            }
        });
    }

    private String getLocalImageUri(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String imageUri = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_USER_IMAGE_URI},
                    DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
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
                    signupSuccessLiveData.postValue(response.body().getMessage());
                    signupErrorLiveData.postValue(null);
                } else {
                    String errorMessage = "Signup failed: ";
                    if (response.errorBody() != null) {
                        try {
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
                signupErrorLiveData.postValue("Network error: " + t.getMessage());
                signupSuccessLiveData.postValue(null);
            }
        });
    }

    public void findUserByIdentifier(String identifier, boolean isPhone) {
        if (allUsersLiveData.getValue() == null) {
            userSearchErrorLiveData.postValue("User list not available. Please try again.");
            return;
        }

        List<User> userList = allUsersLiveData.getValue();
        User foundUser = null;

        for (User user : userList) {
            if (isPhone) {
                if (user.getContact().equals(identifier)) {
                    foundUser = user;
                    break;
                }
            } else {
                if (user.getEmail().equalsIgnoreCase(identifier)) {
                    foundUser = user;
                    break;
                }
            }
        }

        if (foundUser != null) {
            foundUserLiveData.postValue(foundUser);
            userSearchErrorLiveData.postValue(null);
        } else {
            foundUserLiveData.postValue(null);
            userSearchErrorLiveData.postValue("No account found with that " + (isPhone ? "phone number." : "email address."));
        }
    }

    public void updateUserImage(int userId, String imageUri) {
        databaseWriteExecutor.execute(() -> {
            Log.d("UserRepository", "Updating image for user ID: " + userId);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_ID, userId);
            values.put(DatabaseHelper.COLUMN_USER_IMAGE_URI, imageUri);
            db.replace(DatabaseHelper.TABLE_USERS, null, values);

            User currentUserState = loggedInUserLiveData.getValue();
            if (currentUserState != null && currentUserState.getId() == userId) {
                User updatedUser = currentUserState;
                updatedUser.setImageUri(imageUri);
                mainThreadHandler.post(() -> {
                    loggedInUserLiveData.setValue(updatedUser);
                    updateUserSuccessLiveData.setValue("Profile picture saved.");
                });
            }
        });
    }

    public void updateUser(User user) {
        updateUserSuccessLiveData.postValue(null);
        updateUserErrorLiveData.postValue(null);

        apiService.updateUser(studentId, user.getUsername(), user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("UpdateUser", "Success: " + response.body().getMessage());
                    updateUserSuccessLiveData.postValue(response.body().getMessage());
                    getAllUsers();
                } else {
                    String errorBodyString = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBodyString = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("UpdateUser", "Error parsing error body", e);
                        }
                    }
                    Log.e("UpdateUser", "Update Failed. Code: " + response.code() + ", Body: " + errorBodyString);
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
        deleteUserSuccessLiveData.postValue(null);
        deleteUserErrorLiveData.postValue(null);

        apiService.deleteUser(studentId, username).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    deleteUserSuccessLiveData.postValue(response.body().getMessage());
                    User deletedUser = loggedInUserLiveData.getValue();
                    if(deletedUser != null){
                        // Code to delete local data would continue here
                    }
                } else {
                    // Handle error
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Handle failure
            }
        });
    }
}
