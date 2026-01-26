package com.felipeplazas.zzztimerpro.ui.timer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.felipeplazas.zzztimerpro.R

class BreathingPacerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.primary_gold)
        alpha = 200
    }

    private val outerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = ContextCompat.getColor(context, R.color.primary_gold)
        alpha = 100
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 48f
        color = ContextCompat.getColor(context, R.color.white)
    }

    private val bounds = RectF()
    private var currentRadius = 0f
    private var targetRadius = 0f
    private var maxRadius = 0f
    private var minRadius = 0f

    private var currentPhase = BreathingPhase.INHALE
    private var phaseText = "Inhale"

    enum class BreathingPhase {
        INHALE, HOLD, EXHALE, PAUSE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val centerX = w / 2f
        val centerY = h / 2f
        val padding = 50f

        maxRadius = (Math.min(w, h) / 2f) - padding
        minRadius = maxRadius * 0.4f
        currentRadius = minRadius
        targetRadius = minRadius

        bounds.set(
            centerX - maxRadius,
            centerY - maxRadius,
            centerX + maxRadius,
            centerY + maxRadius
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Draw outer circle (guide)
        canvas.drawCircle(centerX, centerY, maxRadius, outerCirclePaint)

        // Draw breathing circle
        canvas.drawCircle(centerX, centerY, currentRadius, circlePaint)

        // Draw phase text
        canvas.drawText(phaseText, centerX, centerY + 16f, textPaint)
    }

    fun setBreathingPhase(phase: BreathingPhase) {
        currentPhase = phase
        phaseText = when (phase) {
            BreathingPhase.INHALE -> "Inhale"
            BreathingPhase.HOLD -> "Hold"
            BreathingPhase.EXHALE -> "Exhale"
            BreathingPhase.PAUSE -> "Pause"
        }

        targetRadius = when (phase) {
            BreathingPhase.INHALE -> maxRadius
            BreathingPhase.HOLD -> maxRadius
            BreathingPhase.EXHALE -> minRadius
            BreathingPhase.PAUSE -> minRadius
        }
    }

    fun animateToPhase(progress: Float) {
        // Smooth interpolation between current and target radius
        currentRadius = currentRadius + (targetRadius - currentRadius) * progress
        invalidate()
    }

    fun setProgress(progress: Float) {
        val range = targetRadius - currentRadius
        currentRadius = minRadius + (range * progress).coerceIn(0f, maxRadius - minRadius)
        invalidate()
    }
}

