package com.voyageur.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnCreateTrip = findViewById(R.id.btnCreateTrip);
        LinearLayout navMyTrips = findViewById(R.id.navMyTrips);
        LinearLayout navProfile = findViewById(R.id.navProfile);
        ImageView ivProfile = findViewById(R.id.ivProfile);

        btnCreateTrip.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, CreateTripActivity.class)));

        navMyTrips.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MyTripsActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        ivProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }
}
