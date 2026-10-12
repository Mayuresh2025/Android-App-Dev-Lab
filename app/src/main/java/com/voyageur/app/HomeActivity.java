package com.voyageur.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button btnNetwork;
    private Button btnCreateTrip;
    private Button btnWeather;
    private Button btnPhotoGallery;
    private Button btnNavigation;
    private Button btnCamera;
    private Button btnMedia;

    private Button btnAiChatbot;
    private Button btnMlScanner;
    private Button btnAiAlerts;

    private LinearLayout navHome;
    private LinearLayout navMyTrips;
    private LinearLayout navProfile;

    private ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnNetwork = findViewById(R.id.btnNetwork);
        btnCreateTrip = findViewById(R.id.btnCreateTrip);
        btnWeather = findViewById(R.id.btnWeather);
        btnPhotoGallery = findViewById(R.id.btnPhotoGallery);
        btnNavigation = findViewById(R.id.btnCampusNav);
        btnCamera = findViewById(R.id.btnCamera);
        btnMedia = findViewById(R.id.btnMedia);

        btnAiChatbot = findViewById(R.id.btnAiChatbot);
        btnMlScanner = findViewById(R.id.btnMlScanner);
        btnAiAlerts = findViewById(R.id.btnAiAlerts);

        navHome = findViewById(R.id.navHome);
        navMyTrips = findViewById(R.id.navMyTrips);
        navProfile = findViewById(R.id.navProfile);

        ivProfile = findViewById(R.id.ivProfile);
        ImageButton btnNotifications = findViewById(R.id.btnNotifications);
        TextView tvSeeAllTools = findViewById(R.id.tvSeeAllTools);
        TextView tvSeeAllTrips = findViewById(R.id.tvSeeAllTrips);
        View cardHomeTripParis = findViewById(R.id.cardHomeTripParis);
        View cardHomeTripTokyo = findViewById(R.id.cardHomeTripTokyo);

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    openActivity(AISmartAlertsActivity.class));
        }

        if (tvSeeAllTools != null) {
            tvSeeAllTools.setOnClickListener(v ->
                    Toast.makeText(this, "Quick Tools: Weather, Camera, Maps, AI Assistant", Toast.LENGTH_SHORT).show());
        }

        if (tvSeeAllTrips != null) {
            tvSeeAllTrips.setOnClickListener(v -> openActivity(MyTripsActivity.class));
        }

        if (cardHomeTripParis != null) {
            cardHomeTripParis.setOnClickListener(v -> openActivity(MyTripsActivity.class));
        }

        if (cardHomeTripTokyo != null) {
            cardHomeTripTokyo.setOnClickListener(v -> openActivity(MyTripsActivity.class));
        }

        if (btnNetwork != null) {
            btnNetwork.setOnClickListener(v -> openActivity(NetworkActivity.class));
        }

        if (btnCreateTrip != null) {
            btnCreateTrip.setOnClickListener(v -> openActivity(CreateTripActivity.class));
        }

        if (btnWeather != null) {
            btnWeather.setOnClickListener(v -> openActivity(WeatherActivity.class));
        }

        if (btnPhotoGallery != null) {
            btnPhotoGallery.setOnClickListener(v -> openActivity(PhotoActivity.class));
        }

        if (btnCamera != null) {
            btnCamera.setOnClickListener(v -> openActivity(CameraActivity.class));
        }

        if (btnMedia != null) {
            btnMedia.setOnClickListener(v -> openActivity(MediaActivity.class));
        }

        if (btnNavigation != null) {
            btnNavigation.setOnClickListener(v -> openActivity(CampusNavigationActivity.class));
        }

        if (btnAiChatbot != null) {
            btnAiChatbot.setOnClickListener(v -> openActivity(ChatbotActivity.class));
        }

        if (btnMlScanner != null) {
            btnMlScanner.setOnClickListener(v -> openActivity(MLImageClassifierActivity.class));
        }

        if (btnAiAlerts != null) {
            btnAiAlerts.setOnClickListener(v -> openActivity(AISmartAlertsActivity.class));
        }

        // Card container click listeners
        setupCardClick(R.id.cardWeather, R.id.btnWeather, WeatherActivity.class);
        setupCardClick(R.id.cardCampusNav, R.id.btnCampusNav, CampusNavigationActivity.class);
        setupCardClick(R.id.cardCamera, R.id.btnCamera, CameraActivity.class);
        setupCardClick(R.id.cardAiChatbot, R.id.btnAiChatbot, ChatbotActivity.class);

        setupCardClick(R.id.cardPhotoGallery, R.id.btnPhotoGallery, PhotoActivity.class);
        setupCardClick(R.id.cardNetwork, R.id.btnNetwork, NetworkActivity.class);
        setupCardClick(R.id.cardMedia, R.id.btnMedia, MediaActivity.class);
        setupCardClick(R.id.cardMlScanner, R.id.btnMlScanner, MLImageClassifierActivity.class);
        setupCardClick(R.id.cardAiAlerts, R.id.btnAiAlerts, AISmartAlertsActivity.class);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Already on Home
            });
        }

        if (navMyTrips != null) {
            navMyTrips.setOnClickListener(v -> openActivity(MyTripsActivity.class));
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> openActivity(ProfileActivity.class));
        }

        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> openActivity(ProfileActivity.class));
        }
    }

    private void setupCardClick(int cardId, int buttonId, Class<?> activityClass) {
        View card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> openActivity(activityClass));
        }
        View button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> openActivity(activityClass));
        }
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(HomeActivity.this, activityClass);
        startActivity(intent);
    }
}
