package com.felipeplazas.zzztimerpro.ui.sounds

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.AmbientSound
import com.felipeplazas.zzztimerpro.data.repository.SoundRepository
import com.felipeplazas.zzztimerpro.databinding.ActivityAmbientSoundsBinding
import com.felipeplazas.zzztimerpro.services.AudioService
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import com.felipeplazas.zzztimerpro.license.LicenseManager
import android.widget.Toast

class AmbientSoundsActivity : BaseActivity() {
    
    private lateinit var binding: ActivityAmbientSoundsBinding
    private lateinit var soundAdapter: SoundAdapter
    private lateinit var licenseManager: LicenseManager
    private var audioService: AudioService? = null
    private var isServiceBound = false
    
    private var currentPlayingSound: AmbientSound? = null
    private var isPlaying = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioBinder
            audioService = binder.getService()
            isServiceBound = true
            updateUIFromService()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            isServiceBound = false
        }
    }
    
    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioService.BROADCAST_PLAYBACK_STATE) {
                isPlaying = intent.getBooleanExtra(AudioService.EXTRA_IS_PLAYING, false)
                val soundResId = intent.getIntExtra(AudioService.EXTRA_CURRENT_SOUND_ID, -1)
                
                if (soundResId != -1) {
                    currentPlayingSound = SoundRepository.getAllSounds().find { it.audioResId == soundResId }
                } else {
                    currentPlayingSound = null
                }
                
                updateNowPlayingCard()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmbientSoundsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        licenseManager = LicenseManager(this)
        
        setupToolbar()
        setupRecyclerView()
        setupNowPlayingControls()
        bindAudioService()
        registerPlaybackReceiver()
        com.felipeplazas.zzztimerpro.utils.StarAnimationHelper.startStarAnimations(this)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        val allSounds = SoundRepository.getAllSounds()
        
        // Limit to 1 sound for free version
        val sounds = if (licenseManager.isPremium()) {
            allSounds
        } else {
            allSounds.take(1) // Only first sound available in free version
        }
        
        soundAdapter = SoundAdapter(sounds) { sound ->
            onSoundClick(sound)
        }
        
        binding.recyclerViewSounds.apply {
            layoutManager = LinearLayoutManager(this@AmbientSoundsActivity)
            adapter = soundAdapter
        }
    }
    
    private fun setupNowPlayingControls() {
        binding.sliderVolume.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val volume = value / 100f
                sendVolumeCommand(volume)
            }
        }
        
        binding.switchLoop.setOnCheckedChangeListener { _, isChecked ->
            sendLoopCommand(isChecked)
        }
        
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseCurrentSound()
            } else {
                resumeCurrentSound()
            }
        }
        
        binding.btnStop.setOnClickListener {
            stopCurrentSound()
        }
    }
    
    private fun bindAudioService() {
        val intent = Intent(this, AudioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun registerPlaybackReceiver() {
        val filter = IntentFilter(AudioService.BROADCAST_PLAYBACK_STATE)
        registerReceiver(playbackStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }
    
    private fun onSoundClick(sound: AmbientSound) {
        // Check if sound is available in free version
        if (!licenseManager.isPremium() && sound.id > 1) {
            Toast.makeText(
                this,
                R.string.sound_locked_free_version,
                Toast.LENGTH_SHORT
            ).show()
            checkPremiumAccess()
            return
        }
        
        if (currentPlayingSound?.id == sound.id) {
            // Toggle play/pause for current sound
            if (isPlaying) {
                pauseCurrentSound()
            } else {
                resumeCurrentSound()
            }
        } else {
            // Play new sound
            playSound(sound)
        }
    }
    
    private fun playSound(sound: AmbientSound) {
        val intent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_PLAY
            putExtra(AudioService.EXTRA_AUDIO_RES_ID, sound.audioResId)
        }
        startService(intent)
        
        currentPlayingSound = sound
        isPlaying = true
        updateNowPlayingCard()
    }
    
    private fun pauseCurrentSound() {
        val intent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_PAUSE
        }
        startService(intent)
        
        isPlaying = false
        updateNowPlayingCard()
    }
    
    private fun resumeCurrentSound() {
        currentPlayingSound?.let { sound ->
            val intent = Intent(this, AudioService::class.java).apply {
                action = AudioService.ACTION_PLAY
                putExtra(AudioService.EXTRA_AUDIO_RES_ID, sound.audioResId)
            }
            startService(intent)
            
            isPlaying = true
            updateNowPlayingCard()
        }
    }
    
    private fun stopCurrentSound() {
        val intent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_STOP
        }
        startService(intent)
        
        currentPlayingSound = null
        isPlaying = false
        updateNowPlayingCard()
    }
    
    private fun sendVolumeCommand(volume: Float) {
        val intent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_SET_VOLUME
            putExtra(AudioService.EXTRA_VOLUME, volume)
        }
        startService(intent)
    }
    
    private fun sendLoopCommand(loop: Boolean) {
        val intent = Intent(this, AudioService::class.java).apply {
            action = AudioService.ACTION_SET_LOOP
            putExtra(AudioService.EXTRA_LOOP, loop)
        }
        startService(intent)
    }
    
    private fun updateNowPlayingCard() {
        if (currentPlayingSound != null) {
            binding.cardNowPlaying.visibility = View.VISIBLE
            binding.tvNowPlayingTitle.text = getString(currentPlayingSound!!.nameResId)
            
            if (isPlaying) {
                binding.btnPlayPause.text = getString(R.string.pause)
                binding.btnPlayPause.setIconResource(R.drawable.ic_pause)
            } else {
                binding.btnPlayPause.text = getString(R.string.play)
                binding.btnPlayPause.setIconResource(R.drawable.ic_play)
            }
            
            soundAdapter.setCurrentPlaying(currentPlayingSound?.id)
        } else {
            binding.cardNowPlaying.visibility = View.GONE
            soundAdapter.setCurrentPlaying(null)
        }
    }
    
    private fun updateUIFromService() {
        audioService?.let { service ->
            isPlaying = service.isPlaying()
            val currentResId = service.getCurrentSoundResId()
            
            if (currentResId != null) {
                currentPlayingSound = SoundRepository.getAllSounds().find { it.audioResId == currentResId }
            }
            
            val volume = service.getCurrentVolume()
            binding.sliderVolume.value = volume * 100
            
            val looping = service.isLooping()
            binding.switchLoop.isChecked = looping
            
            updateNowPlayingCard()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Prevenir memory leaks: unbind service y unregister receiver
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        try {
            unregisterReceiver(playbackStateReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver wasn't registered, ignore
        }
    }
}
