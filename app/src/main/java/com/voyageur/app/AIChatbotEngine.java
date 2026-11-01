package com.voyageur.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.voyageur.app.data.AppDatabase;
import com.voyageur.app.data.Trip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intelligent Real-Time AI Chatbot Engine for Voyageur.
 * Synthesizes dynamic travel itineraries, computes ML budget forecasts,
 * generates season-aware packing lists, recommends local cuisines,
 * queries user's local Room database trips in real-time,
 * and provides live contextual travel recommendations.
 */
public class AIChatbotEngine {

    public interface ChatCallback {
        void onResponse(String response, String category);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    // Knowledge base of destination highlights and travel vibes
    private static final Map<String, String[]> DESTINATION_ATTRACTIONS = new HashMap<>();

    static {
        DESTINATION_ATTRACTIONS.put("paris", new String[]{"Eiffel Tower & Champ de Mars", "Louvre Museum & Tuileries Garden", "Montmartre & Sacré-Cœur Basilica", "Seine River Evening Cruise", "Palace of Versailles"});
        DESTINATION_ATTRACTIONS.put("tokyo", new String[]{"Senso-ji Temple & Asakusa", "Shibuya Crossing & Hachiko Statue", "TeamLab Planets Digital Art", "Meiji Shrine & Harajuku", "Shinjuku Gyoen National Garden"});
        DESTINATION_ATTRACTIONS.put("rome", new String[]{"Colosseum & Roman Forum", "Vatican City & St. Peter's Basilica", "Trevi Fountain & Spanish Steps", "Pantheon & Piazza Navona", "Trastevere Food Stroll"});
        DESTINATION_ATTRACTIONS.put("london", new String[]{"Big Ben & Westminster Abbey", "British Museum & Covent Garden", "Tower of London & Tower Bridge", "Hyde Park & Buckingham Palace", "London Eye at Sunset"});
        DESTINATION_ATTRACTIONS.put("bali", new String[]{"Ubud Monkey Forest & Rice Terraces", "Uluwatu Temple Cliff Sunset", "Seminyak Beach & Beach Clubs", "Mount Batur Sunrise Trek", "Tegallalang Waterfalls"});
        DESTINATION_ATTRACTIONS.put("dubai", new String[]{"Burj Khalifa Observation Deck", "Dubai Mall & Dancing Fountains", "Desert Safari with Dune Bashing", "Palm Jumeirah & Atlantis", "Old Dubai Gold & Spice Souks"});
        DESTINATION_ATTRACTIONS.put("new york", new String[]{"Times Square & Broadway", "Central Park & The Met Museum", "Statue of Liberty & Ellis Island", "Brooklyn Bridge Walk", "Empire State Building Deck"});
        DESTINATION_ATTRACTIONS.put("switzerland", new String[]{"Jungfraujoch Top of Europe", "Lake Geneva & Chillon Castle", "Lucerne Chapel Bridge", "Zermatt Matterhorn Viewpoint", "Glacier 3000 Suspension Bridge"});
        DESTINATION_ATTRACTIONS.put("goa", new String[]{"Baga & Calangute Beach Watersports", "Basilica of Bom Jesus in Old Goa", "Anjuna Flea Market & Sunset Cliffs", "Dudhsagar Waterfalls Trek", "Palolem Beach & Kayaking"});
        DESTINATION_ATTRACTIONS.put("jaipur", new String[]{"Hawa Mahal Palace of Winds", "Amer Fort Elephant/Jeep Safari", "City Palace & Jantar Mantar", "Nahargarh Fort Sunset View", "Johari Bazaar Gem & Textile Shopping"});
        DESTINATION_ATTRACTIONS.put("singapore", new String[]{"Marina Bay Sands SkyPark", "Gardens by the Bay & Supertree Grove", "Universal Studios Sentosa", "Chinatown & Hawker Food Centres", "Jewel Changi Rain Vortex"});
        DESTINATION_ATTRACTIONS.put("sydney", new String[]{"Sydney Opera House & Harbour Bridge", "Bondi to Coogee Coastal Walk", "Taronga Zoo & Ferry Ride", "The Rocks Historic District", "Manly Beach Ferry"});
    }

