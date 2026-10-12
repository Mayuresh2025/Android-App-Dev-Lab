package com.voyageur.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class AISmartAlertsActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "voyageur_ai_alerts";
    private static final int PERMISSION_REQUEST_CODE = 101;

    private TextView tvAlertCountSummary;
    private LinearLayout llAlertsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_smart_alerts);

        ImageView btnBack = findViewById(R.id.btnBack);
        tvAlertCountSummary = findViewById(R.id.tvAlertCountSummary);
        Button btnRescanAlerts = findViewById(R.id.btnRescanAlerts);
        Button btnTestNotification = findViewById(R.id.btnTestNotification);
        llAlertsContainer = findViewById(R.id.llAlertsContainer);

        btnBack.setOnClickListener(v -> finish());

        createNotificationChannel();
        requestNotificationPermission();

        btnRescanAlerts.setOnClickListener(v -> {
            loadAlerts();
            Toast.makeText(this, "AI Scan completed. Smart alerts updated!", Toast.LENGTH_SHORT).show();
        });

        btnTestNotification.setOnClickListener(v -> sendSystemNotification(
                "🚨 Voyageur AI Smart Alert",
                "Your Paris trip budget exceeds safety limits! AI suggests reviewing accommodation plans."
        ));

        loadAlerts();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Voyageur AI Smart Alerts";
            String description = "Notifications for budget risks, weather warnings & travel tips";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void loadAlerts() {
        List<AISmartAlert> alertList = AISmartAlertEngine.generateActiveAlerts();
        llAlertsContainer.removeAllViews();

        tvAlertCountSummary.setText(alertList.size() + " Active AI Smart Alerts");

        for (AISmartAlert alert : alertList) {
            View card = getLayoutInflater().inflate(R.layout.item_ai_alert, llAlertsContainer, false);

            TextView tvAlertTitle = card.findViewById(R.id.tvAlertTitle);
            TextView tvAlertPriority = card.findViewById(R.id.tvAlertPriority);
            TextView tvAlertMessage = card.findViewById(R.id.tvAlertMessage);
            Button btnSendNotification = card.findViewById(R.id.btnSendNotification);
            Button btnAlertAction = card.findViewById(R.id.btnAlertAction);

            tvAlertTitle.setText(alert.getTitle());
            tvAlertMessage.setText(alert.getMessage());
            tvAlertPriority.setText(alert.getPriority());

            if ("HIGH".equalsIgnoreCase(alert.getPriority())) {
                tvAlertPriority.setBackgroundColor(0xFFFFEBEE);
                tvAlertPriority.setTextColor(0xFFC62828);
            } else if ("MEDIUM".equalsIgnoreCase(alert.getPriority())) {
                tvAlertPriority.setBackgroundColor(0xFFFFF3E0);
                tvAlertPriority.setTextColor(0xFFE65100);
            } else {
                tvAlertPriority.setBackgroundColor(0xFFE8F5E9);
                tvAlertPriority.setTextColor(0xFF2E7D32);
            }

            btnAlertAction.setText(alert.getActionText());
            btnAlertAction.setOnClickListener(v ->
                    Toast.makeText(this, "Action executed: " + alert.getActionText(), Toast.LENGTH_SHORT).show());

            btnSendNotification.setOnClickListener(v ->
                    sendSystemNotification(alert.getTitle(), alert.getMessage()));

            llAlertsContainer.addView(card);
        }
    }

    private void sendSystemNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission is required to post status bar alerts.", Toast.LENGTH_SHORT).show();
                requestNotificationPermission();
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            int notificationId = (int) System.currentTimeMillis();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                notificationManager.notify(notificationId, builder.build());
                Toast.makeText(this, "Notification pushed to status bar! Check notification shade.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
