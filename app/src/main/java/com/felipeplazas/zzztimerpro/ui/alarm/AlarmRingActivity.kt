package com.felipeplazas.zzztimerpro.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.databinding.ActivityAlarmRingBinding
import com.felipeplazas.zzztimerpro.utils.LogExt
import kotlin.random.Random

class AlarmRingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmRingBinding
    private var mediaPlayer: MediaPlayer? = null
    private var alarmId: Long = -1
    private var alarmName: String = ""
    private var mathChallengeEnabled: Boolean = false
    private var vibrationEnabled: Boolean = true

    private var currentProblem: MathProblem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Dismiss keyguard (minSdk >= 26)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        binding = ActivityAlarmRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start floating star animations
        com.felipeplazas.zzztimerpro.utils.StarAnimationHelper.startStarAnimations(this)

        // Get alarm data from intent
        alarmId = intent.getLongExtra("ALARM_ID", -1)
        alarmName = intent.getStringExtra("ALARM_NAME") ?: getString(R.string.alarm_time)
        mathChallengeEnabled = intent.getBooleanExtra("MATH_CHALLENGE", false)
        vibrationEnabled = intent.getBooleanExtra("VIBRATION", true)
        val soundResId = intent.getIntExtra("SOUND_RES_ID", -1)

        binding.alarmNameText.text = alarmName

        // Start alarm sound
        startAlarm(soundResId)

        // Start vibration
        if (vibrationEnabled) {
            startVibration()
        }

        // Setup dismiss button
        if (mathChallengeEnabled) {
            setupMathChallenge()
        } else {
            binding.dismissButton.setOnClickListener {
                dismissAlarm()
                LogExt.logStructured(
                    tag = "RING",
                    phase = "USER_ACTION",
                    event = "dismiss",
                    metrics = mapOf("alarm_id" to alarmId)
                )
            }
        }

        // Setup snooze button
        binding.snoozeButton.setOnClickListener {
            snoozeAlarm()
            LogExt.logStructured(
                tag = "RING",
                phase = "USER_ACTION",
                event = "snooze",
                metrics = mapOf("alarm_id" to alarmId)
            )
        }

        // Add back press callback
        onBackPressedDispatcher.addCallback(this) {
            if (!mathChallengeEnabled) {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        LogExt.logStructured(
            tag = "RING",
            phase = "INIT",
            event = "onCreate",
            metrics = mapOf(
                "alarm_id" to alarmId,
                "math" to mathChallengeEnabled,
                "vibration" to vibrationEnabled
            )
        )
    }

    private fun startAlarm(soundResId: Int) {
        try {
            val uri = if (soundResId > 0) {
                "android.resource://$packageName/$soundResId".toUri()
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmRingActivity, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 1000, 500, 1000, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(pattern, 0)
            }
        }
    }

    private fun setupMathChallenge() {
        binding.dismissButton.text = getString(R.string.solve_to_dismiss)
        binding.mathChallengeLayout.visibility = android.view.View.VISIBLE

        generateNewProblem()

        LogExt.logStructured(
            tag = "RING",
            phase = "USER_ACTION",
            event = "math_challenge_init",
            metrics = mapOf("alarm_id" to alarmId)
        )

        binding.dismissButton.setOnClickListener {
            val answer = binding.mathAnswerInput.text.toString().toIntOrNull()
            if (answer != null && answer == currentProblem?.answer) {
                LogExt.logStructured(
                    tag = "RING",
                    phase = "USER_ACTION",
                    event = "math_correct",
                    metrics = mapOf("alarm_id" to alarmId)
                )
                dismissAlarm()
            } else {
                LogExt.logStructured(
                    tag = "RING",
                    phase = "USER_ACTION",
                    event = "math_incorrect",
                    metrics = mapOf(
                        "alarm_id" to alarmId,
                        "expected" to (currentProblem?.answer ?: -1)
                    )
                )
                binding.mathErrorText.visibility = android.view.View.VISIBLE
                binding.mathAnswerInput.text?.clear()
                generateNewProblem()
            }
        }
    }

    private fun generateNewProblem() {
        val num1 = Random.nextInt(10, 50)
        val num2 = Random.nextInt(10, 50)
        val operation = Random.nextInt(0, 2) // 0 = add, 1 = subtract

        currentProblem = when (operation) {
            0 -> MathProblem("$num1 + $num2", num1 + num2)
            else -> MathProblem("$num1 - $num2", num1 - num2)
        }

        binding.mathProblemText.text = getString(R.string.solve_math_problem_with, currentProblem?.question ?: "")
        binding.mathErrorText.visibility = android.view.View.GONE
    }

    private fun dismissAlarm() {
        stopAlarmAndVibration()
        finish()

        LogExt.logStructured(
            tag = "RING",
            phase = "SHUTDOWN",
            event = "dismiss_finish",
            metrics = mapOf("alarm_id" to alarmId)
        )
    }

    private fun snoozeAlarm() {
        stopAlarmAndVibration()
        // TODO: Implement snooze functionality
        finish()

        LogExt.logStructured(
            tag = "RING",
            phase = "SHUTDOWN",
            event = "snooze_finish",
            metrics = mapOf("alarm_id" to alarmId)
        )
    }

    private fun stopAlarmAndVibration() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        // No field vibrator to cancel; system waveform auto finishes
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmAndVibration()

        LogExt.logStructured(
            tag = "RING",
            phase = "SHUTDOWN",
            event = "onDestroy",
            metrics = mapOf("alarm_id" to alarmId)
        )
    }

    private data class MathProblem(
        val question: String,
        val answer: Int
    )
}
