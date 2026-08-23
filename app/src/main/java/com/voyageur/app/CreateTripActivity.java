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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateTripActivity extends AppCompatActivity {

    private EditText etDestination, etStartDate, etEndDate, etBudget;
    private TextView tvTravelerCount;
    private int travelerCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        ImageButton btnBack = findViewById(R.id.btnBack);
        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etBudget = findViewById(R.id.etBudget);
        tvTravelerCount = findViewById(R.id.tvTravelerCount);

        ImageButton btnMinus = findViewById(R.id.btnMinus);
        ImageButton btnPlus = findViewById(R.id.btnPlus);
        Button btnStartPlanning = findViewById(R.id.btnStartPlanning);

        btnBack.setOnClickListener(v -> finish());

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        btnMinus.setOnClickListener(v -> {
            if (travelerCount > 1) {
                travelerCount--;
                tvTravelerCount.setText(String.valueOf(travelerCount));
            }
        });

        btnPlus.setOnClickListener(v -> {
            travelerCount++;
            tvTravelerCount.setText(String.valueOf(travelerCount));
        });

        btnStartPlanning.setOnClickListener(v -> attemptStartPlanning());
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            target.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void attemptStartPlanning() {
        String destination = etDestination.getText().toString().trim();
        String startDateStr = etStartDate.getText().toString().trim();
        String endDateStr = etEndDate.getText().toString().trim();

        if (TextUtils.isEmpty(destination)) {
            etDestination.setError("Destination is required");
            etDestination.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(startDateStr) || TextUtils.isEmpty(endDateStr)) {
            etStartDate.setError("Please select trip dates");
            return;
        }

        int duration = 3; // Default
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Date start = sdf.parse(startDateStr);
            Date end = sdf.parse(endDateStr);
            if (start != null && end != null) {
                long diff = end.getTime() - start.getTime();
                duration = (int) (diff / (1000 * 60 * 60 * 24)) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (duration <= 0) duration = 1;

        Intent intent = new Intent(CreateTripActivity.this, TripPlannerActivity.class);
        intent.putExtra("TRIP_NAME", destination + " Trip");
        intent.putExtra("TRIP_DESTINATION", destination);
        intent.putExtra("TRIP_DATES", startDateStr + " – " + endDateStr + " · " + duration + " Days");
        intent.putExtra("TRIP_DURATION", duration);
        startActivity(intent);
    }
}
