package com.voyageur.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.voyageur.app.data.AppDatabase;
import com.voyageur.app.data.Trip;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyTripsActivity extends AppCompatActivity {

    private LinearLayout tripsContainer;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private FirestoreTripSync firestoreTripSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);

        tripsContainer = findViewById(R.id.tripsContainer);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ImageButton btnCreateTripTop = findViewById(R.id.btnCreateTripTop);
        if (btnCreateTripTop != null) {
            btnCreateTripTop.setOnClickListener(v ->
                    startActivity(new Intent(MyTripsActivity.this, CreateTripActivity.class)));
        }

        // Bottom Navigation
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(MyTripsActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(MyTripsActivity.this, ProfileActivity.class));
                finish();
            });
        }

        firestoreTripSync = new FirestoreTripSync(this);
        loadTrips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips();
    }

    private void loadTrips() {
        databaseExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            List<Trip> trips = database.tripDao().getAllTrips();

            runOnUiThread(() -> {
                displayTrips(trips);
                firestoreTripSync.uploadAllTrips();
            });
        });
    }

    private void displayTrips(List<Trip> trips) {
        tripsContainer.removeAllViews();

        if (trips == null || trips.isEmpty()) {
            // Display empty view
            TextView emptyMessage = new TextView(this);
            emptyMessage.setText("No trips created yet.\nTap '+' above to plan your first adventure!");
            emptyMessage.setTextSize(15);
            emptyMessage.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            emptyMessage.setGravity(Gravity.CENTER);
            emptyMessage.setPadding(20, 80, 20, 80);
            tripsContainer.addView(emptyMessage);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < trips.size(); i++) {
            Trip trip = trips.get(i);
            View tripView = inflater.inflate(R.layout.item_trip_card, tripsContainer, false);

            ImageView ivTripThumb = tripView.findViewById(R.id.ivTripThumb);
            TextView tvTripStatusBadge = tripView.findViewById(R.id.tvTripStatusBadge);
            TextView tvTripDestination = tripView.findViewById(R.id.tvTripDestination);
            TextView tvTripDates = tripView.findViewById(R.id.tvTripDates);
            TextView tvTripBudget = tripView.findViewById(R.id.tvTripBudget);
            TextView tvTripTravelers = tripView.findViewById(R.id.tvTripTravelers);
            ImageButton btnTripMore = tripView.findViewById(R.id.btnTripMore);

            tvTripDestination.setText(trip.getDestination());
            tvTripDates.setText(trip.getStartDate() + " – " + trip.getEndDate());
            tvTripBudget.setText("₹" + String.format(java.util.Locale.getDefault(), "%,.0f", trip.getBudget()));

            String notes = trip.getNotes();
            if (notes != null && !notes.trim().isEmpty()) {
                tvTripTravelers.setText(notes);
            } else {
                tvTripTravelers.setText("1 Traveler");
            }

            // Cycle between destination thumb gradients
            int mod = i % 4;
            if (mod == 0) {
                ivTripThumb.setBackgroundResource(R.drawable.bg_photo_placeholder_paris);
            } else if (mod == 1) {
                ivTripThumb.setBackgroundResource(R.drawable.bg_photo_placeholder_tokyo);
            } else if (mod == 2) {
                ivTripThumb.setBackgroundResource(R.drawable.bg_photo_placeholder_bali);
            } else {
                ivTripThumb.setBackgroundResource(R.drawable.bg_photo_placeholder_mumbai);
            }

            // Badge alternation
            if (i == trips.size() - 1 && trips.size() > 2) {
                tvTripStatusBadge.setText("Past");
                tvTripStatusBadge.setBackgroundResource(R.drawable.bg_badge_past);
                tvTripStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_past_text));
            } else {
                tvTripStatusBadge.setText("Upcoming");
                tvTripStatusBadge.setBackgroundResource(R.drawable.bg_badge_upcoming);
                tvTripStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_upcoming_text));
            }

            btnTripMore.setOnClickListener(v -> showTripOptionsDialog(trip));

            tripsContainer.addView(tripView);
        }
    }

    private void showTripOptionsDialog(Trip trip) {
        CharSequence[] options = {"View / Edit Trip", "Delete Trip", "Cancel"};

        new AlertDialog.Builder(this)
                .setTitle(trip.getDestination())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(this, "Destination: " + trip.getDestination(), Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        deleteTrip(trip);
                    }
                })
                .show();
    }

    private void deleteTrip(Trip trip) {
        databaseExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            database.tripDao().deleteTrip(trip);
            firestoreTripSync.deleteTrip(trip);

            runOnUiThread(() -> {
                Toast.makeText(MyTripsActivity.this, "Trip deleted", Toast.LENGTH_SHORT).show();
                loadTrips();
            });
        });
    }
}
