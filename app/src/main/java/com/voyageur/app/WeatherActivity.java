package com.voyageur.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etCity;
    private Button btnGetWeather;
    private ProgressBar progressBar;
    private TextView tvWeatherResult;

    private TextView tvWeatherCityName;
    private TextView tvWeatherRegion;
    private TextView tvWeatherCurrentDate;
    private TextView tvWeatherTemp;
    private TextView tvWeatherConditionText;
    private TextView tvWeatherFeelsLike;
    private TextView tvWeatherHumidity;
    private TextView tvWeatherWind;
    private ImageView ivWeatherCondition;
    private ImageView btnClearSearch;
    private ImageView ivSearchIcon;

    // 5-day forecast views
    private final TextView[] tvForecastDays = new TextView[5];
    private final ImageView[] ivForecastIcons = new ImageView[5];
    private final TextView[] tvForecastMaxs = new TextView[5];
    private final TextView[] tvForecastMins = new TextView[5];

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static class WeatherData {
        String cityName;
        String region;
        double currentTemp;
        double feelsLike;
        int humidity;
        double windSpeed;
        int weatherCode;

        List<DailyForecast> dailyForecasts = new ArrayList<>();
    }

    private static class DailyForecast {
        String dayName;
        int weatherCode;
        double maxTemp;
        double minTemp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        btnBack = findViewById(R.id.btnBack);
        etCity = findViewById(R.id.etCity);
        btnGetWeather = findViewById(R.id.btnGetWeather);
        progressBar = findViewById(R.id.progressBar);
        tvWeatherResult = findViewById(R.id.tvWeatherResult);

        tvWeatherCityName = findViewById(R.id.tvWeatherCityName);
        tvWeatherRegion = findViewById(R.id.tvWeatherRegion);
        tvWeatherCurrentDate = findViewById(R.id.tvWeatherCurrentDate);
        tvWeatherTemp = findViewById(R.id.tvWeatherTemp);
        tvWeatherConditionText = findViewById(R.id.tvWeatherConditionText);
        tvWeatherFeelsLike = findViewById(R.id.tvWeatherFeelsLike);
        tvWeatherHumidity = findViewById(R.id.tvWeatherHumidity);
        tvWeatherWind = findViewById(R.id.tvWeatherWind);
        ivWeatherCondition = findViewById(R.id.ivWeatherCondition);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        ivSearchIcon = findViewById(R.id.ivSearchIcon);

        // Bind 5-day forecast views
        tvForecastDays[0] = findViewById(R.id.tvForecastDay1);
        tvForecastDays[1] = findViewById(R.id.tvForecastDay2);
        tvForecastDays[2] = findViewById(R.id.tvForecastDay3);
        tvForecastDays[3] = findViewById(R.id.tvForecastDay4);
        tvForecastDays[4] = findViewById(R.id.tvForecastDay5);

        ivForecastIcons[0] = findViewById(R.id.ivForecastIcon1);
        ivForecastIcons[1] = findViewById(R.id.ivForecastIcon2);
        ivForecastIcons[2] = findViewById(R.id.ivForecastIcon3);
        ivForecastIcons[3] = findViewById(R.id.ivForecastIcon4);
        ivForecastIcons[4] = findViewById(R.id.ivForecastIcon5);

        tvForecastMaxs[0] = findViewById(R.id.tvForecastMax1);
        tvForecastMaxs[1] = findViewById(R.id.tvForecastMax2);
        tvForecastMaxs[2] = findViewById(R.id.tvForecastMax3);
        tvForecastMaxs[3] = findViewById(R.id.tvForecastMax4);
        tvForecastMaxs[4] = findViewById(R.id.tvForecastMax5);

        tvForecastMins[0] = findViewById(R.id.tvForecastMin1);
        tvForecastMins[1] = findViewById(R.id.tvForecastMin2);
        tvForecastMins[2] = findViewById(R.id.tvForecastMin3);
        tvForecastMins[3] = findViewById(R.id.tvForecastMin4);
        tvForecastMins[4] = findViewById(R.id.tvForecastMin5);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (etCity != null) {
                    etCity.setText("");
                }
            });
        }

        if (ivSearchIcon != null) {
            ivSearchIcon.setOnClickListener(v -> fetchWeather());
        }

        if (btnGetWeather != null) {
            btnGetWeather.setOnClickListener(v -> fetchWeather());
        }

        if (etCity != null) {
            etCity.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    fetchWeather();
                    return true;
                }
                return false;
            });
        }

        // Set live date string
        if (tvWeatherCurrentDate != null) {
            String currentDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(new Date());
            tvWeatherCurrentDate.setText(currentDate);
        }

        // Default initial load: Mumbai
        etCity.setText("Mumbai");
        fetchWeather();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void fetchWeather() {
        String city = (etCity != null) ? etCity.getText().toString().trim() : "";
        if (TextUtils.isEmpty(city)) {
            if (etCity != null) {
                etCity.setError("Please enter a destination city");
            }
            return;
        }

        hideKeyboard();

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection available. Please check your network.", Toast.LENGTH_LONG).show();
            if (tvWeatherResult != null) {
                tvWeatherResult.setText("Unable to fetch weather. No internet connection.");
            }
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        executor.execute(() -> {
            try {
                // Step 1: Geocoding via Open-Meteo
                double lat = Double.NaN;
                double lon = Double.NaN;
                String resolvedName = city;
                String resolvedRegion = "";

                String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.name());
                String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedCity + "&count=1&language=en&format=json";
                String geoResponse = httpGet(geoUrl);

                if (geoResponse != null) {
                    JSONObject geoJson = new JSONObject(geoResponse);
                    if (geoJson.has("results")) {
                        JSONArray results = geoJson.getJSONArray("results");
                        if (results.length() > 0) {
                            JSONObject first = results.getJSONObject(0);
                            lat = first.getDouble("latitude");
                            lon = first.getDouble("longitude");
                            resolvedName = first.optString("name", city);

                            String country = first.optString("country", "");
                            String admin1 = first.optString("admin1", "");
                            if (!TextUtils.isEmpty(admin1) && !TextUtils.isEmpty(country)) {
                                resolvedRegion = admin1 + ", " + country;
                            } else if (!TextUtils.isEmpty(country)) {
                                resolvedRegion = country;
                            } else if (!TextUtils.isEmpty(admin1)) {
                                resolvedRegion = admin1;
                            }
                        }
                    }
                }

                // Fallback to Photon geocoding if Open-Meteo returned no match
                if (Double.isNaN(lat) || Double.isNaN(lon)) {
                    String photonUrl = "https://photon.komoot.io/api/?q=" + encodedCity + "&limit=1";
                    String photonResp = httpGet(photonUrl);
                    if (photonResp != null) {
                        JSONObject pJson = new JSONObject(photonResp);
                        JSONArray features = pJson.optJSONArray("features");
                        if (features != null && features.length() > 0) {
                            JSONObject feat = features.getJSONObject(0);
                            JSONObject geom = feat.getJSONObject("geometry");
                            JSONArray coords = geom.getJSONArray("coordinates");
                            lon = coords.getDouble(0);
                            lat = coords.getDouble(1);

                            JSONObject props = feat.optJSONObject("properties");
                            if (props != null) {
                                resolvedName = props.optString("name", city);
                                String country = props.optString("country", "");
                                String state = props.optString("state", "");
                                if (!TextUtils.isEmpty(state) && !TextUtils.isEmpty(country)) {
                                    resolvedRegion = state + ", " + country;
                                } else if (!TextUtils.isEmpty(country)) {
                                    resolvedRegion = country;
                                }
                            }
                        }
                    }
                }

                if (Double.isNaN(lat) || Double.isNaN(lon)) {
                    mainHandler.post(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(WeatherActivity.this, "City \"" + city + "\" not found. Please verify spelling.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                // Step 2: Fetch real-time weather & 5-day forecast
                String forecastUrl = String.format(Locale.US,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=6",
                        lat, lon);

                String forecastResponse = httpGet(forecastUrl);
                if (forecastResponse == null) {
                    mainHandler.post(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(WeatherActivity.this, "Failed to load weather data from server.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                JSONObject forecastJson = new JSONObject(forecastResponse);
                JSONObject current = forecastJson.getJSONObject("current");

                WeatherData data = new WeatherData();
                data.cityName = resolvedName;
                data.region = !TextUtils.isEmpty(resolvedRegion) ? resolvedRegion : "Forecast for " + resolvedName;
                data.currentTemp = current.getDouble("temperature_2m");
                data.feelsLike = current.optDouble("apparent_temperature", data.currentTemp);
                data.humidity = current.optInt("relative_humidity_2m", 0);
                data.windSpeed = current.optDouble("wind_speed_10m", 0.0);
                data.weatherCode = current.optInt("weather_code", 0);

                if (forecastJson.has("daily")) {
                    JSONObject daily = forecastJson.getJSONObject("daily");
                    JSONArray timeArr = daily.getJSONArray("time");
                    JSONArray codeArr = daily.getJSONArray("weather_code");
                    JSONArray maxArr = daily.getJSONArray("temperature_2m_max");
                    JSONArray minArr = daily.getJSONArray("temperature_2m_min");

                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat dayFormatter = new SimpleDateFormat("EEE", Locale.getDefault());

                    for (int i = 0; i < timeArr.length() && data.dailyForecasts.size() < 5; i++) {
                        DailyForecast df = new DailyForecast();
                        String dateStr = timeArr.getString(i);
                        try {
                            Date d = parser.parse(dateStr);
                            df.dayName = (d != null) ? dayFormatter.format(d) : dateStr;
                        } catch (Exception ex) {
                            df.dayName = dateStr;
                        }
                        df.weatherCode = codeArr.optInt(i, 0);
                        df.maxTemp = maxArr.optDouble(i, data.currentTemp);
                        df.minTemp = minArr.optDouble(i, data.currentTemp);
                        data.dailyForecasts.add(df);
                    }
                }

                mainHandler.post(() -> updateUI(data));

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(WeatherActivity.this, "Error fetching weather: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateUI(WeatherData data) {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (tvWeatherCityName != null) {
            tvWeatherCityName.setText(data.cityName);
        }
        if (tvWeatherRegion != null) {
            tvWeatherRegion.setText(data.region);
        }
        if (tvWeatherTemp != null) {
            tvWeatherTemp.setText(Math.round(data.currentTemp) + "°C");
        }
        if (tvWeatherConditionText != null) {
            tvWeatherConditionText.setText(getWeatherConditionDescription(data.weatherCode));
        }
        if (ivWeatherCondition != null) {
            ivWeatherCondition.setImageResource(getWeatherIconRes(data.weatherCode));
        }
        if (tvWeatherFeelsLike != null) {
            tvWeatherFeelsLike.setText(Math.round(data.feelsLike) + "°C");
        }
        if (tvWeatherHumidity != null) {
            tvWeatherHumidity.setText(data.humidity + "%");
        }
        if (tvWeatherWind != null) {
            tvWeatherWind.setText(Math.round(data.windSpeed) + " km/h");
        }

        // Update 5-day forecast
        for (int i = 0; i < 5 && i < data.dailyForecasts.size(); i++) {
            DailyForecast df = data.dailyForecasts.get(i);
            if (tvForecastDays[i] != null) {
                tvForecastDays[i].setText(df.dayName);
            }
            if (ivForecastIcons[i] != null) {
                ivForecastIcons[i].setImageResource(getWeatherIconRes(df.weatherCode));
            }
            if (tvForecastMaxs[i] != null) {
                tvForecastMaxs[i].setText(Math.round(df.maxTemp) + "°");
            }
            if (tvForecastMins[i] != null) {
                tvForecastMins[i].setText(Math.round(df.minTemp) + "°");
            }
        }

        String weatherInfo = "Weather in " + data.cityName + ":\n" +
                "Temperature: " + Math.round(data.currentTemp) + "°C, " + getWeatherConditionDescription(data.weatherCode) + "\n" +
                "Feels Like: " + Math.round(data.feelsLike) + "°C\n" +
                "Humidity: " + data.humidity + "%\n" +
                "Wind: " + Math.round(data.windSpeed) + " km/h";

        if (tvWeatherResult != null) {
            tvWeatherResult.setText(weatherInfo);
        }

        Toast.makeText(this, "Updated weather for " + data.cityName, Toast.LENGTH_SHORT).show();
    }

    private String getWeatherConditionDescription(int code) {
        if (code == 0) return "Clear Sky";
        if (code == 1) return "Mainly Clear";
        if (code == 2) return "Partly Cloudy";
        if (code == 3) return "Overcast";
        if (code >= 45 && code <= 48) return "Foggy";
        if (code >= 51 && code <= 55) return "Drizzle";
        if (code >= 56 && code <= 57) return "Freezing Drizzle";
        if (code >= 61 && code <= 65) return "Rain";
        if (code >= 66 && code <= 67) return "Freezing Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain Showers";
        if (code >= 85 && code <= 86) return "Snow Showers";
        if (code >= 95 && code <= 99) return "Thunderstorm";
        return "Pleasant";
    }

    private int getWeatherIconRes(int code) {
        if (code == 0) {
            return R.drawable.ic_sun;
        } else if (code == 1 || code == 2) {
            return R.drawable.ic_weather_cloud_sun;
        } else if (code == 3 || (code >= 45 && code <= 48)) {
            return R.drawable.ic_cloud_sun;
        } else {
            // Rain, showers, storms, snow
            return R.drawable.ic_rain;
        }
    }

    private String httpGet(String urlString) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "Voyageur-WeatherApp/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                InputStream in = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignored) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            android.net.Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null && (
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                );
            }
        }
        return false;
    }
}
