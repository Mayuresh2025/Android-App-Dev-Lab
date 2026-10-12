package com.voyageur.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.voyageur.app.data.AppDatabase;
import com.voyageur.app.data.Trip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateTripActivity extends AppCompatActivity {

    private EditText etDestination;
    private EditText etStartDate;
    private EditText etEndDate;
    private EditText etBudget;

    private TextView tvTravelerCount;

    private int travelerCount = 1;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private FirestoreTripSync firestoreTripSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_trip);

        // --------------------------------------------------
        // FIRESTORE SYNC
        // --------------------------------------------------

        firestoreTripSync =
                new FirestoreTripSync(this);

        // --------------------------------------------------
        // FIND VIEWS
        // --------------------------------------------------

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        etDestination =
                findViewById(R.id.etDestination);

        etStartDate =
                findViewById(R.id.etStartDate);

        etEndDate =
                findViewById(R.id.etEndDate);

        etBudget =
                findViewById(R.id.etBudget);

        tvTravelerCount =
                findViewById(R.id.tvTravelerCount);

        ImageButton btnMinus =
                findViewById(R.id.btnMinus);

        ImageButton btnPlus =
                findViewById(R.id.btnPlus);

        Button btnStartPlanning =
                findViewById(R.id.btnStartPlanning);

        // --------------------------------------------------
        // BACK BUTTON
        // --------------------------------------------------

        btnBack.setOnClickListener(v -> finish());

        // --------------------------------------------------
        // START DATE
        // --------------------------------------------------

        etStartDate.setOnClickListener(
                v -> showDatePicker(etStartDate)
        );

        // --------------------------------------------------
        // END DATE
        // --------------------------------------------------

        etEndDate.setOnClickListener(
                v -> showDatePicker(etEndDate)
        );

        // --------------------------------------------------
        // TRAVELERS MINUS
        // --------------------------------------------------

        btnMinus.setOnClickListener(v -> {

            if (travelerCount > 1) {

                travelerCount--;

                tvTravelerCount.setText(
                        String.valueOf(travelerCount)
                );
            }
        });

        // --------------------------------------------------
        // TRAVELERS PLUS
        // --------------------------------------------------

        btnPlus.setOnClickListener(v -> {

            travelerCount++;

            tvTravelerCount.setText(
                    String.valueOf(travelerCount)
            );
        });

        // --------------------------------------------------
        // START PLANNING
        // --------------------------------------------------

        btnStartPlanning.setOnClickListener(
                v -> createTrip()
        );
    }

    // ======================================================
    // DATE PICKER
    // ======================================================

    private void showDatePicker(EditText target) {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            String date =
                                    dayOfMonth + "/" +
                                            (month + 1) + "/" +
                                            year;

                            target.setText(date);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    // ======================================================
    // CREATE TRIP
    // ======================================================

    private void createTrip() {

        String destination =
                etDestination.getText()
                        .toString()
                        .trim();

        String startDate =
                etStartDate.getText()
                        .toString()
                        .trim();

        String endDate =
                etEndDate.getText()
                        .toString()
                        .trim();

        String budgetText =
                etBudget.getText()
                        .toString()
                        .trim();

        // --------------------------------------------------
        // VALIDATE DESTINATION
        // --------------------------------------------------

        if (TextUtils.isEmpty(destination)) {

            etDestination.setError(
                    "Destination is required"
            );

            etDestination.requestFocus();

            return;
        }

        // --------------------------------------------------
        // VALIDATE DATES
        // --------------------------------------------------

        if (TextUtils.isEmpty(startDate) ||
                TextUtils.isEmpty(endDate)) {

            etStartDate.setError(
                    "Please select trip dates"
            );

            etStartDate.requestFocus();

            return;
        }

        // --------------------------------------------------
        // BUDGET
        // --------------------------------------------------

        double budget = 0;

        if (!TextUtils.isEmpty(budgetText)) {

            try {

                budget =
                        Double.parseDouble(
                                budgetText
                        );

                if (budget < 0) {

                    etBudget.setError(
                            "Budget cannot be negative"
                    );

                    etBudget.requestFocus();

                    return;
                }

            } catch (NumberFormatException e) {

                etBudget.setError(
                        "Enter a valid budget"
                );

                etBudget.requestFocus();

                return;
            }
        }

        // --------------------------------------------------
        // DURATION
        // --------------------------------------------------

        int duration =
                calculateDuration(
                        startDate,
                        endDate
                );

        // --------------------------------------------------
        // CREATE TRIP OBJECT
        // --------------------------------------------------

        Trip trip =
                new Trip(
                        destination,
                        startDate,
                        endDate,
                        budget,
                        "Travelers: " +
                                travelerCount
                );

        final double finalBudget = budget;

        // --------------------------------------------------
        // SAVE TO ROOM
        // --------------------------------------------------

        databaseExecutor.execute(() -> {

            try {

                AppDatabase database =
                        AppDatabase.getInstance(
                                getApplicationContext()
                        );

                // Insert and receive generated Room ID
                long generatedId =
                        database.tripDao()
                                .insertTrip(trip);

                trip.setId(
                        (int) generatedId
                );

                // --------------------------------------------------
                // UPLOAD TO FIRESTORE
                // --------------------------------------------------

                firestoreTripSync.uploadTrip(trip);

                // --------------------------------------------------
                // OPEN TRIP PLANNER
                // --------------------------------------------------

                runOnUiThread(() -> {

                    android.widget.Toast.makeText(
                            CreateTripActivity.this,
                            "Trip saved and synced successfully!",
                            android.widget.Toast.LENGTH_SHORT
                    ).show();

                    Intent intent =
                            new Intent(
                                    CreateTripActivity.this,
                                    TripPlannerActivity.class
                            );

                    intent.putExtra(
                            "TRIP_NAME",
                            destination + " Trip"
                    );

                    intent.putExtra(
                            "TRIP_DESTINATION",
                            destination
                    );

                    intent.putExtra(
                            "TRIP_DATES",
                            startDate +
                                    " → " +
                                    endDate +
                                    " • " +
                                    duration +
                                    " Days"
                    );

                    intent.putExtra(
                            "TRIP_DURATION",
                            duration
                    );

                    intent.putExtra(
                            "TRIP_START_DATE",
                            startDate
                    );

                    intent.putExtra(
                            "TRIP_END_DATE",
                            endDate
                    );

                    intent.putExtra(
                            "TRIP_BUDGET",
                            finalBudget
                    );

                    intent.putExtra(
                            "TRIP_TRAVELERS",
                            travelerCount
                    );

                    startActivity(intent);
                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() ->
                        android.widget.Toast.makeText(
                                CreateTripActivity.this,
                                "Failed to save trip",
                                android.widget.Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }

    // ======================================================
    // CALCULATE DURATION
    // ======================================================

    private int calculateDuration(
            String startDate,
            String endDate) {

        try {

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "d/M/yyyy",
                            Locale.getDefault()
                    );

            Date start =
                    sdf.parse(startDate);

            Date end =
                    sdf.parse(endDate);

            if (start != null &&
                    end != null) {

                long difference =
                        end.getTime() -
                                start.getTime();

                int days =
                        (int) (
                                difference /
                                        (1000L *
                                                60 *
                                                60 *
                                                24)
                        ) + 1;

                return Math.max(
                        days,
                        1
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 1;
    }

    // ======================================================
    // DESTROY
    // ======================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        databaseExecutor.shutdown();

        if (firestoreTripSync != null) {
            firestoreTripSync.shutdown();
        }
    }
}