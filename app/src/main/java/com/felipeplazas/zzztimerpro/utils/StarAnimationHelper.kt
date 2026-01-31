package com.felipeplazas.zzztimerpro.utils

import android.app.Activity
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.felipeplazas.zzztimerpro.R

/**
 * Utility to start floating twinkle animations on star decorations
 * Now with actual floating movement for ULTRA MEGA PREMIUM effect
 */
object StarAnimationHelper {
    
    /**
     * Starts floating animations on all stars in the activity
     * Supports up to 20 stars for maximum premium atmosphere
     */
    fun startStarAnimations(activity: Activity) {
        val stars = listOf(
            R.id.starFloat1 to R.anim.anim_star_float,
            R.id.starFloat2 to R.anim.anim_star_float_alt,
            R.id.starFloat3 to R.anim.anim_star_float_slow,
            R.id.starFloat4 to R.anim.anim_star_float,
            R.id.starFloat5 to R.anim.anim_star_float_alt,
            R.id.starFloat6 to R.anim.anim_star_float_slow,
            R.id.starFloat7 to R.anim.anim_star_float,
            R.id.starFloat8 to R.anim.anim_star_float_alt,
            R.id.starFloat9 to R.anim.anim_star_float_slow,
            R.id.starFloat10 to R.anim.anim_star_float,
            R.id.starFloat11 to R.anim.anim_star_float,
            R.id.starFloat12 to R.anim.anim_star_float_alt,
            R.id.starFloat13 to R.anim.anim_star_float_slow,
            R.id.starFloat14 to R.anim.anim_star_float,
            R.id.starFloat15 to R.anim.anim_star_float_alt,
            R.id.starFloat16 to R.anim.anim_star_float_slow,
            R.id.starFloat17 to R.anim.anim_star_float,
            R.id.starFloat18 to R.anim.anim_star_float_alt,
            R.id.starFloat19 to R.anim.anim_star_float_slow,
            R.id.starFloat20 to R.anim.anim_star_float,
            R.id.starFloat21 to R.anim.anim_star_float_alt,
            R.id.starFloat22 to R.anim.anim_star_float_slow,
            R.id.starFloat23 to R.anim.anim_star_float,
            R.id.starFloat24 to R.anim.anim_star_float_alt,
            R.id.starFloat25 to R.anim.anim_star_float_slow,
            R.id.starFloat26 to R.anim.anim_star_float,
            R.id.starFloat27 to R.anim.anim_star_float_alt,
            R.id.starFloat28 to R.anim.anim_star_float_slow,
            R.id.starFloat29 to R.anim.anim_star_float,
            R.id.starFloat30 to R.anim.anim_star_float_alt,
            R.id.starFloat31 to R.anim.anim_star_float_slow,
            R.id.starFloat32 to R.anim.anim_star_float,
            R.id.starFloat33 to R.anim.anim_star_float_alt,
            R.id.starFloat34 to R.anim.anim_star_float_slow,
            R.id.starFloat35 to R.anim.anim_star_float,
            R.id.starFloat36 to R.anim.anim_star_float_alt,
            R.id.starFloat37 to R.anim.anim_star_float_slow,
            R.id.starFloat38 to R.anim.anim_star_float,
            R.id.starFloat39 to R.anim.anim_star_float_alt,
            R.id.starFloat40 to R.anim.anim_star_float_slow,
            
            // Floating Planet B-612
            R.id.planetB612 to R.anim.anim_planet_float
        )
        
        stars.forEach { (starId, animId) ->
            activity.findViewById<ImageView>(starId)?.let { star ->
                val animation = AnimationUtils.loadAnimation(activity, animId)
                star.startAnimation(animation)
            }
        }
    }
}
