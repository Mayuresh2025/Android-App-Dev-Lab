package com.voyageur.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MLRecommendationEngine {

    public static class Recommendation {
        private String destination;
        private String country;
        private int matchScore; // Percentage 0-100%
        private String highlight;
        private String category;
        private String estimatedCost;

        public Recommendation(String destination, String country, int matchScore, String highlight, String category, String estimatedCost) {
            this.destination = destination;
            this.country = country;
            this.matchScore = matchScore;
            this.highlight = highlight;
            this.category = category;
            this.estimatedCost = estimatedCost;
        }

        public String getDestination() {
            return destination;
        }

        public String getCountry() {
            return country;
        }

        public int getMatchScore() {
            return matchScore;
        }

        public String getHighlight() {
            return highlight;
        }

        public String getCategory() {
            return category;
        }

        public String getEstimatedCost() {
            return estimatedCost;
        }
    }

    public static List<Recommendation> getRecommendations(String preferenceTag, String budgetTier) {
        List<Recommendation> all = new ArrayList<>();

        all.add(new Recommendation("Paris", "France", 95, "Eiffel Tower, Louvre Museum & Gourmet Bistro Dining", "Culture & Romance", "€180/day"));
        all.add(new Recommendation("Tokyo", "Japan", 92, "Shinjuku Lights, Historic Temples & TeamLab Art", "Culture & Modern Tech", "$160/day"));
        all.add(new Recommendation("Bali", "Indonesia", 90, "Ubud Rice Terraces, Beach Resorts & Sunset Temples", "Relaxation & Nature", "$75/day"));
        all.add(new Recommendation("Swiss Alps", "Switzerland", 88, "Matterhorn Views, Glacier Express & Alpine Hiking", "Adventure & Nature", "$280/day"));
        all.add(new Recommendation("Jaipur", "India", 94, "Hawa Mahal, Amer Fort & Vibrant Bazaar Shopping", "Heritage & Culture", "₹4,500/day"));
        all.add(new Recommendation("Rome", "Italy", 89, "Colosseum, Roman Forum & Authentic Gelato Strolls", "History & Food", "€150/day"));
        all.add(new Recommendation("Dubai", "UAE", 86, "Burj Khalifa, Desert Safari & Luxury Malls", "Luxury & Architecture", "$300/day"));

        String prefLower = preferenceTag.toLowerCase(Locale.ROOT);
        String budgetLower = budgetTier.toLowerCase(Locale.ROOT);

        // Adjust ML score weights based on inputs
        for (Recommendation rec : all) {
            int score = rec.matchScore;
            if (rec.category.toLowerCase(Locale.ROOT).contains(prefLower) || rec.highlight.toLowerCase(Locale.ROOT).contains(prefLower)) {
                score += 5;
            }
            if (budgetLower.contains("budget") && (rec.estimatedCost.contains("$75") || rec.estimatedCost.contains("₹4,500"))) {
                score += 8;
            } else if (budgetLower.contains("luxury") && (rec.estimatedCost.contains("$280") || rec.estimatedCost.contains("$300"))) {
                score += 8;
            }
            rec.matchScore = Math.min(99, Math.max(60, score));
        }

        Collections.sort(all, (r1, r2) -> Integer.compare(r2.getMatchScore(), r1.getMatchScore()));
        return all;
    }
}
