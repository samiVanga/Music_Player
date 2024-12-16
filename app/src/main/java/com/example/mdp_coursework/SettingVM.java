package com.example.mdp_coursework;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingVM extends ViewModel{

    private static MutableLiveData<Float> playbackSpeed;

    public SettingVM() {
        if (playbackSpeed == null) {
            playbackSpeed = new MutableLiveData<>(1.0f);
        }
    }

    public void setPlaybackSpeed(float speed) {
        playbackSpeed.setValue(speed);
    } //set the playback speed

    public LiveData<Float> getPlaybackSpeed() {
        if (playbackSpeed == null) {
            playbackSpeed = new MutableLiveData<>(1.0f);
        }
        return playbackSpeed;
    }

    public float getCurrentSpeed() { //get the current speed of the playback
        if (playbackSpeed == null || playbackSpeed.getValue() == null) {
            return 1.0f;
        }
        return playbackSpeed.getValue();
    }
    }



