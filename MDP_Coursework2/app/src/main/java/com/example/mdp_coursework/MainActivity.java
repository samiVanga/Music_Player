package com.example.mdp_coursework;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.databinding.DataBindingUtil;
import com.example.mdp_coursework.databinding.ActivityMainBinding;
import com.example.mdp_coursework.databinding.ItemMusicBinding;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MusicViewModel musicViewModel;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 101;
    private static final String KEY_PLAYBACK_SPEED = "playback_speed";
    private float selectedSpeed = 1.0f;
    private static final int REQUEST_CODE_SETTINGS = 1;
    private static int selectedColor = Color.WHITE; // Default color
    private static final String KEY_BACKGROUND_COLOR = "background_color";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Initialize ViewModel
        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        // Bind ViewModel to layout
        binding.setViewModel(musicViewModel);
        binding.setLifecycleOwner(this);

        // Restore saved state
        if (savedInstanceState != null) {
            selectedColor = savedInstanceState.getInt(KEY_BACKGROUND_COLOR, Color.WHITE);
            selectedSpeed = savedInstanceState.getFloat(KEY_PLAYBACK_SPEED, 1.0f);
        }
        binding.rootLayout.setBackgroundColor(selectedColor);

        // Initialize RecyclerView and Adapter
        MusicAdapter adapter = new MusicAdapter(this, musicViewModel, selectedColor, selectedSpeed);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Observe music files and update the adapter
        musicViewModel.getMusicFiles().observe(this, adapter::setMusicList);

        // Settings button
        binding.setting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            intent.putExtra(KEY_BACKGROUND_COLOR, selectedColor);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
        });

        // Check and request permissions
        checkAndRequestPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK && data != null) {
            selectedColor = data.getIntExtra(KEY_BACKGROUND_COLOR, Color.WHITE);
            binding.rootLayout.setBackgroundColor(selectedColor);
            if (binding.recyclerView.getAdapter() != null) {
                ((MusicAdapter) binding.recyclerView.getAdapter()).updateBackgroundColor(selectedColor);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_BACKGROUND_COLOR, selectedColor);
        outState.putFloat(KEY_PLAYBACK_SPEED, selectedSpeed);
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access the music files on your device.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                                MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO);
            }
        } else {
            musicViewModel.loadMusicFiles();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                musicViewModel.loadMusicFiles();
            } else {
                Toast.makeText(this, "Permission DENIED. Cannot access music files.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<String> musicList = new ArrayList<>();
    private Context context;
    private MusicViewModel musicViewModel;
    private int selectedColor;
    private float selectedSpeed;
    private static final String KEY_BACKGROUND_COLOR = "background_color";
    private static final String KEY_PLAYBACK_SPEED = "playback_speed";


    public void updateBackgroundColor(int color) {
        this.selectedColor = color;
        notifyDataSetChanged();
    }

    public MusicAdapter(Context context, MusicViewModel viewModel, int selectedColor, float selectedSpeed) {
        this.context = context;
        this.musicViewModel = viewModel;
        this.selectedColor = selectedColor;
        this.selectedSpeed = selectedSpeed;
    }

    public void setMusicList(List<String> musicList) {
        this.musicList = musicList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMusicBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_music,
                parent,
                false
        );
        return new MusicViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        String songTitle = musicList.get(position);
        holder.bind(songTitle);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MusicPlayerActivity.class);
            intent.putExtra("songTitle", songTitle);
            String filepath="/storage/self/primary/Music/"+songTitle;
            intent.putExtra("filePath",filepath);
            intent.putExtra(KEY_BACKGROUND_COLOR, selectedColor);
            intent.putExtra(KEY_PLAYBACK_SPEED,selectedSpeed);
            intent.putStringArrayListExtra("playlist", new ArrayList<>(musicList));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        private final ItemMusicBinding binding;

        public MusicViewHolder(@NonNull ItemMusicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String songTitle) {
            binding.setSongTitle(songTitle);
            binding.executePendingBindings();
        }
    }
}











