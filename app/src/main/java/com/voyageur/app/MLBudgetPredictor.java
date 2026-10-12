package com.voyageur.app;

import java.util.Locale;

public class MLBudgetPredictor {

    public static class PredictionResult {
        private final double totalEstimatedCostUsd;
        private final double flightCostUsd;
        private final double hotelCostUsd;
        private final double foodCostUsd;
        private final double activityCostUsd;
        private final String summary;

        public PredictionResult(double totalEstimatedCostUsd, double flightCostUsd, double hotelCostUsd, double foodCostUsd, double activityCostUsd, String summary) {
            this.totalEstimatedCostUsd = totalEstimatedCostUsd;
            this.flightCostUsd = flightCostUsd;
            this.hotelCostUsd = hotelCostUsd;
            this.foodCostUsd = foodCostUsd;
            this.activityCostUsd = activityCostUsd;
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

        public String getSummary() {
            return summary;
        }
    }

    public static PredictionResult predictBudget(String destination, int days, String travelStyle) {
        String destLower = destination.toLowerCase(Locale.ROOT);
        String styleLower = travelStyle.toLowerCase(Locale.ROOT);

        double baseDailyHotel = 80;
        double baseDailyFood = 40;
        double baseDailyActivity = 30;
        double baseFlight = 400;

        if (destLower.contains("paris") || destLower.contains("london") || destLower.contains("new york") || destLower.contains("tokyo") || destLower.contains("switzerland")) {
            baseDailyHotel = 150;
            baseDailyFood = 70;
            baseDailyActivity = 50;
            baseFlight = 800;
        } else if (destLower.contains("bali") || destLower.contains("goa") || destLower.contains("jaipur") || destLower.contains("thailand")) {
            baseDailyHotel = 45;
            baseDailyFood = 20;
            baseDailyActivity = 15;
            baseFlight = 350;
        }

        double multiplier = 1.0;
        if (styleLower.contains("luxury")) {
            multiplier = 2.2;
        } else if (styleLower.contains("budget")) {
            multiplier = 0.6;
        }

        double flight = baseFlight * (styleLower.contains("luxury") ? 1.8 : (styleLower.contains("budget") ? 0.8 : 1.0));
        double hotel = baseDailyHotel * days * multiplier;
        double food = baseDailyFood * days * multiplier;
        double activities = baseDailyActivity * days * multiplier;
        double total = flight + hotel + food + activities;

        String summary = String.format(Locale.US,
                "🤖 ML Cost Prediction for %d days in %s (%s):\n\n" +
                        "• Flights & Transport: $%.0f\n" +
                        "• Accommodation: $%.0f\n" +
                        "• Dining & Food: $%.0f\n" +
                        "• Activities & Entry Fees: $%.0f\n" +
                        "───────────────\n" +
                        "💰 Total Predicted Cost: $%.0f USD",
                days, destination, travelStyle, flight, hotel, food, activities, total);

        return new PredictionResult(total, flight, hotel, food, activities, summary);
    }
}
