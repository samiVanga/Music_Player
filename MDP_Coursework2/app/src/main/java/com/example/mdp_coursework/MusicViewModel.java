package com.example.mdp_coursework;

import android.os.Environment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicViewModel extends ViewModel {
    private final MutableLiveData<List<String>> musicFiles;

    public MusicViewModel() {
        this.musicFiles = new MutableLiveData<>();
    }

    public LiveData<List<String>> getMusicFiles() {
        return musicFiles;
    }



    public void loadMusicFiles() { //loads the files fromt he directory
        List<String> files = new ArrayList<>();
        File musicDir = new File(Environment.getExternalStorageDirectory(), "Music");
        if (musicDir.exists() && musicDir.isDirectory()) {
            File[] fileList = musicDir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile() && file.getName().endsWith(".mp3")) {
                        files.add(file.getName());
                    }
                }
            }
        }
        musicFiles.setValue(files);
    }
}
