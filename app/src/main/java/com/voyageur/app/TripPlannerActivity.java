package com.voyageur.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class TripPlannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_planner);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnProfile = findViewById(R.id.btnProfile);
        TextView tvTripTitle = findViewById(R.id.tvTripTitle);
        TextView tvTripDestination = findViewById(R.id.tvTripDestination);
        TextView tvTripDates = findViewById(R.id.tvTripDates);
        TextView tvTripSummary = findViewById(R.id.tvTripSummary);
        LinearLayout llItineraryContainer = findViewById(R.id.llItineraryContainer);
        Button btnConfirmTrip = findViewById(R.id.btnConfirmTrip);

        String name = getIntent().getStringExtra("TRIP_NAME");
        String destination = getIntent().getStringExtra("TRIP_DESTINATION");
        String dates = getIntent().getStringExtra("TRIP_DATES");
        int duration = getIntent().getIntExtra("TRIP_DURATION", 3);

        if (!TextUtils.isEmpty(name)) {
            tvTripTitle.setText(name);
        }
        if (!TextUtils.isEmpty(destination)) {
            tvTripDestination.setText(destination);
            tvTripSummary.setText("A wonderful " + duration + "-day getaway to " + destination + " filled with iconic landmarks, local culture, and memorable experiences.");
        }
        if (!TextUtils.isEmpty(dates)) {
            tvTripDates.setText(dates);
        }

        generateItinerary(duration, destination, llItineraryContainer);

        btnBack.setOnClickListener(v -> finish());

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(TripPlannerActivity.this, ProfileActivity.class)));

        btnConfirmTrip.setOnClickListener(v -> {
            Toast.makeText(this, "Trip saved successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TripPlannerActivity.this, MyTripsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void generateItinerary(int duration, String destination, LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 1; i <= duration; i++) {
            View dayView = inflater.inflate(R.layout.item_itinerary_day, container, false);
            TextView tvDayNumber = dayView.findViewById(R.id.tvDayNumber);
            LinearLayout llItineraryItems = dayView.findViewById(R.id.llItineraryItems);

            tvDayNumber.setText("Day " + i);

            // Add sample items for each day
            addItineraryItem(llItineraryItems, "Morning: Explore " + destination + " landmarks");
            addItineraryItem(llItineraryItems, "Afternoon: Visit local " + destination + " markets");
            addItineraryItem(llItineraryItems, "Evening: Dinner at a " + destination + " specialty restaurant");

            container.addView(dayView);
        }
    }

    private void addItineraryItem(LinearLayout container, String text) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (8 * getResources().getDisplayMetrics().density);
        params.setMargins(0, margin, 0, margin);
        textView.setLayoutParams(params);
        textView.setText("• " + text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        textView.setTextSize(13);
        container.addView(textView);
    }
}
