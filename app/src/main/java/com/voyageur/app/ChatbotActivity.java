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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.widget.ImageButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private EditText etChatMessage;
    private Button btnSend;
    private ImageButton btnVoiceMic;
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

    private ActivityResultLauncher<Intent> speechRecognitionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        rvChatMessages = findViewById(R.id.rvChatMessages);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnSend = findViewById(R.id.btnSend);
        btnVoiceMic = findViewById(R.id.btnVoiceMic);
        btnBack = findViewById(R.id.btnBack);
        btnClearChat = findViewById(R.id.btnClearChat);
        tvTypingStatus = findViewById(R.id.tvTypingStatus);

        chipParisItinerary = findViewById(R.id.chipParisItinerary);
        chipTokyoBudget = findViewById(R.id.chipTokyoBudget);
        chipBeachPacking = findViewById(R.id.chipBeachPacking);
        chipSafetyTips = findViewById(R.id.chipSafetyTips);

        messageList = new ArrayList<>();
        chatbotEngine = new AIChatbotEngine();

        // Speech recognition launcher
        speechRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String voiceSpokenText = matches.get(0);
                            etChatMessage.setText(voiceSpokenText);
                            sendMessage();
                        }
                    }
                }
        );

        if (btnVoiceMic != null) {
            btnVoiceMic.setOnClickListener(v -> startVoiceInput());
        }

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

        // Check if an initial query was passed via Intent
        if (getIntent() != null && getIntent().hasExtra("initial_query")) {
            String initQuery = getIntent().getStringExtra("initial_query");
            if (!TextUtils.isEmpty(initQuery)) {
                sendQuickQuery(initQuery);
            }
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Voyageur AI (e.g., 'Plan a 3-day trip to Rome')");
        try {
            speechRecognitionLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
        chatbotEngine.generateResponse(this, text, (response, category) -> {
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
