package com.voyageur.app;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MediaActivity extends AppCompatActivity {

    private TextView tvAudioStatus;
    private TextView tvVideoStatus;

    private Button btnPlayAudio;
    private Button btnPauseAudio;
    private Button btnStopAudio;

    private VideoView vvVideo;
    private Button btnPlayVideo;
    private Button btnPauseVideo;

    private MediaPlayer mediaPlayer;

    // Sample video URL for video playback
    private static final String VIDEO_URL =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        tvAudioStatus = findViewById(R.id.tvAudioStatus);
        tvVideoStatus = findViewById(R.id.tvVideoStatus);

        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        btnPauseAudio = findViewById(R.id.btnPauseAudio);
        btnStopAudio = findViewById(R.id.btnStopAudio);

        vvVideo = findViewById(R.id.vvVideo);
        btnPlayVideo = findViewById(R.id.btnPlayVideo);
        btnPauseVideo = findViewById(R.id.btnPauseVideo);

        ImageButton btnMediaBack = findViewById(R.id.btnMediaBack);
        btnMediaBack.setOnClickListener(v -> finish());

        setupAudioPlayer();
        setupVideoPlayer();
    }

    private void setupAudioPlayer() {
        btnPlayAudio.setOnClickListener(v -> playAudio());
        btnPauseAudio.setOnClickListener(v -> pauseAudio());
        btnStopAudio.setOnClickListener(v -> stopAudio());
    }

    private void playAudio() {
        try {
            if (mediaPlayer == null) {
                tvAudioStatus.setText("Loading audio...");
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                );

                // Use default system ringtone/notification uri for reliable audio playback
                Uri audioUri = Settings.System.DEFAULT_RINGTONE_URI;
                if (audioUri == null) {
                    audioUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                }

                mediaPlayer.setDataSource(getApplicationContext(), audioUri);
                mediaPlayer.setLooping(true);

                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start();
                    tvAudioStatus.setText("Audio Status: Playing audio");
                    Toast.makeText(MediaActivity.this, "Audio playback started", Toast.LENGTH_SHORT).show();
                });

                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    tvAudioStatus.setText("Audio Error occurred");
                    return false;
                });

                mediaPlayer.prepareAsync();
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                tvAudioStatus.setText("Audio Status: Playing audio");
            }
        } catch (Exception e) {
            tvAudioStatus.setText("Failed to play audio: " + e.getMessage());
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            tvAudioStatus.setText("Audio Status: Paused");
            Toast.makeText(this, "Audio paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            tvAudioStatus.setText("Audio Status: Stopped");
            Toast.makeText(this, "Audio stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupVideoPlayer() {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(vvVideo);
        vvVideo.setMediaController(mediaController);

        Uri videoUri = Uri.parse(VIDEO_URL);
        vvVideo.setVideoURI(videoUri);

        vvVideo.setOnPreparedListener(mp -> {
            tvVideoStatus.setText("Video Status: Video ready. Press Play Video.");
        });

        vvVideo.setOnErrorListener((mp, what, extra) -> {
            tvVideoStatus.setText("Video Status: Unable to stream video");
            return true;
        });

        vvVideo.setOnCompletionListener(mp -> {
            tvVideoStatus.setText("Video Status: Playback completed");
        });

        btnPlayVideo.setOnClickListener(v -> {
            vvVideo.start();
            tvVideoStatus.setText("Video Status: Playing video");
        });

        btnPauseVideo.setOnClickListener(v -> {
            if (vvVideo.isPlaying()) {
                vvVideo.pause();
                tvVideoStatus.setText("Video Status: Paused");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseAudio();
        if (vvVideo != null && vvVideo.isPlaying()) {
            vvVideo.pause();
        }
    }

    @Override
    protected void onDestroy() {
        stopAudio();
        super.onDestroy();
    }
}
