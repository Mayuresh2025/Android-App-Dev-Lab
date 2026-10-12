package com.voyageur.app;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.voyageur.app.data.AppDatabase;
import com.voyageur.app.data.Trip;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedPlacesActivity extends AppCompatActivity {

    private LinearLayout savedPlacesContainer;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_places);

        savedPlacesContainer = findViewById(R.id.savedPlacesContainer);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadSavedPlaces();
    }

    private void loadSavedPlaces() {
        databaseExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            List<Trip> trips = database.tripDao().getAllTrips();
            runOnUiThread(() -> displaySavedPlaces(trips));
        });
    }

    private void displaySavedPlaces(List<Trip> trips) {
        savedPlacesContainer.removeAllViews();
        if (trips == null || trips.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No saved places or bookmarked trips yet.");
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(20, 80, 20, 80);
            savedPlacesContainer.addView(empty);
            return;
        }

        for (Trip trip : trips) {
            CardView card = new CardView(this);
            card.setRadius(16);
            card.setCardElevation(3);
            card.setUseCompatPadding(true);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(18, 18, 18, 18);

            TextView title = new TextView(this);
            title.setText("📍 " + trip.getDestination());
            title.setTextSize(18);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            content.addView(title);

            TextView dates = new TextView(this);
            dates.setText("Dates: " + trip.getStartDate() + " → " + trip.getEndDate());
            dates.setTextSize(13);
            dates.setPadding(0, 6, 0, 0);
            content.addView(dates);

            TextView budget = new TextView(this);
            budget.setText("Budget: $" + String.format(Locale.getDefault(), "%.0f", trip.getBudget()) + " • " + trip.getNotes());
            budget.setTextSize(13);
            budget.setPadding(0, 6, 0, 0);
            content.addView(budget);

            card.addView(content);
            savedPlacesContainer.addView(card);
        }
    }

    @Override
    protected void onDestroy() {
        databaseExecutor.shutdown();
        super.onDestroy();
    }
}
