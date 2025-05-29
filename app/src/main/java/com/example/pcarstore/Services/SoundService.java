package com.example.pcarstore.Services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.example.pcarstore.R;

public class SoundService extends Service {
    private MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Service no vinculado
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializar MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.notification_sound);
        mediaPlayer.setLooping(false); // No repetir
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Reproducir el sonido cuando se inicia el Service
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        return START_STICKY; // El Service se reinicia si es eliminado por el sistema
    }
    @Override
    public void onDestroy() {
        // Liberar recursos al detener el Service
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
