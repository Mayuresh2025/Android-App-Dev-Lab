package com.voyageur.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.voyageur.app.data.AppDatabase;
import com.voyageur.app.data.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Intelligent Travel Alert Engine.
 * Actively monitors local database trips, evaluates departure timelines,
 * checks budget risks against ML cost predictions, and generates proactive advisories.
 */
public class AISmartAlertEngine {

    public interface AlertCallback {
        void onAlertsGenerated(List<AISmartAlert> alerts);
    }

    public static void generateActiveAlertsAsync(Context context, AlertCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<AISmartAlert> alerts = new ArrayList<>();
            List<Trip> trips = new ArrayList<>();

            try {
                AppDatabase db = AppDatabase.getInstance(context);
                trips = db.tripDao().getAllTrips();
            } catch (Exception ignored) {}

            Date now = new Date();
            SimpleDateFormat[] dateFormats = new SimpleDateFormat[]{
                    new SimpleDateFormat("d/M/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            };

            if (trips != null && !trips.isEmpty()) {
                for (Trip trip : trips) {
                    if (trip.getDestination() == null) continue;

                    Date startDate = null;
                    Date endDate = null;
                    for (SimpleDateFormat sdf : dateFormats) {
                        try {
                            if (startDate == null && trip.getStartDate() != null) {
                                startDate = sdf.parse(trip.getStartDate());
                            }
                            if (endDate == null && trip.getEndDate() != null) {
                                endDate = sdf.parse(trip.getEndDate());
                            }
                        } catch (Exception ignored) {}
                    }

                    int durationDays = 5;
                    if (startDate != null && endDate != null && endDate.after(startDate)) {
                        long diff = endDate.getTime() - startDate.getTime();
                        durationDays = (int) Math.max(1, diff / (1000 * 60 * 60 * 24));
                    }

                    // 1. Departure Timeline & Countdown Alert
                    if (startDate != null) {
                        long diffMillis = startDate.getTime() - now.getTime();
                        long daysUntil = diffMillis / (1000 * 60 * 60 * 24);

                        if (daysUntil >= 0 && daysUntil <= 7) {
                            String daysText = daysUntil == 0 ? "TODAY" : "in " + daysUntil + " day" + (daysUntil > 1 ? "s" : "");
                            alerts.add(new AISmartAlert(
                                    "trip_dep_" + trip.getId(),
                                    "✈️ Departure Countdown: " + trip.getDestination(),
                                    "Your journey to " + trip.getDestination() + " starts " + daysText + "! Complete online airline check-in, download offline maps, and review your packing list.",
                                    daysUntil <= 2 ? "HIGH" : "MEDIUM",
                                    "Packing",
                                    "Review Itinerary"
                            ));
                        } else if (daysUntil < 0 && (endDate == null || now.before(endDate))) {
                            alerts.add(new AISmartAlert(
                                    "trip_active_" + trip.getId(),
                                    "🌟 Active Trip Ongoing: " + trip.getDestination(),
                                    "You are currently traveling in " + trip.getDestination() + "! Keep emergency contacts accessible and check today's live weather forecasts.",
                                    "HIGH",
                                    "General",
                                    "View Live Weather"
                            ));
                        }
                    }

                    // 2. Machine Learning Budget Risk Evaluation
                    if (trip.getBudget() > 0) {
                        MLBudgetPredictor.PredictionResult prediction =
                                MLBudgetPredictor.predictBudget(trip.getDestination(), durationDays, "Standard");

                        double predictedCost = prediction.getTotalEstimatedCostUsd();
                        if (trip.getBudget() < (predictedCost * 0.75)) {
                            double deficitPercent = Math.round(((predictedCost - trip.getBudget()) / predictedCost) * 100);
                            alerts.add(new AISmartAlert(
                                    "trip_budget_" + trip.getId(),
                                    "🚨 Budget Risk Alert: " + trip.getDestination(),
                                    String.format(Locale.US,
                                            "Your allocated budget ($%.0f) is %.0f%% lower than the AI-predicted average expense ($%.0f) for %d days in %s. Consider booking hostels or limiting dining out.",
                                            trip.getBudget(), deficitPercent, predictedCost, durationDays, trip.getDestination()),
                                    "HIGH",
                                    "Budget",
                                    "Adjust Budget"
                            ));
                        }
                    }

                    // 3. Destination Climate & Packing Recommendation Alert
                    String destLower = trip.getDestination().toLowerCase(Locale.ROOT);
                    if (destLower.contains("paris") || destLower.contains("london") || destLower.contains("tokyo") || destLower.contains("seattle")) {
                        alerts.add(new AISmartAlert(
                                "pack_rain_" + trip.getId(),
                                "🌧️ Weather Advisory for " + trip.getDestination(),
                                "Intermittent showers are common in " + trip.getDestination() + ". AI recommends packing a compact windproof umbrella and waterproof footwear.",
                                "MEDIUM",
                                "Weather",
                                "View Weather"
                        ));
                    } else if (destLower.contains("bali") || destLower.contains("goa") || destLower.contains("phuket") || destLower.contains("hawaii")) {
                        alerts.add(new AISmartAlert(
                                "pack_beach_" + trip.getId(),
                                "☀️ Tropical Travel Tip: " + trip.getDestination(),
                                "High UV index and tropical humidity expected. Pack SPF 50+ sunscreen, polarized sunglasses, and breathable linen outfits.",
                                "LOW",
                                "Packing",
                                "Packing Checklist"
                        ));
                    }
                }
            }

            // If user has 0 trips, provide proactive discovery alerts
            if (alerts.isEmpty()) {
                alerts.add(new AISmartAlert(
                        "promo_1",
                        "🗺️ AI Smart Travel Recommendation",
                        "You haven't scheduled any upcoming trips yet. Tap to discover AI-recommended travel destinations based on your budget & interests!",
                        "LOW",
                        "Recommendation",
                        "Plan a Trip"
                ));

                alerts.add(new AISmartAlert(
                        "promo_2",
                        "💡 Global Price Drop Advisory",
                        "Round-trip airfares to Southeast Asia & Europe are at an off-season discount. Great time to plan your next holiday with Voyageur!",
                        "LOW",
                        "Deal",
                        "Explore Flights"
                ));
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.onAlertsGenerated(alerts));
        });
    }
}
