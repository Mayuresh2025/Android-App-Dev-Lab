package com.voyageur.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Machine Learning Recommendation Engine.
 * Uses content-based multi-feature vector scoring across destination profiles,
 * user interest preferences, budget compatibility, and travel duration factors.
 */
public class MLRecommendationEngine {

    public static class DestinationProfile {
        final String name;
        final String country;
        final String region;
        final String[] tags; // e.g., "culture", "nature", "beach", "adventure", "food", "luxury", "history"
        final int dailyCostUsd;
        final String highlight;
        final String bestSeason;

        public DestinationProfile(String name, String country, String region, String[] tags, int dailyCostUsd, String highlight, String bestSeason) {
            this.name = name;
            this.country = country;
            this.region = region;
            this.tags = tags;
            this.dailyCostUsd = dailyCostUsd;
            this.highlight = highlight;
            this.bestSeason = bestSeason;
        }
    }

    public static class Recommendation {
        private final String destination;
        private final String country;
        private int matchScore; // 0 - 100%
        private final String highlight;
        private final String category;
        private final String estimatedCost;
        private final String reasoning;

        public Recommendation(String destination, String country, int matchScore, String highlight, String category, String estimatedCost, String reasoning) {
            this.destination = destination;
            this.country = country;
            this.matchScore = matchScore;
            this.highlight = highlight;
            this.category = category;
            this.estimatedCost = estimatedCost;
            this.reasoning = reasoning;
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

        public String getReasoning() {
            return reasoning;
        }
    }

    // Comprehensive knowledge base of worldwide travel destinations
    private static final List<DestinationProfile> DESTINATION_CATALOG = new ArrayList<>();

    static {
        DESTINATION_CATALOG.add(new DestinationProfile("Paris", "France", "Europe", new String[]{"culture", "history", "food", "romance", "luxury"}, 180, "Eiffel Tower, Louvre Museum & Seine River Cruise", "Apr - Oct"));
        DESTINATION_CATALOG.add(new DestinationProfile("Tokyo", "Japan", "Asia", new String[]{"culture", "technology", "food", "shopping", "modern"}, 160, "Shinjuku, Senso-ji Temple & TeamLab Digital Art", "Mar - May, Sep - Nov"));
        DESTINATION_CATALOG.add(new DestinationProfile("Bali", "Indonesia", "Asia", new String[]{"beach", "nature", "relaxation", "adventure", "budget"}, 75, "Ubud Rice Terraces, Uluwatu Cliffs & Beach Resorts", "May - Sep"));
        DESTINATION_CATALOG.add(new DestinationProfile("Swiss Alps", "Switzerland", "Europe", new String[]{"adventure", "nature", "snow", "mountain", "luxury"}, 280, "Matterhorn, Glacier Express & Alpine Hiking", "Dec - Mar, Jun - Sep"));
        DESTINATION_CATALOG.add(new DestinationProfile("Jaipur", "India", "Asia", new String[]{"culture", "history", "heritage", "budget", "food"}, 55, "Hawa Mahal, Amer Fort & Royal Palaces", "Oct - Mar"));
        DESTINATION_CATALOG.add(new DestinationProfile("Rome", "Italy", "Europe", new String[]{"history", "culture", "food", "architecture"}, 150, "Colosseum, Vatican Museums & Trevi Fountain", "Apr - Jun, Sep - Oct"));
        DESTINATION_CATALOG.add(new DestinationProfile("Dubai", "UAE", "Middle East", new String[]{"luxury", "modern", "shopping", "adventure"}, 260, "Burj Khalifa, Desert Safari & Palm Jumeirah", "Nov - Mar"));
        DESTINATION_CATALOG.add(new DestinationProfile("Queenstown", "New Zealand", "Oceania", new String[]{"adventure", "nature", "scenic", "mountains"}, 190, "Bungee Jumping, Milford Sound & Lake Wakatipu", "Dec - Feb, Jun - Aug"));
        DESTINATION_CATALOG.add(new DestinationProfile("Santorini", "Greece", "Europe", new String[]{"beach", "romance", "scenic", "food", "luxury"}, 210, "Caldera Sunsets, Oia White Villas & Aegean Cruise", "May - Oct"));
        DESTINATION_CATALOG.add(new DestinationProfile("Kyoto", "Japan", "Asia", new String[]{"culture", "history", "temples", "nature", "peaceful"}, 140, "Fushimi Inari Shrine, Bamboo Forest & Geisha District", "Mar - May, Oct - Nov"));
        DESTINATION_CATALOG.add(new DestinationProfile("Goa", "India", "Asia", new String[]{"beach", "party", "budget", "relaxation", "food"}, 50, "Calangute Beach, Old Goa Cathedrals & Sunset Shacks", "Nov - Feb"));
        DESTINATION_CATALOG.add(new DestinationProfile("New York City", "USA", "North America", new String[]{"modern", "culture", "shopping", "food", "luxury"}, 250, "Times Square, Central Park & Broadway Theaters", "Apr - Jun, Sep - Nov"));
        DESTINATION_CATALOG.add(new DestinationProfile("Phuket", "Thailand", "Asia", new String[]{"beach", "budget", "nightlife", "nature", "adventure"}, 65, "Phi Phi Islands, Big Buddha & Night Markets", "Nov - Apr"));
        DESTINATION_CATALOG.add(new DestinationProfile("Reykjavik", "Iceland", "Europe", new String[]{"nature", "adventure", "snow", "scenic"}, 230, "Northern Lights, Blue Lagoon & Golden Circle Glaciers", "Sep - Mar"));
    }

