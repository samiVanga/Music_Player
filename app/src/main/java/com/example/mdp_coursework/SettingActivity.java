package com.example.mdp_coursework;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import androidx.databinding.DataBindingUtil;
import com.example.mdp_coursework.databinding.SettingsBinding;


import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class SettingActivity extends AppCompatActivity {
    private SettingVM viewModel;
    private static final String KEY_PLAYBACK_SPEED = "playback_speed";
    private float selectedSpeed = 1.0f;
    private static final String KEY_BACKGROUND_COLOR = "background_color";
    private int selectedColor = Color.WHITE; // Default color
    private SettingsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.settings);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SettingVM.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        // Retrieve saved instance or intent extras
        if (savedInstanceState != null) {
            selectedColor = savedInstanceState.getInt(KEY_BACKGROUND_COLOR, Color.WHITE);
            selectedSpeed = savedInstanceState.getFloat(KEY_PLAYBACK_SPEED, 1.0f);
            viewModel.setPlaybackSpeed(selectedSpeed);
        } else if (getIntent().hasExtra(KEY_BACKGROUND_COLOR)) {
            selectedColor = getIntent().getIntExtra(KEY_BACKGROUND_COLOR, Color.WHITE);
        }

        // Set up initial background color
        binding.settingsRoot.setBackgroundColor(selectedColor);

        // Set up spinner
        String[] speedOptions = {"1", "1.5", "2"};
        ArrayAdapter<String> speedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, speedOptions);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(speedAdapter);

        for (int i = 0; i < speedOptions.length; i++) {
            if (Float.parseFloat(speedOptions[i]) == selectedSpeed) {
                binding.spinner.setSelection(i);
                break;
            }
        }

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSpeed = Float.parseFloat(speedOptions[position]);
                viewModel.setPlaybackSpeed(selectedSpeed);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set up radio group for background color
        binding.colorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBlue) {
                selectedColor = Color.BLUE;
            } else if (checkedId == R.id.radioGreen) {
                selectedColor = Color.GREEN;
            } else if (checkedId == R.id.radioRed) {
                selectedColor = Color.RED;
            } else if (checkedId == R.id.radioWhite) {
                selectedColor = Color.WHITE;
            }
            binding.settingsRoot.setBackgroundColor(selectedColor);
        });

        // Back button logic
        binding.backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(KEY_BACKGROUND_COLOR, selectedColor);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    //for the intents fro backgrounf colour and speed
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_BACKGROUND_COLOR, selectedColor);
        outState.putFloat(KEY_PLAYBACK_SPEED, selectedSpeed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.setPlaybackSpeed(selectedSpeed);
    }
}