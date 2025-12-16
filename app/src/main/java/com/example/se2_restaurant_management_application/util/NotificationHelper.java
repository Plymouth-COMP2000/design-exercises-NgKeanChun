package com.example.se2_restaurant_management_application.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.se2_restaurant_management_application.R;
import com.example.se2_restaurant_management_application.ui.main.MainActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "restaurant_channel";
    private static final String CHANNEL_NAME = "Restaurant Notifications";
    private static final String CHANNEL_DESC = "Notifications for reservation updates and confirmations";

    private final Context context;
    // FIX 1: Add a SettingsManager instance
    private final SettingsManager settingsManager;


    public NotificationHelper(Context context) {
        this.context = context;
        this.settingsManager = new SettingsManager(context); // Initialize it
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // FIX 2: The showNotification method now requires a userType
    public void showNotification(String title, String content, int notificationId, String userType) {
        // --- SETTINGS CHECK ---
        // Determine which master setting to check based on the user type
        String settingKey = "Guest".equalsIgnoreCase(userType)
                ? "guest_notifications_allowed"
                : "staff_notifications_allowed";

        // If the master notification switch for this user type is OFF, do not proceed.
        if (!settingsManager.getBoolean(settingKey, true)) {
            return; // Exit the method immediately, no pop-up will be shown.
        }
        // --- END OF SETTINGS CHECK ---

        // Check for Android 13+ notification permission first
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // Exit if permission is not granted
        }

        // --- The rest of the method is the same ---
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from_notification", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_IMMUTABLE :
                        PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(notificationId, builder.build());
    }
}