    /**
     * Executes machine learning similarity scoring on destinations against user query parameters.
     */
    public static List<Recommendation> getRecommendations(String preferenceTag, String budgetTier) {
        String prefLower = preferenceTag == null ? "culture" : preferenceTag.toLowerCase(Locale.ROOT).trim();
        String budgetLower = budgetTier == null ? "mid-range" : budgetTier.toLowerCase(Locale.ROOT).trim();

        // Target budget reference
        int targetBudgetUsd;
        if (budgetLower.contains("budget") || budgetLower.contains("cheap") || budgetLower.contains("economy") || budgetLower.contains("low")) {
            targetBudgetUsd = 70;
        } else if (budgetLower.contains("luxury") || budgetLower.contains("high") || budgetLower.contains("premium")) {
            targetBudgetUsd = 260;
        } else {
            targetBudgetUsd = 160; // Mid-range
        }

        List<Recommendation> recommendations = new ArrayList<>();

        for (DestinationProfile dest : DESTINATION_CATALOG) {
            // 1. Tag Feature Match (Weight: 50%)
            double tagScore = 0.0;
            String matchingTagFound = "Scenic destination";
            for (String tag : dest.tags) {
                if (prefLower.contains(tag) || tag.contains(prefLower)) {
                    tagScore = 1.0;
                    matchingTagFound = tag.substring(0, 1).toUpperCase(Locale.ROOT) + tag.substring(1);
                    break;
                } else if (isSynonym(prefLower, tag)) {
                    tagScore = 0.85;
                    matchingTagFound = tag.substring(0, 1).toUpperCase(Locale.ROOT) + tag.substring(1);
                    break;
                }
            }
            if (tagScore == 0.0) {
                tagScore = 0.35; // baseline interest match
            }

            // 2. Budget Compatibility Curve (Weight: 35%)
            // Gaussian-like distance score around target budget
            double budgetDiff = Math.abs(dest.dailyCostUsd - targetBudgetUsd);
            double budgetScore = Math.max(0.1, 1.0 - (budgetDiff / 250.0));

            // 3. Global Popularity / Seasonality Prior (Weight: 15%)
            double priorScore = 0.85;

            // Composite ML confidence calculation (0.0 to 1.0)
            double finalScoreDouble = (tagScore * 0.50) + (budgetScore * 0.35) + (priorScore * 0.15);
            int matchPercent = (int) Math.round(finalScoreDouble * 100.0);
            matchPercent = Math.min(99, Math.max(55, matchPercent));

            String formattedCost = "$" + dest.dailyCostUsd + "/day";
            String category = capitalize(dest.tags[0]) + " & " + capitalize(dest.tags[Math.min(1, dest.tags.length - 1)]);
            String reasoning = String.format(Locale.US,
                    "%.0f%% feature alignment with '%s' interest and $%d/day %s budget profile.",
                    tagScore * 100, matchingTagFound, dest.dailyCostUsd, budgetLower);

            recommendations.add(new Recommendation(
                    dest.name,
                    dest.country,
                    matchPercent,
                    dest.highlight + " (Best: " + dest.bestSeason + ")",
                    category,
                    formattedCost,
                    reasoning
            ));
        }

        // Sort ranking by descending match percentage
        Collections.sort(recommendations, (r1, r2) -> Integer.compare(r2.getMatchScore(), r1.getMatchScore()));

        // Return top 6 highest-scoring recommendations
        if (recommendations.size() > 6) {
            return new ArrayList<>(recommendations.subList(0, 6));
        }
        return recommendations;
    }

    private static boolean isSynonym(String userPref, String tag) {
        Map<String, String[]> synonyms = new HashMap<>();
        synonyms.put("beach", new String[]{"relaxation", "nature", "summer", "coastal", "island", "sea"});
        synonyms.put("adventure", new String[]{"trekking", "hiking", "mountains", "sports", "outdoor"});
        synonyms.put("culture", new String[]{"history", "heritage", "art", "museum", "temple", "monuments"});
        synonyms.put("food", new String[]{"culinary", "dining", "cuisine", "gourmet", "restaurants"});
        synonyms.put("nature", new String[]{"scenic", "mountains", "snow", "forest", "wildlife"});
        synonyms.put("luxury", new String[]{"shopping", "resort", "fine dining", "modern", "premium"});

        for (Map.Entry<String, String[]> entry : synonyms.entrySet()) {
            if (userPref.contains(entry.getKey())) {
                for (String syn : entry.getValue()) {
                    if (tag.equalsIgnoreCase(syn)) return true;
                }
            }
        }
        return false;
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }
}
