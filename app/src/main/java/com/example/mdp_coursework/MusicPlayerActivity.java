package com.example.mdp_coursework;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity {
    private String filePath,songTitle;
    private static final String KEY_IS_PLAYING = "is_playing";
    private Button playPauseButton,nextButton,previousButton,backButton,bookmark,clearBookmark;
    private int duration, currentPosition,savedPosition;
    private boolean wasPlayingBeforeSeek = false;
    private SettingVM settingViewModel;
    private float currentSpeed = 1.0f;
    private static final String KEY_PLAYBACK_SPEED = "playback_speed";
    private TextView songTitleTextView;
    private static final String KEY_CURRENT_POSITION = "current_position";
    private static final String KEY_PLAYLIST_POSITION = "playlist_position";
    private SeekBar seekBar;
    private TextView totalTimeTextView,currentTimeTextView,playbackSpeed;
    MusicPlayerVM viewModel;
    private AudioPlayerService audiobookService;
    private boolean bound = false;
    private ArrayList<String> playlist;
    private int selectedColor = Color.WHITE;
    private static final String KEY_BACKGROUND_COLOR = "background_color";
    private View rootView;
    private static final String KEY_SONG_TITLE = "songTitle";
    boolean wasPlaying;


    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AudioPlayerService.AudiobookBinder binder = (AudioPlayerService.AudiobookBinder) service;
            audiobookService = binder.getService();
            bound = true;
            currentSpeed = settingViewModel.getCurrentSpeed();

            // Set up progress callback
            audiobookService.setProgressCallback(new AudioPlayerService.ProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    if (seekBar.getProgress() != progress) {
                        updateProgress(progress);
                    }
                    viewModel.setPlaybackState(progress, audiobookService.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING);
                }

                @Override
                public void onPlaybackStateChanged(AudiobookPlayer.AudiobookPlayerState state) {
                    updatePlayPauseButton(state);
                }
            });

            // Check if this is a configuration change
            if (getIntent().getBooleanExtra("isNewStart", true)) {
                // Initial setup - only do this for fresh start
                filePath = "/storage/self/primary/Music/" + playlist.get(currentPosition);
                audiobookService.loadAudiobook(filePath, currentSpeed);

                if (viewModel.hasBookmark(songTitle)) {
                    int bookmarkPosition = viewModel.getBookmarkPosition(songTitle);
                    seekBar.setProgress(bookmarkPosition);
                    audiobookService.seekTo(bookmarkPosition);
                    audiobookService.showNotification("Now Playing", songTitle);
                } else {
                    audiobookService.seekTo(0);
                    audiobookService.showNotification("Now Playing", songTitle);
                }
            }

            // Always update UI
            if (bound && audiobookService != null) {
                duration = audiobookService.getDuration();
                seekBar.setMax(duration);
                totalTimeTextView.setText(viewModel.formatTime(duration));
                seekBar.setProgress(savedPosition);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);
        rootView = findViewById(R.id.playerLayout);

        viewModel = new ViewModelProvider(this).get(MusicPlayerVM.class);
        settingViewModel = new ViewModelProvider(this).get(SettingVM.class);

        boolean isNewStart = savedInstanceState == null;


        if (savedInstanceState != null) {
            selectedColor = savedInstanceState.getInt(KEY_BACKGROUND_COLOR, Color.WHITE);
            currentSpeed = savedInstanceState.getFloat(KEY_PLAYBACK_SPEED, 1.0f);
            savedPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            songTitle = savedInstanceState.getString(KEY_SONG_TITLE, "");
            currentPosition = savedInstanceState.getInt(KEY_PLAYLIST_POSITION, 0);
            wasPlaying = savedInstanceState.getBoolean(KEY_IS_PLAYING, false);

            // Get playlist from intent since it's not saved in state
            playlist = getIntent().getStringArrayListExtra("playlist");
        } else {
            selectedColor = getIntent().getIntExtra(KEY_BACKGROUND_COLOR, Color.WHITE);
            currentSpeed = settingViewModel.getCurrentSpeed();
            songTitle = getIntent().getStringExtra("songTitle");
            filePath = getIntent().getStringExtra("filePath");
            playlist = getIntent().getStringArrayListExtra("playlist");
            currentPosition = playlist.indexOf(songTitle);
        }

        settingViewModel.getPlaybackSpeed().observe(this, speed -> {
            currentSpeed = speed;
            if (bound && audiobookService != null) {
                audiobookService.setSpeed(currentSpeed);
            }
        });

        initializeViews();
        setupUI();
        setUpClickListeners();

        // Bind service
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.putExtra("isNewStart", isNewStart);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void initializeViews() {
        songTitleTextView = findViewById(R.id.songTitleTextView);
        playPauseButton = findViewById(R.id.playButton);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        seekBar = findViewById(R.id.seekBar);
        bookmark = findViewById(R.id.Bookmark);
        clearBookmark = findViewById(R.id.clearBookmark);
        backButton = findViewById(R.id.BackButton);
        playbackSpeed = findViewById(R.id.plackbackSpeed);
    }

    private void setupUI() {
        rootView.setBackgroundColor(selectedColor);
        songTitleTextView.setText(songTitle);
        playbackSpeed.setText("playback speed: " + currentSpeed + "x");
        viewModel.setPlaylist(playlist, currentPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_BACKGROUND_COLOR, selectedColor);
        outState.putFloat(KEY_PLAYBACK_SPEED, currentSpeed);
        outState.putString(KEY_SONG_TITLE, songTitle);
        outState.putInt(KEY_PLAYLIST_POSITION, currentPosition);

        if (bound && audiobookService != null) {
            audiobookService.setConfigurationChange(true);
            outState.putInt(KEY_CURRENT_POSITION, audiobookService.getCurrentPosition());
            outState.putBoolean(KEY_IS_PLAYING, audiobookService.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING);
        } else {
            outState.putInt(KEY_CURRENT_POSITION, seekBar.getProgress());
        }
    }

    private void setUpClickListeners() {
        playPauseButton.setOnClickListener(v -> {
            if (bound && audiobookService != null) {
                if (audiobookService.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING) {
                    audiobookService.pauseAudiobook();
                    audiobookService.removeNotification();
                } else {
                    audiobookService.playAudiobook();
                    audiobookService.showNotification("Continued to play: ", songTitle);
                }
            }
        });

        nextButton.setOnClickListener(v -> {
            if (playlist != null && !playlist.isEmpty()) {
                currentPosition++;
                if (currentPosition >= playlist.size()) {
                    currentPosition = 0; // Loop back to the start
                }
                loadSongAtCurrentPosition();
            }
        });

        previousButton.setOnClickListener(v -> {
            if (playlist != null && !playlist.isEmpty()) {
                currentPosition--;
                if (currentPosition < 0) {
                    currentPosition = playlist.size() - 1; // Loop back to the end
                }
                loadSongAtCurrentPosition();
            }
        });

        bookmark.setOnClickListener(v -> {
            if (bound && audiobookService != null) {
                int currentPosition = viewModel.getCurrentPosition();
                viewModel.setBookmark(songTitle, currentPosition);
                Toast.makeText(this, "Bookmark set!", Toast.LENGTH_SHORT).show();
            }
        });

        clearBookmark.setOnClickListener(v -> {
            viewModel.clearBookmark(songTitle);
            Toast.makeText(this, "Bookmark cleared!", Toast.LENGTH_SHORT).show();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && bound && audiobookService != null) {
                    audiobookService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (bound && audiobookService != null) {
                    wasPlayingBeforeSeek = audiobookService.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (bound && audiobookService != null && wasPlayingBeforeSeek) {
                    audiobookService.playAudiobook();
                }
            }
        });

        backButton.setOnClickListener(v -> {
            if (bound && audiobookService != null) {
                audiobookService.stopAudiobook();
            }
            Intent intent = new Intent(MusicPlayerActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void updateProgress(int progress) {
        if (seekBar.getProgress() != progress) {
            seekBar.setProgress(progress);
            currentTimeTextView.setText(viewModel.formatTime(progress));
        }
    }

    private void updatePlayPauseButton(AudiobookPlayer.AudiobookPlayerState state) {
        playPauseButton.setText(state == AudiobookPlayer.AudiobookPlayerState.PLAYING ? "Pause" : "Play");
    }

    @Override
    protected void onDestroy() {
        if (bound) {
            if (!isChangingConfigurations()) {
                if (audiobookService != null) {
                    audiobookService.stopAudiobook();
                }
            }
            unbindService(connection);
            bound = false;
        }
        super.onDestroy();
    }

    private void loadSongAtCurrentPosition() {
        audiobookService.stop();
        if (playlist != null && !playlist.isEmpty()) {
            // Update current song title and file path
            songTitle = playlist.get(currentPosition);
            filePath = "/storage/self/primary/Music/" + songTitle;

            // Update the UI
            songTitleTextView.setText(songTitle);

            if (bound && audiobookService != null) {
                // Stop the current audiobook
                audiobookService.stopAudiobook();

                // Load the new audiobook with the updated file path
                audiobookService.loadAudiobook(filePath, currentSpeed);

                // Retrieve and set the duration
                duration = audiobookService.getDuration();
                seekBar.setMax(duration);
                totalTimeTextView.setText(viewModel.formatTime(duration));

                // Check for bookmarks or start from the beginning
                int startPosition = viewModel.hasBookmark(songTitle)
                        ? viewModel.getBookmarkPosition(songTitle)
                        : 0;
                audiobookService.seekTo(startPosition);
                viewModel.setPlaybackState(startPosition, false); // Sync ViewModel state

                // Start playing and show notification
                audiobookService.playAudiobook();
                audiobookService.showNotification("Now Playing", songTitle);

                // Update ViewModel with the current song and position
                viewModel.setCurrentSongTitle(songTitle);
                viewModel.setPlaylist(playlist, currentPosition); // Update playlist state
            }
        }
    }
}