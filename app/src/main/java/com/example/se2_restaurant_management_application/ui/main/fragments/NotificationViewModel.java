package com.example.se2_restaurant_management_application.ui.main.fragments;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.se2_restaurant_management_application.data.local.NotificationRepository;
import com.example.se2_restaurant_management_application.data.models.Notification;

import java.util.List;

public class NotificationViewModel extends AndroidViewModel {

    private final NotificationRepository notificationRepository;
    private final LiveData<List<Notification>> notificationsLiveData;
    private final MediatorLiveData<String> errorLiveData = new MediatorLiveData<>();

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        this.notificationRepository = NotificationRepository.getInstance(application);
        this.notificationsLiveData = notificationRepository.getNotificationsLiveData();

        // Listen to the repository's error source
        errorLiveData.addSource(notificationRepository.getErrorLiveData(), error -> errorLiveData.setValue(error));
    }

    public LiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    public void fetchNotifications(int userId) {
        notificationRepository.fetchNotificationsForUser(userId);
    }

    public void createNotification(Notification notification) {
        notificationRepository.createNotification(notification);
    }


    public void markNotificationAsRead(Notification notification) {
        notificationRepository.markNotificationAsRead(notification);
    }
}
