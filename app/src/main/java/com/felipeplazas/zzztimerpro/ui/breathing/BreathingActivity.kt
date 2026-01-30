package com.felipeplazas.zzztimerpro.ui.breathing

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.databinding.ActivityBreathingBinding

class BreathingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBreathingBinding
    private var isRunning = false
    private var currentPhase = BreathingPhase.READY
    private var currentCycle = 0
    private var timer: CountDownTimer? = null
    private var animator: android.animation.ValueAnimator? = null
    private var selectedTechnique: BreathingTechnique = BreathingTechnique.TECHNIQUE_478

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Start floating star animations
        com.felipeplazas.zzztimerpro.utils.StarAnimationHelper.startStarAnimations(this)

        setupToolbar()
        setupTechniqueSelector()
        
        binding.startButton.setOnClickListener {
            com.felipeplazas.zzztimerpro.utils.HapticHelper.confirm(it)
            if (isRunning) stopExercise() else startExercise()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.breathing_exercises)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
    
    // No lottie setup needed

    private fun setupTechniqueSelector() {
        binding.technique478.setOnClickListener { 
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            selectTechnique(BreathingTechnique.TECHNIQUE_478)
        }
        binding.techniqueBox.setOnClickListener { 
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            selectTechnique(BreathingTechnique.BOX_BREATHING)
        }
        binding.techniqueCalm.setOnClickListener { 
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            selectTechnique(BreathingTechnique.CALM_BREATHING)
        }
        binding.techniqueEnergizing.setOnClickListener { 
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            selectTechnique(BreathingTechnique.ENERGIZING)
        }
        binding.techniqueCpapAdapt.setOnClickListener { 
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            selectTechnique(BreathingTechnique.CPAP_ADAPT)
        }
        binding.techniqueCpapResist.setOnClickListener { 
            com.felipeplazas.zzztimerpro.utils.HapticHelper.lightTick(it)
            selectTechnique(BreathingTechnique.CPAP_RESIST)
        }
        selectTechnique(BreathingTechnique.TECHNIQUE_478)
    }

    private fun selectTechnique(technique: BreathingTechnique) {
        selectedTechnique = technique

        binding.technique478.strokeWidth = 0
        binding.techniqueBox.strokeWidth = 0
        binding.techniqueCalm.strokeWidth = 0
        binding.techniqueEnergizing.strokeWidth = 0
        binding.techniqueCpapAdapt.strokeWidth = 0
        binding.techniqueCpapResist.strokeWidth = 0

        val selectedCard = when (technique) {
            BreathingTechnique.TECHNIQUE_478 -> binding.technique478
            BreathingTechnique.BOX_BREATHING -> binding.techniqueBox
            BreathingTechnique.CALM_BREATHING -> binding.techniqueCalm
            BreathingTechnique.ENERGIZING -> binding.techniqueEnergizing
            BreathingTechnique.CPAP_ADAPT -> binding.techniqueCpapAdapt
            BreathingTechnique.CPAP_RESIST -> binding.techniqueCpapResist
        }
        selectedCard.strokeWidth = 5
        selectedCard.strokeColor = getColor(R.color.primary_gold)
    }

    private fun startExercise() {
        isRunning = true
        currentCycle = 0
        binding.startButton.text = getString(R.string.stop)
        
        // UI State
        binding.techniqueSelector.visibility = View.GONE
        binding.lotusView.visibility = View.VISIBLE
        binding.lotusView.setProgress(0f) 
        
        startNextPhase()
    }

    private fun stopExercise() {
        isRunning = false
        timer?.cancel()
        animator?.cancel()
        timer = null
        animator = null
        
        binding.startButton.text = getString(R.string.start)
        
        binding.techniqueSelector.visibility = View.VISIBLE
        binding.lotusView.visibility = View.GONE
        
        binding.instructionText.text = getString(R.string.ready_to_begin)
        binding.timerText.text = ""
        binding.cycleCounter.text = ""
    }

    private fun startNextPhase() {
        if (!isRunning) return

        val pair = getNextPhase()
        currentPhase = pair.first
        val durationSec = pair.second

        binding.instructionText.text = when (currentPhase) {
            BreathingPhase.INHALE -> "Breathe In"
            BreathingPhase.HOLD -> "Hold"
            BreathingPhase.EXHALE -> "Breathe Out"
            BreathingPhase.REST -> "Rest"
            else -> "Ready"
        }
        binding.timerText.text = durationSec.toString()
        
        animateLotus(currentPhase, durationSec)

        timer?.cancel()
        timer = object : CountDownTimer((durationSec * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isRunning) { cancel(); return }
                val secondsLeft = (millisUntilFinished / 1000) + 1
                binding.timerText.text = secondsLeft.toString()
            }

            override fun onFinish() {
                if (!isRunning) return
                
                // Count cycles when a full breath completes
                var cycleCompleted = false
                when (selectedTechnique) {
                    BreathingTechnique.TECHNIQUE_478 -> {
                        if (currentPhase == BreathingPhase.EXHALE) cycleCompleted = true
                    }
                    BreathingTechnique.BOX_BREATHING -> {
                        if (currentPhase == BreathingPhase.REST) cycleCompleted = true
                    }
                    BreathingTechnique.CALM_BREATHING, BreathingTechnique.ENERGIZING,
                    BreathingTechnique.CPAP_ADAPT, BreathingTechnique.CPAP_RESIST -> {
                        if (currentPhase == BreathingPhase.EXHALE) cycleCompleted = true
                    }
                }
                
                if (cycleCompleted) {
                    currentCycle++
                    binding.cycleCounter.text = "Cycles: $currentCycle"
                }
                
                startNextPhase()
            }
        }.start()
    }

    private fun getNextPhase(): Pair<BreathingPhase, Int> {
        return when (selectedTechnique) {
            BreathingTechnique.TECHNIQUE_478 -> when (currentPhase) {
                BreathingPhase.READY, BreathingPhase.EXHALE -> Pair(BreathingPhase.INHALE, 4)
                BreathingPhase.INHALE -> Pair(BreathingPhase.HOLD, 7)
                BreathingPhase.HOLD -> Pair(BreathingPhase.EXHALE, 8)
                else -> Pair(BreathingPhase.INHALE, 4)
            }
            BreathingTechnique.BOX_BREATHING -> when (currentPhase) {
                BreathingPhase.READY, BreathingPhase.REST -> Pair(BreathingPhase.INHALE, 4)
                BreathingPhase.INHALE -> Pair(BreathingPhase.HOLD, 4)
                BreathingPhase.HOLD -> Pair(BreathingPhase.EXHALE, 4)
                BreathingPhase.EXHALE -> Pair(BreathingPhase.REST, 4) // Hold after exhale
                else -> Pair(BreathingPhase.INHALE, 4)
            }
            BreathingTechnique.CALM_BREATHING -> when (currentPhase) {
                BreathingPhase.READY, BreathingPhase.EXHALE -> Pair(BreathingPhase.INHALE, 4)
                BreathingPhase.INHALE -> Pair(BreathingPhase.EXHALE, 6)
                else -> Pair(BreathingPhase.INHALE, 4)
            }
            BreathingTechnique.ENERGIZING -> when (currentPhase) {
                // Description: "Quick inhale for 2 seconds, exhale for 4"
                BreathingPhase.READY, BreathingPhase.EXHALE -> Pair(BreathingPhase.INHALE, 2)
                BreathingPhase.INHALE -> Pair(BreathingPhase.EXHALE, 4)
                else -> Pair(BreathingPhase.INHALE, 2)
            }
            // CPAP-specific techniques
            BreathingTechnique.CPAP_ADAPT -> when (currentPhase) {
                // Mask adaptation: Inhale 4s, Exhale 6s (matches CPAP pressure rhythm)
                BreathingPhase.READY, BreathingPhase.EXHALE -> Pair(BreathingPhase.INHALE, 4)
                BreathingPhase.INHALE -> Pair(BreathingPhase.EXHALE, 6)
                else -> Pair(BreathingPhase.INHALE, 4)
            }
            BreathingTechnique.CPAP_RESIST -> when (currentPhase) {
                // Resistance training: Deep inhale 5s, controlled exhale 5s
                BreathingPhase.READY, BreathingPhase.EXHALE -> Pair(BreathingPhase.INHALE, 5)
                BreathingPhase.INHALE -> Pair(BreathingPhase.EXHALE, 5)
                else -> Pair(BreathingPhase.INHALE, 5)
            }
        }
    }

    private fun animateLotus(phase: BreathingPhase, durationSec: Int) {
        val durationMs = (durationSec * 1000).toLong()
        animator?.cancel()
        
        // Inhale: 0 -> 1
        // Exhale: 1 -> 0
        // Hold/Rest: Pulse around current state
        
        val startVal = when(phase) {
            BreathingPhase.INHALE -> 0f
            BreathingPhase.EXHALE -> 1f
            else -> if (currentPhase == BreathingPhase.HOLD) 1f else 0f
        }
        
        val endVal = when(phase) {
            BreathingPhase.INHALE -> 1f
            BreathingPhase.EXHALE -> 0f
            else -> startVal // Just pulse
        }

        if (phase == BreathingPhase.HOLD || phase == BreathingPhase.REST) {
            // Pulse Animation
            animator = android.animation.ValueAnimator.ofFloat(startVal, startVal * 0.9f, startVal).apply {
                duration = 2000
                repeatCount = android.animation.ValueAnimator.INFINITE
                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                addUpdateListener { 
                    binding.lotusView.setProgress(it.animatedValue as Float)
                }
            }
        } else {
            // Expansion/Contraction
            animator = android.animation.ValueAnimator.ofFloat(startVal, endVal).apply {
                duration = durationMs
                interpolator = android.view.animation.DecelerateInterpolator()
                addUpdateListener { 
                    binding.lotusView.setProgress(it.animatedValue as Float)
                }
            }
        }
        animator?.start()
    }

    private enum class BreathingPhase { READY, INHALE, HOLD, EXHALE, REST }
    private enum class BreathingTechnique { 
        TECHNIQUE_478, BOX_BREATHING, CALM_BREATHING, ENERGIZING,
        CPAP_ADAPT, CPAP_RESIST
    }
}

