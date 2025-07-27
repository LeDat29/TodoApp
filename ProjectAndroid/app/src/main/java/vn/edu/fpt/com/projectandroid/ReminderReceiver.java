package vn.edu.fpt.com.projectandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "reminder_channel";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "=== ReminderReceiver triggered ===");
        
        try {
            String title = intent.getStringExtra("title");
            String notifyType = intent.getStringExtra("notifyType");
            
            Log.d(TAG, "Title: " + title);
            Log.d(TAG, "NotifyType: " + notifyType);

            // Create notification channel (Android 8+)
            createNotificationChannel(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Reminder")
                    .setContentText(title)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            // Chỉ có âm thanh nếu notifyType là Sound
            if ("Sound".equals(notifyType)) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
                Log.d(TAG, "Setting sound notification");
            } else {
                builder.setDefaults(0); // Không rung, không âm thanh
                Log.d(TAG, "Setting silent notification");
            }

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                int notificationId = (int) System.currentTimeMillis();
                manager.notify(notificationId, builder.build());
                Log.d(TAG, "=== Notification sent successfully with ID: " + notificationId + " ===");
            } else {
                Log.e(TAG, "ERROR: NotificationManager is null");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "ERROR in onReceive: " + e.getMessage(), e);
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, 
                "Reminders", 
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminder notifications");
            channel.enableVibration(false);
            channel.enableLights(true);
            channel.setShowBadge(true);
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created successfully");
            } else {
                Log.e(TAG, "ERROR: Cannot create notification channel - manager is null");
            }
        }
    }
} 