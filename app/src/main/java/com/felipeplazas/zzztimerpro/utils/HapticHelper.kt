package com.felipeplazas.zzztimerpro.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Utility class for subtle haptic feedback throughout the app.
 * Provides very light vibration for button presses and interactions.
 */
object HapticHelper {
    
    /**
     * Provides a very subtle haptic tick (like a light tap).
     * Used for button presses, toggles, and selection changes.
     */
    fun lightTick(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * Provides a subtle confirmation feedback.
     * Used for successful actions, confirmations.
     */
    fun confirm(view: View) {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONFIRM,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
    
    /**
     * Provides a very subtle vibration using the Vibrator service.
     * Fallback for custom duration.
     * 
     * @param context Application context
     * @param durationMs Duration in milliseconds (default 10ms - very subtle)
     */
    fun subtleVibrate(context: Context, durationMs: Long = 10L) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.EFFECT_TICK)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    }
    
    /**
     * Extension function for Views to easily add haptic feedback on click.
     */
    fun View.setHapticClickListener(listener: View.OnClickListener) {
        setOnClickListener { v ->
            lightTick(v)
            listener.onClick(v)
        }
    }
}
