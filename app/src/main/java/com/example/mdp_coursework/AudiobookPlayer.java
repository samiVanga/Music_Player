package com.example.mdp_coursework;

/**
 * Created by pszat on 14/05/24
 */

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudiobookPlayer {

    protected MediaPlayer mediaPlayer;
    protected AudiobookPlayerState state;
    protected String filePath;

    public enum AudiobookPlayerState {
        ERROR,
        PLAYING,
        PAUSED,
        STOPPED
    }

    public AudiobookPlayer() {
        this.state = AudiobookPlayerState.STOPPED;
    }

    public AudiobookPlayerState getState() {
        return this.state;
    }

    public void load(String filePath, float speed) {
        this.filePath = filePath;
        mediaPlayer = new MediaPlayer();
        File file = new File(filePath);


        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        mediaPlayer.setAudioAttributes(audioAttributes);


        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("AudiobookPlayer", e.toString());
            e.printStackTrace();
            this.state = AudiobookPlayerState.ERROR;
            return;
        } catch (IllegalArgumentException e) {
            Log.e("AudiobookPlayer", e.toString());
            e.printStackTrace();
            this.state = AudiobookPlayerState.ERROR;
            return;
        }

        this.state = AudiobookPlayerState.PLAYING;
        mediaPlayer.start();
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getProgress() {
        if (mediaPlayer != null) {
            if (this.state == AudiobookPlayerState.PAUSED || this.state == AudiobookPlayerState.PLAYING)
                return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void play() {
        if (this.state == AudiobookPlayerState.PAUSED) {
            mediaPlayer.start();
            this.state = AudiobookPlayerState.PLAYING;
        }
    }

    public void pause() {
        if (this.state == AudiobookPlayerState.PLAYING) {
            mediaPlayer.pause();
            this.state = AudiobookPlayerState.PAUSED;
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            this.state = AudiobookPlayerState.STOPPED;
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void setPlaybackSpeed(float speed) { //sets the playback speed of the audiobook
        if (mediaPlayer != null) {
            this.state = AudiobookPlayerState.PAUSED;
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            this.state = AudiobookPlayerState.PLAYING;
        }
    }

    public void skipTo(int milliseconds) { // allow the user to skip to the position of the audiobook or when using the seekbar, skip to the position
        if (mediaPlayer != null && (this.state == AudiobookPlayerState.PLAYING || this.state == AudiobookPlayerState.PAUSED)) {
            this.state = AudiobookPlayerState.PAUSED;
            mediaPlayer.seekTo(milliseconds);
            this.state = AudiobookPlayerState.PLAYING;
        }
    }
}
