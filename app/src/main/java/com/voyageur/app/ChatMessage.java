package com.voyageur.app;

public class ChatMessage {
    private String text;
    private boolean isUser;
    private long timestamp;
    private String category;

    public ChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
        this.category = "General";
    }

    public ChatMessage(String text, boolean isUser, String category) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCategory() {
        return category;
    }
}
