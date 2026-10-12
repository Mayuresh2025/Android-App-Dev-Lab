package com.voyageur.app;

import java.util.ArrayList;
import java.util.List;

public class AISmartAlertEngine {

    public static List<AISmartAlert> generateActiveAlerts() {
        List<AISmartAlert> alerts = new ArrayList<>();

        alerts.add(new AISmartAlert(
                "alert_1",
                "🚨 High-Priority Budget Risk Alert",
                "Your planned Paris trip budget (€500) is 25% lower than the AI-predicted average cost (€680). Consider adjusting accommodation tier or daily food budget.",
                "HIGH",
                "Budget",
                "Adjust Budget"
        ));

        alerts.add(new AISmartAlert(
                "alert_2",
                "🌧️ Weather Hazard & Packing Warning",
                "Heavy rain showers forecast in Tokyo during your upcoming visit dates (Oct 12 - Oct 15). AI suggests packing a waterproof jacket and compact umbrella.",
                "HIGH",
                "Weather",
                "View Weather & Packing"
        ));

        alerts.add(new AISmartAlert(
                "alert_3",
                "🧳 Trip Departure Countdown & Reminders",
                "Your Paris adventure starts in 3 days! Complete web check-in, download offline maps, and verify passport validity.",
                "MEDIUM",
                "Packing",
                "Check Checklist"
        ));

        alerts.add(new AISmartAlert(
                "alert_4",
                "💡 AI Smart Price Drop Advisory",
                "Flight prices to Bali dropped by 18% for November departures! Saved 4-star resort rates are at a 6-month low.",
                "LOW",
                "Deal",
                "View Deal"
        ));

        return alerts;
    }
}