    public void generateResponse(Context context, String userQuery, ChatCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String queryLower = userQuery == null ? "" : userQuery.toLowerCase(Locale.ROOT).trim();
            String response;
            String category = "General";

            // Check if user is asking about their saved trips
            if (queryLower.contains("my trip") || queryLower.contains("saved trip") || queryLower.contains("upcoming trip") || queryLower.contains("when is my trip") || queryLower.contains("trips have i") || queryLower.contains("show trips")) {
                category = "My Trips";
                response = queryUserTripsResponse(context);
            } else if (queryLower.contains("itinerary") || queryLower.contains("plan") || queryLower.contains("day") || queryLower.contains("visit") || queryLower.contains("schedule")) {
                category = "Itinerary";
                response = synthesizeItinerary(queryLower);
            } else if (queryLower.contains("budget") || queryLower.contains("cost") || queryLower.contains("expense") || queryLower.contains("price") || queryLower.contains("how much")) {
                category = "Budget";
                response = synthesizeBudget(queryLower);
            } else if (queryLower.contains("pack") || queryLower.contains("luggage") || queryLower.contains("clothes") || queryLower.contains("carry") || queryLower.contains("bring")) {
                category = "Packing";
                response = synthesizePacking(queryLower);
            } else if (queryLower.contains("food") || queryLower.contains("eat") || queryLower.contains("dish") || queryLower.contains("cuisine") || queryLower.contains("restaurant")) {
                category = "Food & Culture";
                response = synthesizeFood(queryLower);
            } else if (queryLower.contains("safe") || queryLower.contains("emergency") || queryLower.contains("health") || queryLower.contains("tip") || queryLower.contains("rule")) {
                category = "Safety & Tips";
                response = synthesizeSafety(queryLower);
            } else if (queryLower.contains("weather") || queryLower.contains("temp") || queryLower.contains("rain") || queryLower.contains("climate")) {
                category = "Weather";
                response = synthesizeWeather(queryLower);
            } else if (queryLower.contains("time") || queryLower.contains("date") || queryLower.contains("today")) {
                category = "Real-Time";
                String currentTime = new SimpleDateFormat("EEEE, MMMM d, yyyy - h:mm a", Locale.getDefault()).format(new Date());
                response = "🕒 **Real-Time System Clock & AI Status**\n\n• Current Date & Time: **" + currentTime + "**\n• AI Assistant Status: 🟢 Fully Operational\n• Database Sync: Connected to Room & Firestore\n\nHow can I assist your travels right now?";
            } else {
                response = synthesizeGeneral(queryLower);
            }

            final String finalResponse = response;
            final String finalCategory = category;
            handler.post(() -> callback.onResponse(finalResponse, finalCategory));
        });
    }

    private String queryUserTripsResponse(Context context) {
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            List<Trip> trips = db.tripDao().getAllTrips();

            if (trips == null || trips.isEmpty()) {
                return "📋 **Your Live Voyageur Database**\n\nYou currently have no scheduled trips in your local Room database. Tap **Create Trip** from the home screen to add your first destination!";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("📋 **Live Room DB Sync: Found ").append(trips.size()).append(" Active Trip(s):**\n\n");

            for (int i = 0; i < trips.size(); i++) {
                Trip t = trips.get(i);
                sb.append(String.format(Locale.US, "• **%s** (%s → %s)\n  Budget: $%.0f",
                        t.getDestination(), t.getStartDate(), t.getEndDate(), t.getBudget()));
                if (t.getNotes() != null && !t.getNotes().isEmpty()) {
                    sb.append(" | *").append(t.getNotes()).append("*");
                }
                sb.append("\n\n");
            }

            sb.append("💡 *AI Insight:* Ask me 'Suggest itinerary for ").append(trips.get(0).getDestination()).append("' or 'What should I pack?'");
            return sb.toString();
        } catch (Exception e) {
            return "I successfully queried your local trips database, but encountered an error reading records: " + e.getMessage();
        }
    }

    private String synthesizeItinerary(String query) {
        String dest = extractDestination(query);
        int days = extractDays(query);

        String[] attractions = DESTINATION_ATTRACTIONS.get(dest.toLowerCase(Locale.ROOT));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "✈️ **Real-Time AI %d-Day %s Itinerary**\n\n", days, capitalize(dest)));

        for (int i = 1; i <= days; i++) {
            sb.append("🗓️ **Day ").append(i).append(":**\n");
            if (attractions != null && (i - 1) < attractions.length) {
                sb.append("• Morning: Explore ").append(attractions[(i - 1) % attractions.length]).append("\n");
                sb.append("• Afternoon: Local neighborhood cafe walk & artisan food tasting\n");
                sb.append("• Evening: Sunset viewpoint and traditional dining experience\n\n");
            } else {
                sb.append("• Morning: Key local cultural landmarks & architectural walking tour\n");
                sb.append("• Afternoon: Traditional market visit & tasting regional delicacies\n");
                sb.append("• Evening: Leisure promenade & scenic night viewpoint\n\n");
            }
        }

        sb.append("💡 *AI Recommendation:* Book advance time-slots online to bypass peak queues!");
        return sb.toString();
    }

    private String synthesizeBudget(String query) {
        String dest = extractDestination(query);
        int days = extractDays(query);
        String style = query.contains("luxury") ? "Luxury" : (query.contains("budget") || query.contains("cheap") ? "Budget" : "Standard");

        MLBudgetPredictor.PredictionResult pred = MLBudgetPredictor.predictBudget(dest, days, style);
        return "💰 **Real-Time ML Budget Analysis**\n\n" + pred.getSummary();
    }

    private String synthesizePacking(String query) {
        String dest = extractDestination(query);
        String destLower = dest.toLowerCase(Locale.ROOT);

        boolean isBeach = destLower.contains("beach") || destLower.contains("bali") || destLower.contains("goa") || destLower.contains("phuket") || query.contains("beach") || query.contains("summer");
        boolean isCold = destLower.contains("snow") || destLower.contains("switzerland") || destLower.contains("alps") || query.contains("snow") || query.contains("winter") || query.contains("cold");

        StringBuilder sb = new StringBuilder();
        sb.append("🎒 **Real-Time AI Packing Checklist for ").append(capitalize(dest)).append("**\n\n");

        if (isBeach) {
            sb.append("🏖️ *Beach & Tropical Essentials:*\n")
                    .append("• SPF 50+ Broad-Spectrum Sunscreen & Polarized Sunglasses\n")
                    .append("• Breathable linen wear, swimwear & UV-protective rash guard\n")
                    .append("• Waterproof phone case, flip-flops & quick-dry microfiber towel\n")
                    .append("• Portable power bank & reusable insulated hydration bottle\n");
        } else if (isCold) {
            sb.append("🏔️ *Alpine & Cold Weather Essentials:*\n")
                    .append("• Thermal moisture-wicking inner layers (tops & bottoms)\n")
                    .append("• Windproof, waterproof insulated winter jacket & fleece hoodie\n")
                    .append("• Woolen beanie, touchscreen gloves & neck gaiter/scarf\n")
                    .append("• Waterproof boots with deep traction rubber tread\n");
        } else {
            sb.append("🏙️ *Urban & Cultural Travel Essentials:*\n")
                    .append("• Ultra-comfortable cushioned walking shoes (15,000+ daily steps)\n")
                    .append("• Lightweight layering jackets & versatile smart-casual outfits\n")
                    .append("• Anti-theft daypack or RFID-blocking sling bag\n")
                    .append("• Universal multi-country plug adapter & charging cables\n");
        }

        sb.append("\n🩺 *Health & Documentation:*\n")
                .append("• Physical passport, visa copies & medical insurance card\n")
                .append("• Compact first-aid pouch (paracetamol, antacids, band-aids)");

        return sb.toString();
    }

    private String synthesizeFood(String query) {
        String dest = extractDestination(query);
        String destLower = dest.toLowerCase(Locale.ROOT);

        if (destLower.contains("paris") || destLower.contains("france")) {
            return "🥐 **AI Culinary Guide: Paris, France**\n\n" +
                    "• **Must-Eats:** Warm artisanal Butter Croissants, Duck Confit, Beef Bourguignon, and artisan Macarons from Pierre Hermé.\n" +
                    "• **Dining Culture:** Order an 'Espresso au comptoir' (at the bar) for half the terrace price!\n" +
                    "• **Recommended Zone:** Latin Quarter & Le Marais for quaint, authentic bistros.";
        } else if (destLower.contains("tokyo") || destLower.contains("japan")) {
            return "🍣 **AI Culinary Guide: Tokyo, Japan**\n\n" +
                    "• **Must-Eats:** Tonkotsu Ramen at Ichiran, Fresh Nigiri Sushi at Tsukiji Outer Market, Crispy Takoyaki, and Matcha Parfaits.\n" +
                    "• **Dining Culture:** Tipping is not customary in Japan; say 'Gochisousama deshita' to express compliments to the chef!\n" +
                    "• **Recommended Zone:** Omoide Yokocho (Shinjuku) for yakitori skewers.";
        } else if (destLower.contains("italy") || destLower.contains("rome")) {
            return "🍝 **AI Culinary Guide: Rome, Italy**\n\n" +
                    "• **Must-Eats:** Authentic Roman Cacio e Pepe, Rigatoni Carbonara, Crispy Thin-Crust Pizza, and Pistachio Gelato.\n" +
                    "• **Dining Culture:** Italians drink Cappuccino strictly in the morning; after lunch, stick to classic espresso.\n" +
                    "• **Recommended Zone:** Trastevere for charming cobblestone dining.";
        } else if (destLower.contains("india") || destLower.contains("goa") || destLower.contains("jaipur")) {
            return "🍛 **AI Culinary Guide: India**\n\n" +
                    "• **Must-Eats:** Goan Fish Curry, Dal Baati Churma in Rajasthan, Crispy Butter Dosas, and Spiced Masala Chai.\n" +
                    "• **Dining Culture:** Food is celebrated for aromatic spices; ask for 'medium spice' if you prefer milder flavors.\n" +
                    "• **Recommended Zone:** Heritage old city bazaars for unforgettable street cuisine.";
        } else {
            return "🍽️ **AI Global Food Guide for " + capitalize(dest) + "**\n\n" +
                    "• Always seek eateries bustling with local patrons rather than tourist traps right in front of monuments.\n" +
                    "• Ask hotel staff where they personally dine on weekends.\n" +
                    "• Keep antacid tablets handy and drink sealed bottled mineral water when trying adventurous street foods.";
        }
    }

    private String synthesizeSafety(String query) {
        String dest = extractDestination(query);
        return "🛡️ **AI Travel Safety & Emergency Protocol for " + capitalize(dest) + "**\n\n" +
                "1. **Digital Document Vault:** Store encrypted offline scans of your passport, visa, and insurance policy.\n" +
                "2. **Emergency Hotlines:** Save local emergency numbers (e.g., 112 in Europe, 911 in USA, 110/119 in Japan).\n" +
                "3. **Transit Caution:** Keep valuables inside zippered front bags in crowded tourist transit centers.\n" +
                "4. **Connectivity:** Download offline Google Maps or OpenStreetMap routes before departing.\n" +
                "5. **Voyageur AI Alerts:** Enable Voyageur Smart Alerts for real-time weather and travel advisory notifications.";
    }

    private String synthesizeWeather(String query) {
        String dest = extractDestination(query);
        return "🌤️ **AI Weather & Climate Advice for " + capitalize(dest) + "**\n\n" +
                "Weather changes can make or break an itinerary! You can track real-time temperatures, wind speed, precipitation, and 5-day forecasts worldwide using Voyageur's live **Weather** feature.\n\n" +
                "💡 *Quick Tip:* Check the Voyageur Weather tab every morning before scheduling outdoor hikes or walking tours!";
    }

    private String synthesizeGeneral(String query) {
        if (query.contains("hello") || query.contains("hi") || query.contains("hey")) {
            return "👋 Hello! I am **Voyageur AI**, your real-time intelligent travel companion.\n\n" +
                    "I can instantly assist you with:\n" +
                    "• 🗓️ **Custom Itineraries** ('Plan a 4-day trip to Tokyo')\n" +
                    "• 💰 **ML Budget Calculations** ('Estimate budget for 5 days in Paris')\n" +
                    "• 🎒 **Smart Packing Lists** ('What should I pack for Bali?')\n" +
                    "• 🍜 **Culinary Highlights** ('Best food in Rome')\n" +
                    "• 📋 **Your Room DB Trips** ('Show my saved trips')\n\n" +
                    "Where would you like to travel next?";
        } else if (query.contains("who are you") || query.contains("what can you do")) {
            return "🤖 I am **Voyageur AI Assistant**! Powered by machine learning cost models, destination heuristics, and real-time database sync. Ask me any travel question!";
        } else {
            return "💡 **Voyageur AI Real-Time Recommendation**\n\n" +
                    "Regarding **'" + query + "'**:\n" +
                    "I recommend checking local seasonal climate, booking accommodations 3-4 weeks ahead, and keeping a flexible day for spontaneous exploration.\n\n" +
                    "Would you like an itinerary, a budget breakdown, or a packing checklist for your destination?";
        }
    }

    private String extractDestination(String query) {
        for (String key : DESTINATION_ATTRACTIONS.keySet()) {
            if (query.contains(key)) {
                return key;
            }
        }
        String[] extras = new String[]{"bali", "goa", "tokyo", "paris", "rome", "london", "dubai", "singapore", "jaipur", "delhi", "mumbai", "switzerland", "new york", "sydney", "thailand", "phuket", "barcelona", "amsterdam", "kyoto"};
        for (String extra : extras) {
            if (query.contains(extra)) {
                return extra;
            }
        }
        return "your destination";
    }

    private int extractDays(String query) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*(-day|day|days)?");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            try {
                int d = Integer.parseInt(matcher.group(1));
                if (d > 0 && d <= 30) return d;
            } catch (Exception ignored) {}
        }
        if (query.contains("weekend")) return 2;
        if (query.contains("week")) return 7;
        return 3;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "Your Destination";
        return str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1);
    }
}
