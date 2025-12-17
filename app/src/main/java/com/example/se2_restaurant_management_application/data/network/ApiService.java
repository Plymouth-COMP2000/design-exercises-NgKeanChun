package com.example.se2_restaurant_management_application.data.network;

import com.example.se2_restaurant_management_application.data.models.ApiResponse;
import com.example.se2_restaurant_management_application.data.models.User;
import com.example.se2_restaurant_management_application.data.models.UserResponse;
import com.example.se2_restaurant_management_application.data.models.Notification;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.DELETE;
import retrofit2.http.Query;

public interface ApiService {

    @PUT("update_user/{student_id}/{username}")
    Call<ApiResponse> updateUser(
            @Path("student_id") String studentId,
            @Path("username") String username,
            @Body User user
    );
    @POST("create_student/{student_id}")
    Call<ApiResponse> createStudentDatabase(@Path("student_id") String studentId);

    @GET("read_all_users/{student_id}")
    Call<UserResponse> getAllUsers(@Path("student_id") String studentId);

    @POST("create_user/{student_id}")
    Call<ApiResponse> createUser(@Path("student_id") String studentId, @Body User user);


    /**
     * Creates a new notification on the server.
     */
    @POST("create_notification/{student_id}")
    Call<ApiResponse> createNotification(@Path("student_id") String studentId, @Body Notification notification);

    /**
     * Gets all notifications for a specific user.
     * Example URL: read_notifications.php?student_id=BSSE...&user_id=123
     */
    @GET("read_notifications/{student_id}")
    Call<ApiResponse> getNotificationsForUser(@Path("student_id") String studentId, @Query("user_id") int userId);

    @DELETE("delete_user/{student_id}/{username}")
    Call<ApiResponse> deleteUser(
            @Path("student_id") String studentId,
            @Path("username") String username
    );
}
