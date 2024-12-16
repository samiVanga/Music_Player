package com.example.mdp_coursework;

import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MusicPlayerVM extends ViewModel {
    private List<String> playlist;
    private int currentIndex = 0;
    private int currentPosition = 0;  // Store current playback position
    private boolean isPlaying = false;  // Store playing state
    private String currentSongTitle = ""; // Store current song title


    private static Map<String, Integer> bookmarkStorage = new HashMap<>();



    public void setPlaybackState(int position, boolean playing) {
        currentPosition = position;
        isPlaying = playing;
    }
    public String formatTime(int milliseconds) { //this is to format the time from miliseconds
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public int getCurrentPosition() {
        return currentPosition;

    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaylist(List<String> songs, int startPosition) {
        this.playlist = songs;
        this.currentIndex = startPosition;

    }
    public void setBookmark(String audiobookTitle, int position) { //thsi sets a bookmark
        bookmarkStorage.put(audiobookTitle, position);
    }

    public void clearBookmark(String audiobookTitle) {
        bookmarkStorage.remove(audiobookTitle);
    } //clears the bookmark so a new one can be set

    public boolean hasBookmark(String audiobookTitle) { //checks if the audiobook has a bookmark
        return bookmarkStorage.containsKey(audiobookTitle);
    }

    public int getBookmarkPosition(String audiobookTitle) { //retrieved the position of the created bookamrk
        return bookmarkStorage.getOrDefault(audiobookTitle, 0);
    }
    public void setCurrentSongTitle(String songTitle) { //this is the current song title that needs to be played
        currentSongTitle = songTitle;
        if (playlist != null) {
            currentIndex = playlist.indexOf(songTitle);
        }
    }



}
