package com.felipeplazas.zzztimerpro.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateDecelerateInterpolator

class BreathingLotusView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var progress = 0f // 0.0 (closed) to 1.0 (fully open/inhaled)
    
    // Aesthetic Colors (Lotus Pink/Gold/White mix)
    private val petalColorRaw = Color.parseColor("#80FFF176") // Translucent Gold
    private val coreColorRaw = Color.parseColor("#CCFFFFFF") // White Core

    init {
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = (Math.min(width, height) / 2f) * 0.8f
        
        // Draw 8 Petals (Rotated Ovals)
        val petals = 8
        val baseScale = 0.5f + (0.5f * progress) // Scales from 50% to 100%
        val petalLen = maxRadius * baseScale
        val petalWidth = petalLen * 0.6f
        
        // Rotation blooms as it expands
        val bloomRotation = 45f * progress 

        // Draw multiple layers for "Ultra Premium" feel
        for (i in 0 until petals) {
            canvas.save()
            val angle = (360f / petals) * i + bloomRotation
            canvas.rotate(angle, cx, cy)
            
            paint.color = petalColorRaw
            // Draw petal (oval) offset from center
            val offset = (petalLen * 0.2f) * progress
            canvas.drawOval(
                cx - petalWidth / 2f, 
                cy - petalLen - offset, 
                cx + petalWidth / 2f, 
                cy - offset, 
                paint
            )
            canvas.restore()
        }
        
        // Draw Glowing Core
        paint.color = coreColorRaw
        val coreSize = (maxRadius * 0.3f) + (maxRadius * 0.1f * progress)
        canvas.drawCircle(cx, cy, coreSize, paint)
    }

    fun setProgress(value: Float) {
        this.progress = value.coerceIn(0f, 1f)
        invalidate()
    }
}
