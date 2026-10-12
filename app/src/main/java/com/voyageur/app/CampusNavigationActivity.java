package com.voyageur.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CampusNavigationActivity extends AppCompatActivity {

    private TextView tvLocationStatus;
    private TextView tvCoordinates;
    private TextView tvLocationStatusBadge;
    private EditText etDestination;
    private Button btnGetLocation;

    private LocationManager locationManager;
    private Location currentLocation;
    private boolean tracking = false;
    private boolean permissionRequestInProgress = false;

    private ActivityResultLauncher<String[]> permissionLauncher;

    private final LocationListener locationListener =
            new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    currentLocation = location;
                    displayLocation(location);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    if (tvLocationStatus != null) {
                        tvLocationStatus.setText("Location provider enabled. Finding location...");
                    }
                    if (tvLocationStatusBadge != null) {
                        tvLocationStatusBadge.setText("Searching");
                    }
                }

                @Override
                public void onProviderDisabled(String provider) {
                    if (tvLocationStatus != null) {
                        tvLocationStatus.setText("Location provider disabled. Enable Location services.");
                    }
                    if (tvLocationStatusBadge != null) {
                        tvLocationStatusBadge.setText("Inactive");
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_navigation);

        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        tvLocationStatusBadge = findViewById(R.id.tvLocationStatusBadge);
        etDestination = findViewById(R.id.etDestination);
        btnGetLocation = findViewById(R.id.btnGetLocation);

        Button btnOpenMaps = findViewById(R.id.btnOpenMaps);
        Button btnGetDirections = findViewById(R.id.btnGetDirections);
        ImageButton btnNavigationBack = findViewById(R.id.btnNavigationBack);
        ImageButton btnRecenter = findViewById(R.id.btnRecenter);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    permissionRequestInProgress = false;

                    if (hasLocationPermission()) {
                        startLocationTracking();
                    } else {
                        if (tvLocationStatus != null) {
                            tvLocationStatus.setText("Location permission denied. Allow access in Settings.");
                        }
                        if (tvLocationStatusBadge != null) {
                            tvLocationStatusBadge.setText("Denied");
                        }
                    }
                }
        );

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> {
                if (tracking) {
                    stopLocationTracking();
                } else if (hasLocationPermission()) {
                    startLocationTracking();
                } else {
                    requestLocationPermission();
                }
            });
        }

        if (btnOpenMaps != null) {
            btnOpenMaps.setOnClickListener(v -> openCurrentLocationInMaps());
        }

        if (btnGetDirections != null) {
            btnGetDirections.setOnClickListener(v -> navigateToDestination());
        }

        if (btnNavigationBack != null) {
            btnNavigationBack.setOnClickListener(v -> {
                stopLocationTracking();
                finish();
            });
        }

        if (btnRecenter != null) {
            btnRecenter.setOnClickListener(v -> {
                if (currentLocation != null) {
                    displayLocation(currentLocation);
                    Toast.makeText(this, "Recentered to current GPS coordinates", Toast.LENGTH_SHORT).show();
                } else {
                    startLocationTracking();
                }
            });
        }

        if (tvCoordinates != null) {
            tvCoordinates.setText("📍 Latitude: --\n📍 Longitude: --\n🎯 Accuracy: --");
        }
        if (tvLocationStatusBadge != null) {
            tvLocationStatusBadge.setText("Inactive");
        }
    }

    private boolean hasFinePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCoarsePermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasLocationPermission() {
        return hasFinePermission() || hasCoarsePermission();
    }

    private void requestLocationPermission() {
        if (permissionRequestInProgress) {
            return;
        }

        permissionRequestInProgress = true;

        permissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void startLocationTracking() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        boolean registeredAtLeastOneProvider = false;

        try {
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!gpsEnabled && !networkEnabled) {
                if (tvLocationStatus != null) {
                    tvLocationStatus.setText("Location services disabled. Enable GPS.");
                }
                if (tvLocationStatusBadge != null) {
                    tvLocationStatusBadge.setText("Disabled");
                }

                try {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } catch (Exception ignored) {
                }

                return;
            }

            tracking = true;
            if (btnGetLocation != null) {
                btnGetLocation.setText("Stop Live Location");
            }
            if (tvLocationStatusBadge != null) {
                tvLocationStatusBadge.setText("Active");
            }

            if (hasFinePermission() && gpsEnabled) {
                try {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            2000L,
                            2f,
                            locationListener
                    );

                    registeredAtLeastOneProvider = true;

                    Location lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastGps != null) {
                        currentLocation = lastGps;
                        displayLocation(lastGps);
                    }
                } catch (SecurityException | IllegalArgumentException ignored) {
                }
            }

            if (networkEnabled) {
                try {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            3000L,
                            5f,
                            locationListener
                    );

                    registeredAtLeastOneProvider = true;

                    Location lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (lastNetwork != null && (currentLocation == null || lastNetwork.getTime() > currentLocation.getTime())) {
                        currentLocation = lastNetwork;
                        displayLocation(lastNetwork);
                    }
                } catch (SecurityException | IllegalArgumentException ignored) {
                }
            }

            if (!registeredAtLeastOneProvider) {
                tracking = false;
                if (btnGetLocation != null) {
                    btnGetLocation.setText("Start Live Location");
                }
                if (tvLocationStatusBadge != null) {
                    tvLocationStatusBadge.setText("Inactive");
                }
            }

        } catch (Exception e) {
            tracking = false;
            if (btnGetLocation != null) {
                btnGetLocation.setText("Start Live Location");
            }
            if (tvLocationStatusBadge != null) {
                tvLocationStatusBadge.setText("Error");
            }
        }
    }

    private void displayLocation(Location location) {
        if (tvLocationStatusBadge != null) {
            tvLocationStatusBadge.setText("Active");
        }

        if (tvCoordinates != null) {
            tvCoordinates.setText(
                    String.format(
                            java.util.Locale.getDefault(),
                            "📍 Latitude: %.4f\n📍 Longitude: %.4f\n🎯 Accuracy: %.0f m",
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getAccuracy()
                    )
            );
        }
    }

    private void stopLocationTracking() {
        tracking = false;

        try {
            if (locationManager != null && hasLocationPermission()) {
                locationManager.removeUpdates(locationListener);
            }
        } catch (SecurityException ignored) {
        }

        if (btnGetLocation != null) {
            btnGetLocation.setText("Start Live Location");
        }

        if (tvLocationStatusBadge != null) {
            tvLocationStatusBadge.setText("Inactive");
        }
    }

    private void openCurrentLocationInMaps() {
        if (currentLocation == null) {
            Toast.makeText(this, "Start live location first to acquire coordinates.", Toast.LENGTH_SHORT).show();
            return;
        }

        String coordinates = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        openMapsUri(Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(coordinates)));
    }

    private void navigateToDestination() {
        String destination = (etDestination != null) ? etDestination.getText().toString().trim() : "";

        if (destination.isEmpty()) {
            if (etDestination != null) {
                etDestination.setError("Enter a destination.");
                etDestination.requestFocus();
            }
            return;
        }

        if (currentLocation == null) {
            Toast.makeText(this, "Start live location first to acquire coordinates.", Toast.LENGTH_SHORT).show();
            return;
        }

        String origin = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        String url = "https://www.google.com/maps/dir/?api=1"
                + "&origin=" + Uri.encode(origin)
                + "&destination=" + Uri.encode(destination)
                + "&travelmode=driving";

        openMapsUri(Uri.parse(url));
    }

    private void openMapsUri(Uri uri) {
        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapsIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapsIntent);
        } catch (Exception mapsUnavailable) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open Google Maps.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        stopLocationTracking();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopLocationTracking();
        super.onDestroy();
    }
}
