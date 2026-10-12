package com.voyageur.app;

public class AISmartAlert {
    private String id;
    private String title;
    private String message;
    private String priority; // HIGH, MEDIUM, LOW
    private String category; // Budget, Weather, Packing, Deal
    private long timestamp;
    private String actionText;

    public AISmartAlert(String id, String title, String message, String priority, String category, String actionText) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
        this.actionText = actionText;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getActionText() {
        return actionText;
    }
}
