package com.voyageur.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MyTripsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button fabCreateTrip = findViewById(R.id.fabCreateTrip);
        CardView cardTripParis = findViewById(R.id.cardTripParis);
        CardView cardTripTokyo = findViewById(R.id.cardTripTokyo);

        btnBack.setOnClickListener(v -> finish());

        fabCreateTrip.setOnClickListener(v ->
                startActivity(new Intent(MyTripsActivity.this, CreateTripActivity.class)));

        cardTripParis.setOnClickListener(v -> openTripPlanner("Paris Adventure", "Paris, France", "20 Aug – 25 Aug"));
        cardTripTokyo.setOnClickListener(v -> openTripPlanner("Tokyo Explorer", "Tokyo, Japan", "05 Sep – 10 Sep"));
    }

    private void openTripPlanner(String name, String destination, String dates) {
        Intent intent = new Intent(MyTripsActivity.this, TripPlannerActivity.class);
        intent.putExtra("TRIP_NAME", name);
        intent.putExtra("TRIP_DESTINATION", destination);
        intent.putExtra("TRIP_DATES", dates);
        startActivity(intent);
    }
}
