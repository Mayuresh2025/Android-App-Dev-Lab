package com.voyageur.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private EditText etChatMessage;
    private Button btnSend;
    private ImageView btnBack;
    private ImageView btnClearChat;
    private TextView tvTypingStatus;

    private Button chipParisItinerary;
    private Button chipTokyoBudget;
    private Button chipBeachPacking;
    private Button chipSafetyTips;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private AIChatbotEngine chatbotEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        rvChatMessages = findViewById(R.id.rvChatMessages);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnClearChat = findViewById(R.id.btnClearChat);
        tvTypingStatus = findViewById(R.id.tvTypingStatus);

        chipParisItinerary = findViewById(R.id.chipParisItinerary);
        chipTokyoBudget = findViewById(R.id.chipTokyoBudget);
        chipBeachPacking = findViewById(R.id.chipBeachPacking);
        chipSafetyTips = findViewById(R.id.chipSafetyTips);

        messageList = new ArrayList<>();
        chatbotEngine = new AIChatbotEngine();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setAdapter(chatAdapter);

        // Initial welcome greeting from AI
        addAiMessage("👋 Hello! I am Voyageur AI, your personal travel assistant.\n\nAsk me anything about destination itineraries, packing checklists, budget estimations, local food, or travel safety!", "Welcome");

        btnSend.setOnClickListener(v -> sendMessage());

        btnBack.setOnClickListener(v -> finish());

        btnClearChat.setOnClickListener(v -> {
            messageList.clear();
            addAiMessage("Chat cleared! What would you like to explore next?", "System");
            Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show();
        });

        // Quick suggestion chips
        chipParisItinerary.setOnClickListener(v -> sendQuickQuery("Suggest a 3-day Paris itinerary"));
        chipTokyoBudget.setOnClickListener(v -> sendQuickQuery("What is the estimated budget for Tokyo?"));
        chipBeachPacking.setOnClickListener(v -> sendQuickQuery("What should I pack for a beach vacation?"));
        chipSafetyTips.setOnClickListener(v -> sendQuickQuery("Give me travel safety tips"));
    }

    private void sendQuickQuery(String query) {
        etChatMessage.setText(query);
        sendMessage();
    }

    private void sendMessage() {
        String text = etChatMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return;
        }

        // Add user message
        messageList.add(new ChatMessage(text, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatMessages.smoothScrollToPosition(messageList.size() - 1);

        etChatMessage.setText("");

        // Show typing indicator
        tvTypingStatus.setVisibility(View.VISIBLE);

        // Generate AI Response
        chatbotEngine.generateResponse(text, (response, category) -> {
            tvTypingStatus.setVisibility(View.GONE);
            addAiMessage(response, category);
        });
    }

    private void addAiMessage(String text, String category) {
        messageList.add(new ChatMessage(text, false, category));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
    }
}
