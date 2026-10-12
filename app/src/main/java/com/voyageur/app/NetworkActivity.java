package com.voyageur.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkActivity extends AppCompatActivity {

    private TextView tvNetworkStatus;
    private TextView tvApiResult;
    private Button btnCheckConnection;
    private Button btnTestApi;
    private Button btnUploadApi;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        tvNetworkStatus = findViewById(R.id.tvNetworkStatus);
        tvApiResult = findViewById(R.id.tvApiResult);
        btnCheckConnection = findViewById(R.id.btnCheckConnection);
        btnTestApi = findViewById(R.id.btnTestApi);
        btnUploadApi = findViewById(R.id.btnUploadApi);

        btnCheckConnection.setOnClickListener(v ->
                checkNetworkConnection());

        btnTestApi.setOnClickListener(v -> testRestApi());
        btnUploadApi.setOnClickListener(v -> uploadRestApi());

        findViewById(R.id.btnNetworkBack).setOnClickListener(v ->
                finish());

        checkNetworkConnection();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        Network network = manager.getActiveNetwork();

        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                manager.getNetworkCapabilities(network);

        return capabilities != null
                && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private void checkNetworkConnection() {
        if (isInternetAvailable()) {
            tvNetworkStatus.setText(
                    "Internet Status: Connected\n"
                            + "Internet access is available.");
        } else {
            tvNetworkStatus.setText(
                    "Internet Status: Offline\n"
                            + "Enable Wi-Fi or mobile data.");
        }
    }

    private void testRestApi() {
        if (!isInternetAvailable()) {
            tvApiResult.setText(
                    "No internet connection. Please try again.");
            return;
        }

        btnTestApi.setEnabled(false);
        tvApiResult.setText("Sending HTTP GET request...");

        executor.execute(() -> {
            HttpURLConnection connection = null;
            String result;

            try {
                URL url = new URL(
                        "https://jsonplaceholder.typicode.com/todos/1");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty(
                        "Accept", "application/json");

                int responseCode = connection.getResponseCode();

                InputStream stream =
                        responseCode >= 200 && responseCode < 400
                                ? connection.getInputStream()
                                : connection.getErrorStream();

                StringBuilder response = new StringBuilder();

                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    stream, StandardCharsets.UTF_8))) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line).append('\n');
                        }
                    }
                }

                result = "HTTP Response Code: " + responseCode
                        + "\n\nResponse Body:\n" + response;

            } catch (Exception e) {
                result = "API Request Failed\n\n"
                        + e.getClass().getSimpleName()
                        + ": " + e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            String finalResult = result;

            runOnUiThread(() -> {
                tvApiResult.setText(finalResult);
                btnTestApi.setEnabled(true);
            });
        });
    }

    private void uploadRestApi() {
        if (!isInternetAvailable()) {
            tvApiResult.setText("No internet connection. Please try again.");
            return;
        }

        btnUploadApi.setEnabled(false);
        tvApiResult.setText("Sending HTTP POST (Upload Data)...");

        executor.execute(() -> {
            HttpURLConnection connection = null;
            String result;

            try {
                URL url = new URL("https://jsonplaceholder.typicode.com/posts");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = "{\"title\": \"Voyageur Travel Note\", \"body\": \"Uploaded travel log via REST API\", \"userId\": 1}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                InputStream stream = responseCode >= 200 && responseCode < 400
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder response = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line).append('\n');
                        }
                    }
                }

                result = "HTTP POST Upload Successful!\nResponse Code: " + responseCode
                        + "\n\nServer Response:\n" + response;

            } catch (Exception e) {
                result = "Upload Failed\n\n" + e.getClass().getSimpleName() + ": " + e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            String finalResult = result;
            runOnUiThread(() -> {
                tvApiResult.setText(finalResult);
                btnUploadApi.setEnabled(true);
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (tvNetworkStatus != null) {
            checkNetworkConnection();
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
