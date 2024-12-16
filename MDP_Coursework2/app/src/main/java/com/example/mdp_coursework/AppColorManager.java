package com.example.mdp_coursework;

import android.graphics.Color;

public class AppColorManager { //do deal with the saved instances for the background colour
    private static AppColorManager instance; //this is the saved instance
    private int backgroundColor = Color.WHITE;  // Default color to White

    private AppColorManager() {} // Private constructor

    public static AppColorManager getInstance() { //this gets the saved instance for the colour
        if (instance == null) {
            instance = new AppColorManager();
        }
        return instance;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }
}