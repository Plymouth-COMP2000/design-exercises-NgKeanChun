package com.example.se2_restaurant_management_application.data.local;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.se2_restaurant_management_application.data.local.DatabaseHelper;
import com.example.se2_restaurant_management_application.data.models.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationRepository {

    private static volatile NotificationRepository instance;
    private final DatabaseHelper dbHelper;
    private final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    // LiveData for holding the list of notifications
    private final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
    // LiveData for any errors
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // Private constructor for Singleton pattern
    private NotificationRepository(Application application) {
        this.dbHelper = new DatabaseHelper(application);
    }

    // Singleton getInstance method
    public static NotificationRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (NotificationRepository.class) {
                if (instance == null) {
                    instance = new NotificationRepository(application);
                }
            }
        }
        return instance;
    }

    // Public getters for the LiveData
    public LiveData<List<Notification>> getNotificationsLiveData() {
        return notificationsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /**
     * Fetches all notifications for a given user ID from the local SQLite database.
     */
    public void fetchNotificationsForUser(int userId) {
        databaseWriteExecutor.execute(() -> {
            List<Notification> notificationList = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String selection = DatabaseHelper.COLUMN_NOTIFICATION_USER_ID + " = ?";
            String[] selectionArgs = { String.valueOf(userId) };

            Cursor cursor = db.query(DatabaseHelper.TABLE_NOTIFICATIONS, null, selection, selectionArgs, null, null, DatabaseHelper.COLUMN_NOTIFICATION_ID + " DESC");

            try {
                if (cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_ID));
                        int dbUserId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_USER_ID));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_TITLE));
                        String body = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_BODY));
                        String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_STATUS));
                        boolean isRead = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTIFICATION_IS_READ)) == 1;

                        notificationList.add(new Notification(id, dbUserId, title, body, status, isRead));
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                mainThreadHandler.post(() -> errorLiveData.setValue("Error reading notifications from database"));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            mainThreadHandler.post(() -> notificationsLiveData.setValue(notificationList));
        });
    }

    /**
     * Inserts a new notification into the local SQLite database.
     */
    public void createNotification(Notification notification) {
        databaseWriteExecutor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NOTIFICATION_USER_ID, notification.getUserId());
            values.put(DatabaseHelper.COLUMN_NOTIFICATION_TITLE, notification.getTitle());
            values.put(DatabaseHelper.COLUMN_NOTIFICATION_BODY, notification.getBody());
            values.put(DatabaseHelper.COLUMN_NOTIFICATION_STATUS, notification.getStatus());
            values.put(DatabaseHelper.COLUMN_NOTIFICATION_IS_READ, notification.isRead() ? 1 : 0);

            long id = db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, values);
            if (id != -1) {
                fetchNotificationsForUser(notification.getUserId());
            }
        });
    }

    public void markNotificationAsRead(Notification notification) {
        databaseWriteExecutor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_NOTIFICATION_IS_READ, 1); // 1 for true

            String selection = DatabaseHelper.COLUMN_NOTIFICATION_ID + " = ?";
            String[] selectionArgs = { String.valueOf(notification.getId()) };

            db.update(DatabaseHelper.TABLE_NOTIFICATIONS, values, selection, selectionArgs);

        });
    }
}
