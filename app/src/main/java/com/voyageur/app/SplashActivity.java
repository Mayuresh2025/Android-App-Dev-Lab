package com.voyageur.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {

            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {

                // User is already logged in
                Intent intent = new Intent(
                        SplashActivity.this,
                        HomeActivity.class
                );

                startActivity(intent);
                finish();

            } else {

                // User is not logged in
                Intent intent = new Intent(
                        SplashActivity.this,
                        LoginActivity.class
                );

                startActivity(intent);
                finish();
            }

        }, SPLASH_DELAY);
    }
}