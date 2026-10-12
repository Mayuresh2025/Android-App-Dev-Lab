package com.voyageur.app;

import android.os.Handler;
import android.os.Looper;

import java.util.Locale;

public class AIChatbotEngine {

    public interface ChatCallback {
        void onResponse(String response, String category);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    public void generateResponse(String userQuery, ChatCallback callback) {
        // Simulate network processing latency for realistic AI experience
        handler.postDelayed(() -> {
            String queryLower = userQuery.toLowerCase(Locale.ROOT).trim();
            String response;
            String category = "General";

            if (queryLower.contains("itinerary") || queryLower.contains("plan") || queryLower.contains("days")) {
                category = "Itinerary";
                response = generateItineraryResponse(queryLower);
            } else if (queryLower.contains("pack") || queryLower.contains("luggage") || queryLower.contains("clothes")) {
                category = "Packing";
                response = generatePackingResponse(queryLower);
            } else if (queryLower.contains("budget") || queryLower.contains("cost") || queryLower.contains("expense") || queryLower.contains("price")) {
                category = "Budget";
                response = generateBudgetResponse(queryLower);
            } else if (queryLower.contains("food") || queryLower.contains("eat") || queryLower.contains("restaurant") || queryLower.contains("dish")) {
                category = "Food & Culture";
                response = generateFoodResponse(queryLower);
            } else if (queryLower.contains("safe") || queryLower.contains("emergency") || queryLower.contains("health") || queryLower.contains("tip")) {
                category = "Safety & Tips";
                response = generateSafetyResponse(queryLower);
            } else if (queryLower.contains("weather") || queryLower.contains("temp") || queryLower.contains("climate")) {
                category = "Weather";
                response = generateWeatherResponse(queryLower);
            } else {
                response = generateGeneralResponse(queryLower);
            }

            callback.onResponse(response, category);
        }, 1000);
    }

    private String generateItineraryResponse(String query) {
        if (query.contains("paris")) {
            return "🇫🇷 **AI Custom Paris Itinerary**\n\n" +
                    "• **Day 1:** Morning at the Louvre Museum, afternoon stroll through Tuileries Garden, and evening Eiffel Tower light show.\n" +
                    "• **Day 2:** Explore Montmartre & Sacré-Cœur, indulge in French pastries, and take a Seine River evening cruise.\n" +
                    "• **Day 3:** Palace of Versailles day tour, followed by dining in the Latin Quarter.\n\n" +
                    "💡 *Pro-Tip:* Book museum tickets online in advance to skip 2-hour queues!";
        } else if (query.contains("tokyo")) {
            return "🇯🇵 **AI Custom Tokyo Itinerary**\n\n" +
                    "• **Day 1:** Senso-ji Temple in Asakusa, Tokyo Skytree, and evening neon walk in Akihabara.\n" +
                    "• **Day 2:** Meiji Shrine, Harajuku fashion district, and famous Shibuya Crossing.\n" +
                    "• **Day 3:** Digital art at TeamLab Planets and authentic sushi dining in Tsukiji Outer Market.\n\n" +
                    "💡 *Pro-Tip:* Get a Suica / Pasmo transit card for instant subway taps!";
        } else if (query.contains("goa") || query.contains("beach")) {
            return "🏖️ **AI Beach Getaway Itinerary**\n\n" +
                    "• **Day 1:** Baga & Calangute beach watersports, evening shacks & sunset views.\n" +
                    "• **Day 2:** Old Goa Heritage churches (Basilica of Bom Jesus) & Spice Plantation tour.\n" +
                    "• **Day 3:** South Goa serene beaches (Palolem, Agonda) and luxury dining.\n\n" +
                    "💡 *Pro-Tip:* Rent a scooter for easy coastal cruising!";
        } else {
            return "✈️ **AI Travel Recommendation**\n\n" +
                    "I can build a custom day-by-day itinerary for any destination! Specify duration & city (e.g., '3-day trip to Rome' or 'Weekend in New York') to get curated daily highlights, landmarks, and local recommendations.";
        }
    }

    private String generatePackingResponse(String query) {
        if (query.contains("beach") || query.contains("summer") || query.contains("goa") || query.contains("bali")) {
            return "🎒 **AI Smart Beach Packing Checklist**\n\n" +
                    "1. SPF 50+ Sunscreen & Polarized Sunglasses\n" +
                    "2. Lightweight cotton clothes & swimwear\n" +
                    "3. Waterproof phone pouch & portable power bank\n" +
                    "4. Flip-flops & breathable walking shoes\n" +
                    "5. Reusable water bottle & quick-dry towel";
        } else if (query.contains("cold") || query.contains("snow") || query.contains("winter") || query.contains("mountain")) {
            return "🏔️ **AI Winter / Cold Weather Packing Checklist**\n\n" +
                    "1. Thermal inner layers (top & bottom)\n" +
                    "2. Insulated windproof jacket & fleece sweater\n" +
                    "3. Woolen beanie, gloves, and warm scarf\n" +
                    "4. Waterproof anti-slip boots\n" +
                    "5. Moisturizer, lip balm & hand warmers";
        } else {
            return "🧳 **AI General Travel Packing Essentials**\n\n" +
                    "• Passport, Visa, ID & digital trip backups\n" +
                    "• Universal travel adapter & charging cables\n" +
                    "• Personal medical kit (painkillers, band-aids, motion sickness)\n" +
                    "• Comfortable walking shoes & versatile clothing layer\n" +
                    "• Compact travel umbrella / raincoat";
        }
    }

