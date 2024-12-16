package com.example.mdp_coursework;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

public class AudioPlayerService extends Service {
    private final IBinder binder = new AudiobookBinder();
    private static final String CHANNEL_ID ="playbackChannel";
    private static final int NOTIFICATION_ID = 1;
    private boolean isConfigurationChange = false;
    private boolean isConfigChange = false;

    private boolean isNotificationShowing = false;  // Track if notification is already showing
    private boolean isPlaying = false;
    private AudiobookPlayer player;
    private Handler progressHandler;
    private ProgressCallback progressCallback;

    public AudioPlayerService() {
    }
    public interface ProgressCallback {
        void onProgressUpdate(int progress);
        void onPlaybackStateChanged(AudiobookPlayer.AudiobookPlayerState state);
    }

    public class AudiobookBinder extends Binder {
        AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        player = new AudiobookPlayer();
        progressHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        startProgressUpdates();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;    }

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }
    private void startProgressUpdates() { //this is to display the current progress
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressCallback != null && player != null) {
                    progressCallback.onProgressUpdate(player.getProgress());
                    progressCallback.onPlaybackStateChanged(player.getState());
                }
                progressHandler.postDelayed(this,0); // Update
            }
        }, 1000);
    }
    public void removeNotification() { //when pause pressed notificaiton is removed
        stopForeground(true);
        isNotificationShowing = false;
    }
    public int getDuration(){
        return player.mediaPlayer.getDuration();
    }
    public void setConfigurationChange(boolean isChange) {// to set when the page is rotated
        isConfigurationChange = isChange;
    }


    public void loadAudiobook(String filePath, float speed) {
        if (!isConfigChange) {
            player.load(filePath, speed);
        }
    }
    public void showNotification(String ContentTitle, String ContentText){ //displays the notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(ContentTitle)
                .setContentText(ContentText)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();
        startForeground(NOTIFICATION_ID,notification);

    }
    public void playAudiobook() {
        player.play();
        isPlaying = true;
    }
    public void stop(){
        player.stop();
    }
    public int getCurrentPosition(){
        return player.getProgress();
    }

    public void pauseAudiobook() {
        player.pause();
        isPlaying = false;
    }

    public void stopAudiobook() {
        player.stop();
        stopForeground(true);
        isNotificationShowing = false;
        isPlaying = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    public void setSpeed(float speed) {
        player.setPlaybackSpeed(speed);
    }

    public void seekTo(int position) {
        player.skipTo(position);
    }

    public AudiobookPlayer.AudiobookPlayerState getState() {
        return player.getState();
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.stop();
        }
        isNotificationShowing = false;
        progressHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private void createNotificationChannel(){ //creates a notification
        //Log.d(TAG,"CreateNotificationChannel called");
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
            CharSequence name ="Audiobook playing";
            String description = "An audiobook is currently playing";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}