package com.voyageur.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;
import java.util.Locale;

public class MLImageClassifierActivity extends AppCompatActivity {

    private ImageView ivSelectedPhoto;
    private TextView tvMlResults;

    private EditText etPreferenceTag;
    private EditText etBudgetTier;
    private LinearLayout llRecommendationsContainer;

    private EditText etPredictDestination;
    private EditText etPredictDays;
    private EditText etPredictStyle;
    private TextView tvPredictionResult;

    private Bitmap selectedBitmap;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml_image_classifier);

        ImageView btnBack = findViewById(R.id.btnBack);
        ivSelectedPhoto = findViewById(R.id.ivSelectedPhoto);
        Button btnPickImage = findViewById(R.id.btnPickImage);
        Button btnRunMlClassification = findViewById(R.id.btnRunMlClassification);
        tvMlResults = findViewById(R.id.tvMlResults);

        etPreferenceTag = findViewById(R.id.etPreferenceTag);
        etBudgetTier = findViewById(R.id.etBudgetTier);
        Button btnGetRecommendations = findViewById(R.id.btnGetRecommendations);
        llRecommendationsContainer = findViewById(R.id.llRecommendationsContainer);

        etPredictDestination = findViewById(R.id.etPredictDestination);
        etPredictDays = findViewById(R.id.etPredictDays);
        etPredictStyle = findViewById(R.id.etPredictStyle);
        Button btnPredictCost = findViewById(R.id.btnPredictCost);
        tvPredictionResult = findViewById(R.id.tvPredictionResult);

        btnBack.setOnClickListener(v -> finish());

        // Setup image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                selectedBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            }
                            ivSelectedPhoto.setImageBitmap(selectedBitmap);
                            tvMlResults.setText("Image selected successfully! Tap 'Run ML Scan' to analyze with Google ML Kit.");
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Travel Image"));
        });

        btnRunMlClassification.setOnClickListener(v -> runMlClassification());

        btnGetRecommendations.setOnClickListener(v -> generateRecommendations());

        btnPredictCost.setOnClickListener(v -> runCostPrediction());
    }

    private void runMlClassification() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        tvMlResults.setText("Running Google ML Kit Image Labeler...");

        try {
            InputImage image = InputImage.fromBitmap(selectedBitmap, 0);
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        if (labels.isEmpty()) {
                            tvMlResults.setText("ML Scan completed. No prominent landmarks or labels detected.");
                            return;
                        }

                        StringBuilder sb = new StringBuilder();
                        sb.append("🏷️ **Google ML Kit Classification Labels:**\n\n");

                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence() * 100;
                            sb.append(String.format(Locale.US, "• **%s**: %.1f%% confidence\n", text, confidence));
                        }

                        sb.append("\n💡 **AI Travel Insight:**\n");
                        String primary = labels.get(0).getText().toLowerCase(Locale.ROOT);
                        if (primary.contains("building") || primary.contains("tower") || primary.contains("monument") || primary.contains("architecture")) {
                            sb.append("This image features historic architecture or a famous landmark! Recommended for cultural sightseeing & photography.");
                        } else if (primary.contains("mountain") || primary.contains("tree") || primary.contains("nature") || primary.contains("sky")) {
                            sb.append("This photo showcases natural landscapes! Great for hiking, eco-tourism, and adventure travel.");
                        } else if (primary.contains("water") || primary.contains("beach") || primary.contains("sea") || primary.contains("ocean")) {
                            sb.append("Tropical coastal scenery detected! Perfect for beach resort stays and water sports.");
                        } else if (primary.contains("food") || primary.contains("dish") || primary.contains("cuisine")) {
                            sb.append("Culinary photo detected! Recommended for local food tours and dining experiences.");
                        } else {
                            sb.append("Identified travel subject: '").append(labels.get(0).getText()).append("'. Ideal for custom itinerary tagging.");
                        }

                        tvMlResults.setText(sb.toString());
                    })
                    .addOnFailureListener(e -> tvMlResults.setText("ML Classification failed: " + e.getMessage()));
        } catch (Exception e) {
            tvMlResults.setText("Error processing image: " + e.getMessage());
        }
    }

    private void generateRecommendations() {
        String pref = etPreferenceTag.getText().toString().trim();
        String budget = etBudgetTier.getText().toString().trim();

        if (TextUtils.isEmpty(pref)) {
            pref = "Culture";
        }
        if (TextUtils.isEmpty(budget)) {
            budget = "Mid-Range";
        }

        List<MLRecommendationEngine.Recommendation> recs = MLRecommendationEngine.getRecommendations(pref, budget);
        llRecommendationsContainer.removeAllViews();

        for (MLRecommendationEngine.Recommendation rec : recs) {
            View card = getLayoutInflater().inflate(R.layout.item_ml_recommendation, llRecommendationsContainer, false);

            TextView tvRecTitle = card.findViewById(R.id.tvRecTitle);
            TextView tvRecMatchScore = card.findViewById(R.id.tvRecMatchScore);
            TextView tvRecSub = card.findViewById(R.id.tvRecSub);
            TextView tvRecHighlight = card.findViewById(R.id.tvRecHighlight);

            tvRecTitle.setText(rec.getDestination() + ", " + rec.getCountry());
            tvRecMatchScore.setText(rec.getMatchScore() + "% Match");
            tvRecSub.setText("Category: " + rec.getCategory() + " • Est. Cost: " + rec.getEstimatedCost());
            tvRecHighlight.setText("💡 " + rec.getHighlight());

            llRecommendationsContainer.addView(card);
        }
    }

    private void runCostPrediction() {
        String dest = etPredictDestination.getText().toString().trim();
        String daysStr = etPredictDays.getText().toString().trim();
        String style = etPredictStyle.getText().toString().trim();

        if (TextUtils.isEmpty(dest)) {
            etPredictDestination.setError("Destination required");
            return;
        }

        int days = 5;
        if (!TextUtils.isEmpty(daysStr)) {
            try {
                days = Integer.parseInt(daysStr);
            } catch (NumberFormatException ignored) {}
        }

        if (TextUtils.isEmpty(style)) {
            style = "Mid-Range";
        }

        MLBudgetPredictor.PredictionResult result = MLBudgetPredictor.predictBudget(dest, days, style);
        tvPredictionResult.setText(result.getSummary());
    }
}
