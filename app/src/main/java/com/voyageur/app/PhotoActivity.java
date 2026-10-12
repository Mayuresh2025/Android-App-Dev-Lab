package com.voyageur.app;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PhotoActivity extends AppCompatActivity {

    private GridView gridPhotos;
    private TextView tvPhotoStatus;
    private final List<Uri> photoUris = new ArrayList<>();
    private PhotoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        gridPhotos = findViewById(R.id.gridPhotos);
        tvPhotoStatus = findViewById(R.id.tvPhotoStatus);
        ImageButton btnPhotoBack = findViewById(R.id.btnPhotoBack);

        adapter = new PhotoAdapter();
        gridPhotos.setAdapter(adapter);

        if (btnPhotoBack != null) {
            btnPhotoBack.setOnClickListener(v -> finish());
        }

        loadVoyageurPhotos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gridPhotos != null && adapter != null) {
            loadVoyageurPhotos();
        }
    }

    private void loadVoyageurPhotos() {
        photoUris.clear();

        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH
        };

        String selection = null;
        String[] selectionArgs = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
            selectionArgs = new String[]{"Pictures/Voyageur/%"};
        } else {
            selection = MediaStore.Images.Media.DISPLAY_NAME + " LIKE ?";
            selectionArgs = new String[]{"Voyageur_%"};
        }

        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media._ID
                );

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);

                    Uri photoUri = Uri.withAppendedPath(
                            collection,
                            String.valueOf(id)
                    );

                    photoUris.add(photoUri);
                }
            }

            adapter.notifyDataSetChanged();

            if (photoUris.isEmpty()) {
                tvPhotoStatus.setVisibility(View.VISIBLE);
                tvPhotoStatus.setText(
                        "No Voyageur photos found yet.\n\n"
                                + "Open Travel Camera, capture a photo, "
                                + "and return here."
                );
            } else {
                tvPhotoStatus.setVisibility(View.VISIBLE);
                tvPhotoStatus.setText(
                        "Your Travel Memories (" + photoUris.size() + " photos)\nLong-press a photo to delete it"
                );
            }

        } catch (Exception e) {
            tvPhotoStatus.setVisibility(View.VISIBLE);
            tvPhotoStatus.setText(
                    "Unable to load Travel Memories:\n" + e.getMessage()
            );

            Toast.makeText(
                    this,
                    "Unable to load photos.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void confirmDeletePhoto(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo from your travel memories?")
                .setPositiveButton("Delete", (dialog, which) -> deletePhoto(uri))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePhoto(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            int rowsDeleted = resolver.delete(uri, null, null);
            if (rowsDeleted > 0) {
                Toast.makeText(this, "Photo deleted successfully", Toast.LENGTH_SHORT).show();
                loadVoyageurPhotos();
            } else {
                // Remove from list if media store couldn't delete directly
                photoUris.remove(uri);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException se) {
            // Android 10+ scoped storage recoverable security exception
            Toast.makeText(this, "Permission required to delete photo from storage", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to delete photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openPhoto(Uri uri) {
        if (uri == null) {
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            showPhotoDialog(uri);
        }
    }

    private void showPhotoDialog(Uri uri) {
        try {
            Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageURI(uri);
            imageView.setOnClickListener(v -> dialog.dismiss());
            dialog.setContentView(imageView);
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to display photo.", Toast.LENGTH_SHORT).show();
        }
    }

    private static class SquareImageView extends androidx.appcompat.widget.AppCompatImageView {
        public SquareImageView(android.content.Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    private class PhotoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return photoUris.size();
        }

        @Override
        public Object getItem(int position) {
            return photoUris.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {
            SquareImageView imageView;

            if (convertView instanceof SquareImageView) {
                imageView = (SquareImageView) convertView;
            } else {
                imageView = new SquareImageView(PhotoActivity.this);
                imageView.setLayoutParams(new GridView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(4, 4, 4, 4);
                imageView.setBackgroundColor(0xFFE1E8EF);
            }

            Uri uri = photoUris.get(position);
            imageView.setContentDescription("Travel memory " + (position + 1));

            try {
                imageView.setImageURI(null);
                imageView.setImageURI(uri);
            } catch (Exception e) {
                imageView.setImageResource(
                        android.R.drawable.ic_menu_report_image
                );
            }

            imageView.setOnClickListener(v -> openPhoto(uri));

            imageView.setOnLongClickListener(v -> {
                confirmDeletePhoto(uri);
                return true;
            });

            return imageView;
        }
    }
}