    private String generateBudgetResponse(String query) {
        if (query.contains("tokyo") || query.contains("japan")) {
            return "💰 **AI Budget Estimation: Tokyo, Japan**\n\n" +
                    "• **Budget Travel:** $60 - $90 / day (Capsule hotels, Ramen/Convenience stores, Subway pass)\n" +
                    "• **Mid-Range:** $150 - $250 / day (3-star hotel, local Izakaya dining, entrance passes)\n" +
                    "• **Luxury:** $450+ / day (5-star stay, Fine dining, private bullet train pass)\n\n" +
                    "📊 *AI Forecast:* Food is surprisingly affordable at $10-$15 per meal!";
        } else if (query.contains("paris") || query.contains("france") || query.contains("europe")) {
            return "💶 **AI Budget Estimation: Paris, France**\n\n" +
                    "• **Budget Travel:** €70 - €100 / day (Hostels/B&B, Boulangerie lunches, Metro tickets)\n" +
                    "• **Mid-Range:** €180 - €280 / day (Boutique hotel, Bistro dinners, Museum pass)\n" +
                    "• **Luxury:** €500+ / day (Luxury suites, Michelin dining, private tours)\n\n" +
                    "📊 *AI Tip:* Get a Paris Museum Pass to save up to 40% on entry fees.";
        } else {
            return "💳 **AI Budget Calculator**\n\n" +
                    "On average, travel expenses divide into:\n" +
                    "• 40% Accommodation\n" +
                    "• 25% Food & Dining\n" +
                    "• 20% Local Transport & Flights\n" +
                    "• 15% Sightseeing & Emergency Fund\n\n" +
                    "Tell me your destination & trip length to calculate an exact AI price prediction!";
        }
    }

    private String generateFoodResponse(String query) {
        return "🍜 **AI Culinary Recommendations**\n\n" +
                "• **Paris:** Fresh Croissants, Duck Confit, Escargot, and Macarons from Ladurée.\n" +
                "• **Tokyo:** Tonkotsu Ramen, Fresh Sashimi at Tsukiji, Takoyaki, and Matcha desserts.\n" +
                "• **Italy:** Authentic Neapolitan Pizza, Carbonara in Rome, and Gelato.\n" +
                "• **India:** Butter Chicken, South Indian Dosa, Street Food Chaat, and Masala Chai.\n\n" +
                "😋 Always look for spots crowded with locals for authentic taste & food safety!";
    }

    private String generateSafetyResponse(String query) {
        return "🛡️ **AI Travel Safety & Emergency Guidelines**\n\n" +
                "1. Keep offline digital copies of your passport, visa & travel insurance.\n" +
                "2. Store local emergency numbers and embassy contacts on your phone.\n" +
                "3. Avoid carrying large amounts of cash in crowded tourist areas.\n" +
                "4. Share your live location with family using Voyageur GPS features.\n" +
                "5. Drink bottled water in developing regions.";
    }

    private String generateWeatherResponse(String query) {
        return "🌤️ **AI Weather Advice**\n\n" +
                "Weather can drastically impact travel comfort! Check the Voyageur Weather tab for real-time live forecasts before heading out. Always pack a compact rain jacket and wear breathable footwear during summer months.";
    }

    private String generateGeneralResponse(String query) {
        if (query.contains("hello") || query.contains("hi") || query.contains("hey")) {
            return "Hello! 👋 I am **Voyageur AI Assistant**. How can I help you plan your next trip today?\n\n" +
                    "Try asking:\n" +
                    "• 'Suggest 3-day Paris itinerary'\n" +
                    "• 'What is the estimated budget for Tokyo?'\n" +
                    "• 'What should I pack for a beach vacation?'\n" +
                    "• 'Give me travel safety tips'";
        } else if (query.contains("who are you") || query.contains("name")) {
            return "🤖 I am **Voyageur AI**, your personal intelligent travel guide and planner! I can generate custom itineraries, estimate trip budgets, suggest local food & packing checklists, and trigger smart alerts for your journeys.";
        } else {
            return "🤖 **Voyageur AI Insight**\n\n" +
                    "That sounds like an exciting travel idea! For query '" + query + "', I recommend checking local attraction hours, verifying visa guidelines, and booking accommodations early.\n\n" +
                    "Need specific details? Ask me about itineraries, packing, budget estimates, food, or safety tips!";
        }
    }
}
