package com.felipeplazas.zzztimerpro.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.media.AudioManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import androidx.annotation.OptIn
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.felipeplazas.zzztimerpro.utils.LogExt

/**
 * Audio Service using Dual ExoPlayers for GAPLESS CROSSFADE LOOPING.
 * This fixes silence gaps in imperfect audio files by overlapping the end
 * of one loop with the start of the next.
 */
class AudioService : Service() {
    
    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SET_VOLUME = "ACTION_SET_VOLUME"
        const val ACTION_SET_LOOP = "ACTION_SET_LOOP"
        const val EXTRA_AUDIO_RES_ID = "EXTRA_AUDIO_RES_ID"
        const val EXTRA_VOLUME = "EXTRA_VOLUME"
        const val EXTRA_LOOP = "EXTRA_LOOP"
        
        const val BROADCAST_PLAYBACK_STATE = "com.felipeplazas.zzztimerpro.PLAYBACK_STATE"
        const val EXTRA_IS_PLAYING = "EXTRA_IS_PLAYING"
        const val EXTRA_CURRENT_SOUND_ID = "EXTRA_CURRENT_SOUND_ID"
        
        private const val CROSSFADE_DURATION_MS = 2000L // 2 seconds overlap
        private const val NOTIFICATION_ID = 1003
    }
    
    private val binder = AudioBinder()
    
    // Dual players for crossfade
    private var player1: ExoPlayer? = null
    private var player2: ExoPlayer? = null
    private var currentPlayer: ExoPlayer? = null
    private var nextPlayer: ExoPlayer? = null
    
    private var currentSoundResId: Int? = null
    private var isPlaying = false
    private var currentVolume = 1.0f
    private var isLooping = true
    
    private val handler = Handler(Looper.getMainLooper())
    private var crossfadeRunnable: Runnable? = null
    
    // Audio focus logic...
    private lateinit var audioManager: AudioManager
    private var audioFocusGranted = false
    private var audioFocusRequest: AudioFocusRequest? = null
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseSound()
            AudioManager.AUDIOFOCUS_GAIN -> if (!isPlaying && currentSoundResId != null) startPlayback()
        }
    }

    inner class AudioBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }
    
    override fun onCreate() {
        super.onCreate()
        setupAudioFocus()
        
        // Initialize two players
        player1 = createPlayer()
        player2 = createPlayer()
        currentPlayer = player1
        nextPlayer = player2
    }
    
    private fun createPlayer(): ExoPlayer {
        return ExoPlayer.Builder(this).build().apply {
            volume = currentVolume
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }
    
    private fun setupAudioFocus() {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener).build()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val audioResId = intent.getIntExtra(EXTRA_AUDIO_RES_ID, -1)
                if (audioResId != -1) playSound(audioResId)
            }
            ACTION_PAUSE -> pauseSound()
            ACTION_STOP -> stopSound()
            ACTION_SET_VOLUME -> setVolume(intent.getFloatExtra(EXTRA_VOLUME, 1.0f))
            ACTION_SET_LOOP -> isLooping = intent.getBooleanExtra(EXTRA_LOOP, true)
        }
        return START_NOT_STICKY
    }

    private fun playSound(@RawRes audioResId: Int) {
        if (currentSoundResId == audioResId && isPlaying) return
        
        stopSound() // Reset
        if (!requestAudioFocus()) return

        currentSoundResId = audioResId
        isPlaying = true
        
        startPlayback()
    }
    
    private fun startPlayback() {
        val resId = currentSoundResId ?: return
        val uri = "android.resource://${packageName}/${resId}"
        val mediaItem = MediaItem.fromUri(uri)
        
        // Start Foreground Service to prevent killing
        startForegroundService()

        currentPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        
        if (isLooping) {
            scheduleNextLoop(currentPlayer!!)
        }
        
        broadcastPlaybackState()
    }
    
    private fun startForegroundService() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= 34) {
             startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
             startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(): android.app.Notification {
        return androidx.core.app.NotificationCompat.Builder(this, com.felipeplazas.zzztimerpro.ZzzTimerApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(com.felipeplazas.zzztimerpro.R.string.audio_playing))
            .setContentText(getString(com.felipeplazas.zzztimerpro.R.string.app_name))
            .setSmallIcon(com.felipeplazas.zzztimerpro.R.drawable.ic_music_note)
            .setOngoing(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun scheduleNextLoop(activePlayer: ExoPlayer) {
        // Wait for duration to be known
        handler.postDelayed(object : Runnable {
            override fun run() {
                val duration = activePlayer.duration
                if (duration <= 0) {
                    handler.postDelayed(this, 100) // Retry
                    return
                }
                
                // Schedule next player start at (duration - crossfade)
                val delay = duration - CROSSFADE_DURATION_MS
                if (delay > 0) {
                    crossfadeRunnable = Runnable {
                        startNextPlayer()
                    }
                    handler.postDelayed(crossfadeRunnable!!, delay)
                }
            }
        }, 100)
    }
    
    private fun startNextPlayer() {
        if (!isPlaying || currentSoundResId == null) return
        
        // swap players
        val temp = currentPlayer
        currentPlayer = nextPlayer
        nextPlayer = temp
        
        val uri = "android.resource://${packageName}/${currentSoundResId}"
        currentPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            volume = currentVolume // Ensure full volume
            prepare()
            play()
        }
        
        // Schedule next loop
        scheduleNextLoop(currentPlayer!!)
    }

    private fun pauseSound() {
        currentPlayer?.pause()
        nextPlayer?.stop()
        handler.removeCallbacksAndMessages(null)
        isPlaying = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        broadcastPlaybackState()
    }
    
    private fun stopSound() {
        player1?.stop()
        player2?.stop()
        handler.removeCallbacksAndMessages(null)
        isPlaying = false
        currentSoundResId = null
        abandonAudioFocus()
        stopForeground(STOP_FOREGROUND_REMOVE)
        broadcastPlaybackState()
    }
    
    private fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        player1?.volume = currentVolume
        player2?.volume = currentVolume
    }

    private fun requestAudioFocus(): Boolean {
        // Simplified focus request
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED } ?: false
        } else {
             authFocusLegacy()
        }
    }
    
    @Suppress("DEPRECATION")
    private fun authFocusLegacy(): Boolean {
        return audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }

    private fun broadcastPlaybackState() {
        /* ... same broadcast logic ... */
        val intent = Intent(BROADCAST_PLAYBACK_STATE).apply {
            putExtra(EXTRA_IS_PLAYING, isPlaying)
            putExtra(EXTRA_CURRENT_SOUND_ID, currentSoundResId ?: -1)
        }
        sendBroadcast(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        player1?.release()
        player2?.release()
        handler.removeCallbacksAndMessages(null)
    }
    
    // Public methods
    fun isPlaying() = isPlaying
    fun getCurrentSoundResId() = currentSoundResId
    fun getCurrentVolume() = currentVolume
    fun isLooping() = isLooping
}
