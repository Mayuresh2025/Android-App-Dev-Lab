package com.voyageur.app;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private ImageView ivCapturedPhoto;
    private TextView tvCameraStatus;
    private Button btnCapturePhoto;

    private File photoFile;
    private Uri photoUri;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ivCapturedPhoto = findViewById(R.id.ivCapturedPhoto);
        tvCameraStatus = findViewById(R.id.tvCameraStatus);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        ImageButton btnCameraBack = findViewById(R.id.btnCameraBack);
        ImageButton btnTopGallery = findViewById(R.id.btnTopGallery);
        View layoutGalleryBtn = findViewById(R.id.layoutGalleryBtn);
        View flCaptureButton = findViewById(R.id.flCaptureButton);
        View layoutFlipBtn = findViewById(R.id.layoutFlipBtn);
        TextView tvModePhoto = findViewById(R.id.tvModePhoto);
        TextView tvModeVideo = findViewById(R.id.tvModeVideo);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContract<Uri, Boolean>() {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull Uri input) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, input);
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setClipData(ClipData.newRawUri("photo", input));
                        return intent;
                    }

                    @Override
                    public Boolean parseResult(int resultCode, @Nullable Intent intent) {
                        return resultCode == RESULT_OK;
                    }
                },
                success -> {
                    if (btnCapturePhoto != null) {
                        btnCapturePhoto.setEnabled(true);
                    }

                    boolean hasPhoto = photoFile != null && photoFile.exists() && photoFile.length() > 0;

                    if (success || hasPhoto) {
                        try {
                            Uri savedMediaUri = saveToMediaStore(photoFile);
                            Uri displayUri = (savedMediaUri != null) ? savedMediaUri : photoUri;

                            ivCapturedPhoto.setImageURI(null);
                            ivCapturedPhoto.setImageURI(displayUri);

                            tvCameraStatus.setText("Photo saved to Pictures/Voyageur!");

                            Toast.makeText(
                                    this,
                                    "Photo saved successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } catch (Exception e) {
                            tvCameraStatus.setText("Save failed: " + e.getMessage());
                        } finally {
                            cleanupTempFile();
                        }
                    } else {
                        cleanupTempFile();
                        tvCameraStatus.setText("Capture cancelled. Tap shutter to try again.");
                    }
                }
        );

        if (btnCapturePhoto != null) {
            btnCapturePhoto.setOnClickListener(v -> capturePhoto());
        }

        if (flCaptureButton != null) {
            flCaptureButton.setOnClickListener(v -> capturePhoto());
        }

        if (btnCameraBack != null) {
            btnCameraBack.setOnClickListener(v -> finish());
        }

        View.OnClickListener openGalleryAction = v -> {
            startActivity(new Intent(CameraActivity.this, PhotoActivity.class));
        };

        if (btnTopGallery != null) {
            btnTopGallery.setOnClickListener(openGalleryAction);
        }
        if (layoutGalleryBtn != null) {
            layoutGalleryBtn.setOnClickListener(openGalleryAction);
        }

        if (layoutFlipBtn != null) {
            layoutFlipBtn.setOnClickListener(v ->
                    Toast.makeText(this, "Switched to front camera", Toast.LENGTH_SHORT).show());
        }

        if (tvModePhoto != null && tvModeVideo != null) {
            tvModePhoto.setOnClickListener(v -> {
                tvModePhoto.setBackgroundResource(R.drawable.bg_camera_pill_selected);
                tvModePhoto.setTextColor(ContextCompat.getColor(this, R.color.white));
                tvModeVideo.setBackground(null);
                tvModeVideo.setTextColor(ContextCompat.getColor(this, R.color.text_light));
            });

            tvModeVideo.setOnClickListener(v -> {
                tvModeVideo.setBackgroundResource(R.drawable.bg_camera_pill_selected);
                tvModeVideo.setTextColor(ContextCompat.getColor(this, R.color.white));
                tvModePhoto.setBackground(null);
                tvModePhoto.setTextColor(ContextCompat.getColor(this, R.color.text_light));
                Toast.makeText(this, "Video recording mode selected", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void capturePhoto() {
        try {
            photoFile = createTempFile();
            if (photoFile == null) {
                tvCameraStatus.setText("Unable to create photo file.");
                Toast.makeText(this, "Unable to prepare photo storage.", Toast.LENGTH_SHORT).show();
                return;
            }

            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );

            grantUriPermissions(photoUri);

            if (btnCapturePhoto != null) {
                btnCapturePhoto.setEnabled(false);
            }
            tvCameraStatus.setText("Opening camera...");

            cameraLauncher.launch(photoUri);
        } catch (Exception e) {
            cleanupTempFile();
            if (btnCapturePhoto != null) {
                btnCapturePhoto.setEnabled(true);
            }
            tvCameraStatus.setText("Unable to open camera: " + e.getMessage());
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFile() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                storageDir = getCacheDir();
            }
            return File.createTempFile("Voyageur_" + timestamp + "_", ".jpg", storageDir);
        } catch (Exception e) {
            return null;
        }
    }

    private void grantUriPermissions(Uri uri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        }

        String[] knownCameraPackages = {
                "com.android.camera",
                "com.google.android.GoogleCamera",
                "com.android.camera2",
                "com.sec.android.app.camera"
        };
        for (String pkg : knownCameraPackages) {
            try {
                grantUriPermission(
                        pkg,
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {
            }
        }
    }

    private Uri saveToMediaStore(File imageFile) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Voyageur_" + timestamp + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Voyageur");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (FileInputStream in = new FileInputStream(imageFile);
                 OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out != null) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues updateValues = new ContentValues();
                updateValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(uri, updateValues, null, null);
            }
        }
        return uri;
    }

    private void cleanupTempFile() {
        if (photoUri != null) {
            try {
                revokeUriPermission(
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {
            }
        }
        if (photoFile != null && photoFile.exists()) {
            try {
                photoFile.delete();
            } catch (Exception ignored) {
            }
        }
    }
}
