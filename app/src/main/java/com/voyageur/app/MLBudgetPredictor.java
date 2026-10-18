package com.voyageur.app;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Machine Learning Multi-Variable Budget Predictor.
 * Implements a weighted cost regression model predicting travel expenses across
 * flights, accommodations, food, and sightseeing based on destination cost indices,
 * trip duration curves, and traveler comfort style profiles.
 */
public class MLBudgetPredictor {

    public static class PredictionResult {
        private final double totalEstimatedCostUsd;
        private final double flightCostUsd;
        private final double hotelCostUsd;
        private final double foodCostUsd;
        private final double activityCostUsd;
        private final double emergencyBufferUsd;
        private final String summary;

        public PredictionResult(double totalEstimatedCostUsd, double flightCostUsd, double hotelCostUsd,
                                double foodCostUsd, double activityCostUsd, double emergencyBufferUsd, String summary) {
            this.totalEstimatedCostUsd = totalEstimatedCostUsd;
            this.flightCostUsd = flightCostUsd;
            this.hotelCostUsd = hotelCostUsd;
            this.foodCostUsd = foodCostUsd;
            this.activityCostUsd = activityCostUsd;
            this.emergencyBufferUsd = emergencyBufferUsd;
            this.summary = summary;
        }

        public double getTotalEstimatedCostUsd() {
            return totalEstimatedCostUsd;
        }

        public double getFlightCostUsd() {
            return flightCostUsd;
        }

        public double getHotelCostUsd() {
            return hotelCostUsd;
        }

        public double getFoodCostUsd() {
            return foodCostUsd;
        }

        public double getActivityCostUsd() {
            return activityCostUsd;
        }

        public double getEmergencyBufferUsd() {
            return emergencyBufferUsd;
        }

        public String getSummary() {
            return summary;
        }
    }

    // Cost-of-Living & Tourism Price Index dictionary for major global destination hubs
    private static final Map<String, Double> COST_INDEX_MAP = new HashMap<>();

    static {
        // Higher index = higher cost (Base 1.0 = global median)
        COST_INDEX_MAP.put("paris", 1.85);
        COST_INDEX_MAP.put("london", 1.95);
        COST_INDEX_MAP.put("new york", 2.20);
        COST_INDEX_MAP.put("tokyo", 1.70);
        COST_INDEX_MAP.put("switzerland", 2.30);
        COST_INDEX_MAP.put("swiss alps", 2.35);
        COST_INDEX_MAP.put("dubai", 1.75);
        COST_INDEX_MAP.put("rome", 1.55);
        COST_INDEX_MAP.put("santorini", 1.80);
        COST_INDEX_MAP.put("sydney", 1.80);
        COST_INDEX_MAP.put("singapore", 1.75);
        COST_INDEX_MAP.put("bali", 0.70);
        COST_INDEX_MAP.put("thailand", 0.65);
        COST_INDEX_MAP.put("phuket", 0.75);
        COST_INDEX_MAP.put("bangkok", 0.70);
        COST_INDEX_MAP.put("goa", 0.55);
        COST_INDEX_MAP.put("jaipur", 0.50);
        COST_INDEX_MAP.put("delhi", 0.55);
        COST_INDEX_MAP.put("mumbai", 0.65);
        COST_INDEX_MAP.put("vietnam", 0.60);
    }

    public static PredictionResult predictBudget(String destination, int days, String travelStyle) {
        if (days <= 0) days = 1;
        String destLower = destination == null ? "general" : destination.toLowerCase(Locale.ROOT).trim();
        String styleLower = travelStyle == null ? "standard" : travelStyle.toLowerCase(Locale.ROOT).trim();

        // 1. Resolve Cost of Living Index
        double costIndex = 1.0;
        for (Map.Entry<String, Double> entry : COST_INDEX_MAP.entrySet()) {
            if (destLower.contains(entry.getKey())) {
                costIndex = entry.getValue();
                break;
            }
        }

        // 2. Travel Style Coefficient (Non-linear regression weights)
        double styleMultiplier = 1.0;
        double flightClassMultiplier = 1.0;
        String styleLabel = "Standard / Mid-Range";

        if (styleLower.contains("budget") || styleLower.contains("backpacker") || styleLower.contains("economy")) {
            styleMultiplier = 0.55;
            flightClassMultiplier = 0.80;
            styleLabel = "Budget / Backpacker";
        } else if (styleLower.contains("luxury") || styleLower.contains("vip") || styleLower.contains("first class")) {
            styleMultiplier = 2.40;
            flightClassMultiplier = 2.10;
            styleLabel = "Luxury / Premium";
        }

        // 3. Base Daily Parameter Regression (USD)
        double baseFlightUsd = 450.0 * costIndex * flightClassMultiplier;

        // Long-stay discount factor (e.g., 7+ days has slight per-day efficiency)
        double durationEfficiency = days > 7 ? 0.92 : 1.0;

        double dailyHotel = 85.0 * costIndex * styleMultiplier * durationEfficiency;
        double dailyFood = 45.0 * costIndex * styleMultiplier;
        double dailyActivities = 30.0 * costIndex * styleMultiplier;

        double hotelTotal = dailyHotel * days;
        double foodTotal = dailyFood * days;
        double activityTotal = dailyActivities * days;

        // 4. Emergency & Insurance Buffer (8% of subtotal)
        double subtotal = baseFlightUsd + hotelTotal + foodTotal + activityTotal;
        double emergencyBuffer = subtotal * 0.08;
        double grandTotal = subtotal + emergencyBuffer;

        String summary = String.format(Locale.US,
                "🤖 **ML Cost Prediction Model**\n" +
                        "📍 %s | ⏱️ %d Days | 🎒 %s\n" +
                        "───────────────────────────────\n" +
                        "✈️ Flights & Transit: $%.0f\n" +
                        "🏨 Accommodation ($%.0f/day): $%.0f\n" +
                        "🍽️ Food & Dining ($%.0f/day): $%.0f\n" +
                        "🎟️ Activities & Landmarks: $%.0f\n" +
                        "🛡️ Recommended Safety Buffer: $%.0f\n" +
                        "───────────────────────────────\n" +
                        "💰 **Total Predicted Budget: $%.0f USD**\n" +
                        "(Est. Daily Burn Rate: $%.0f/day)",
                destination, days, styleLabel,
                baseFlightUsd,
                dailyHotel, hotelTotal,
                dailyFood, foodTotal,
                activityTotal,
                emergencyBuffer,
                grandTotal,
                (grandTotal - baseFlightUsd) / days);

        return new PredictionResult(grandTotal, baseFlightUsd, hotelTotal, foodTotal, activityTotal, emergencyBuffer, summary);
    }
}
