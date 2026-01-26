package com.felipeplazas.zzztimerpro.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.random.Random

/**
 * A Premium "Little Prince" inspired view that draws twinkling stars and floating planets.
 * Adds a subtle, high-quality ambience to the Timer screen.
 */
class StarrySkyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class Star(
        var x: Float,
        var y: Float,
        var size: Float,
        var alpha: Int,
        var alphaSpeed: Int,
        var growing: Boolean
    )

    private val stars = mutableListOf<Star>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    
    private val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.FILL
    }

    private var animator: ValueAnimator? = null
    private val random = Random(System.currentTimeMillis())

    init {
        // Create 100 stars
        post {
            createStars(width, height)
            startAnimation()
        }
    }

    private fun createStars(w: Int, h: Int) {
        stars.clear()
        for (i in 0 until 100) {
            stars.add(
                Star(
                    x = random.nextFloat() * w,
                    y = random.nextFloat() * h,
                    size = random.nextFloat() * 6f + 2f, // 2 to 8px
                    alpha = random.nextInt(100, 255),
                    alphaSpeed = random.nextInt(2, 8),
                    growing = random.nextBoolean()
                )
            )
        }
        // Add a few "Gold" stars (Planets/Asteroids representation)
         for (i in 0 until 5) {
            stars.add(
                Star(
                    x = random.nextFloat() * w,
                    y = random.nextFloat() * h,
                    size = random.nextFloat() * 12f + 8f, // Larger
                    alpha = 255,
                    alphaSpeed = 0, // Steady
                    growing = false
                ).apply { 
                    // Use alpha field to tag as gold (hacky but acts as type)
                    alpha = -1 
                }
            )
        }
    }

    private fun startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 16 // ~60fps
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                updateStars()
                invalidate()
            }
            start()
        }
    }

    private fun updateStars() {
        stars.forEach { star ->
            if (star.alpha == -1) {
                // Gold Planet/Asteroid: Slow rise
                star.y -= 0.08f
                if (star.y < -100) {
                    star.y = height + 100f
                    star.x = random.nextFloat() * width
                }
            } else {
                // Normal star: Twinkle logic
                if (star.growing) {
                    star.alpha += star.alphaSpeed
                    if (star.alpha >= 255) {
                        star.alpha = 255
                        star.growing = false
                    }
                } else {
                    star.alpha -= star.alphaSpeed
                    if (star.alpha <= 50) { // Don't disappear completely
                        star.alpha = 50
                        star.growing = true
                    }
                }
                
                // Slow drift
                star.y -= 0.05f * (star.size / 2f)
                if (star.y < 0) {
                    star.y = height.toFloat()
                    star.x = random.nextFloat() * width
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // No clear; transparent

        stars.forEach { star ->
            if (star.alpha == -1) {
                // Draw Planet B-612 representation (Circle with subtle crater details)
                goldPaint.alpha = 255
                canvas.drawCircle(star.x, star.y, star.size, goldPaint)
                // Draw a small "crater" shadow on the planet
                paint.color = Color.parseColor("#CCFFA000") // Darker gold
                canvas.drawCircle(star.x - (star.size * 0.3f), star.y - (star.size * 0.3f), star.size * 0.25f, paint)
                paint.color = Color.WHITE // Reset
            } else {
                paint.alpha = star.alpha
                canvas.drawCircle(star.x, star.y, star.size, paint)
                // Draw "glow" for larger stars
                if (star.size > 4f) {
                    paint.alpha = star.alpha / 4
                    canvas.drawCircle(star.x, star.y, star.size * 2, paint)
                }
            }
        }
        
        // Draw Planet Horizon (Bottom of screen)
        drawHorizon(canvas)
    }
    
    private fun drawHorizon(canvas: Canvas) {
        val path = android.graphics.Path()
        // Start from bottom left
        path.moveTo(0f, height.toFloat())
        // Curve up to center
        path.cubicTo(
            width * 0.2f, height * 0.95f, 
            width * 0.8f, height * 0.9f, 
            width.toFloat(), height * 0.95f
        )
        // Close shape
        path.lineTo(width.toFloat(), height.toFloat())
        path.close() // Close to bottom right then bottom left implicitly
        
        // Draw with dark planet color
        val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#151B38") // Slightly lighter than bg
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, planetPaint)
        
        // Draw subtle highlight on horizon
        planetPaint.style = Paint.Style.STROKE
        planetPaint.strokeWidth = 2f
        planetPaint.color = Color.parseColor("#304060")
        canvas.drawPath(path, planetPaint)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (stars.isEmpty()) createStars(w, h)
    }
}
