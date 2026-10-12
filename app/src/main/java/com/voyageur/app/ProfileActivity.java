package com.voyageur.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvUserEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);
        View cardEditProfile = findViewById(R.id.cardEditProfile);
        View cardSavedPlaces = findViewById(R.id.cardSavedPlaces);
        View cardSettings = findViewById(R.id.cardSettings);
        View cardAbout = findViewById(R.id.cardAbout);
        View btnEditAvatar = findViewById(R.id.btnEditAvatar);

        View navHome = findViewById(R.id.navHome);
        View navMyTrips = findViewById(R.id.navMyTrips);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        loadUser();

        View.OnClickListener editAction = v -> showEditProfileDialog();

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(editAction);
        }
        if (cardEditProfile != null) {
            cardEditProfile.setOnClickListener(editAction);
        }
        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(editAction);
        }

        if (cardSavedPlaces != null) {
            cardSavedPlaces.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, SavedPlacesActivity.class)));
        }

        View layoutSavedPlaces = findViewById(R.id.layoutSavedPlaces);
        if (layoutSavedPlaces != null) {
            layoutSavedPlaces.setOnClickListener(v ->
                    startActivity(new Intent(ProfileActivity.this, SavedPlacesActivity.class)));
        }

        if (cardSettings != null) {
            cardSettings.setOnClickListener(v ->
                    Toast.makeText(this, "Settings opened", Toast.LENGTH_SHORT).show());
        }

        if (cardAbout != null) {
            cardAbout.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setTitle("About Voyageur")
                            .setMessage("Voyageur v1.0\nYour ultimate smart travel companion.\nExplore, Capture, Discover.")
                            .setPositiveButton("OK", null)
                            .show());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logoutUser());
        }

        // Bottom Navigation
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navMyTrips != null) {
            navMyTrips.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, MyTripsActivity.class));
                finish();
            });
        }
    }

    private void loadUser() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            if (tvUserName != null) {
                tvUserName.setText("Voyageur User");
            }
            if (tvUserEmail != null) {
                tvUserEmail.setText("nathan@gmail.com");
            }
            return;
        }

        String email = user.getEmail();
        if (!TextUtils.isEmpty(email) && tvUserEmail != null) {
            tvUserEmail.setText(email);
        }

        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty() && tvUserName != null) {
            tvUserName.setText(user.getDisplayName());
        } else if (tvUserName != null) {
            tvUserName.setText("Voyageur User");
        }
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditText etDialogName = dialogView.findViewById(R.id.etDialogName);
        EditText etDialogEmail = dialogView.findViewById(R.id.etDialogEmail);

        if (tvUserName != null) {
            etDialogName.setText(tvUserName.getText().toString());
        }
        if (tvUserEmail != null) {
            etDialogEmail.setText(tvUserEmail.getText().toString());
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etDialogName.getText().toString().trim();
                    String newEmail = etDialogEmail.getText().toString().trim();

                    if (!TextUtils.isEmpty(newName) && tvUserName != null) {
                        tvUserName.setText(newName);
                    }
                    if (!TextUtils.isEmpty(newEmail) && tvUserEmail != null) {
                        tvUserEmail.setText(newEmail);
                    }

                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
