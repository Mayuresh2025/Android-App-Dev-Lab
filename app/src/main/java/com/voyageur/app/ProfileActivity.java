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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditText etDialogName = dialogView.findViewById(R.id.etDialogName);
        EditText etDialogEmail = dialogView.findViewById(R.id.etDialogEmail);

        etDialogName.setText(tvUserName.getText().toString());
        etDialogEmail.setText(tvUserEmail.getText().toString());

        new AlertDialog.Builder(this)
                .setTitle(R.string.edit_profile)
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etDialogName.getText().toString().trim();
                    String newEmail = etDialogEmail.getText().toString().trim();

                    if (!TextUtils.isEmpty(newName)) {
                        tvUserName.setText(newName);
                    }
                    if (!TextUtils.isEmpty(newEmail)) {
                        tvUserEmail.setText(newEmail);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
