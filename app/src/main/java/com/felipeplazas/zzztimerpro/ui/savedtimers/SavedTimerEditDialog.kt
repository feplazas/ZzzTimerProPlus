package com.felipeplazas.zzztimerpro.ui.savedtimers

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.SavedTimer
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SavedTimerEditDialog(
    context: Context,
    private val timer: SavedTimer?,
    private val onSave: (SavedTimer) -> Unit
) : Dialog(context) {

    private lateinit var etName: EditText
    private lateinit var npDuration: NumberPicker
    private lateinit var btnSelectSound: MaterialButton
    private lateinit var tvSelectedSound: TextView
    
    private var selectedSoundResId: Int? = null
    private var selectedSoundName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_timer_edit, null)

        etName = view.findViewById(R.id.etTimerName)
        npDuration = view.findViewById(R.id.npDuration)
        btnSelectSound = view.findViewById(R.id.btnSelectSound)
        tvSelectedSound = view.findViewById(R.id.tvSelectedSound)

        // Setup duration picker
        npDuration.minValue = 5
        npDuration.maxValue = 120
        npDuration.value = timer?.durationMinutes ?: 30

        // Set existing values if editing
        timer?.let {
            etName.setText(it.name)
            selectedSoundResId = it.soundResId
            selectedSoundName = it.soundName
            updateSoundDisplay()
        }
        
        // Setup sound selector
        btnSelectSound.setOnClickListener {
            showSoundSelectionDialog()
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(if (timer == null) R.string.add_timer else R.string.edit_timer)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                saveTimer()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show()
    }
    
    private fun showSoundSelectionDialog() {
        val sounds = arrayOf(
            context.getString(R.string.soft_rain),
            context.getString(R.string.ocean_waves),
            context.getString(R.string.night_forest),
            context.getString(R.string.gentle_wind),
            context.getString(R.string.white_noise),
            context.getString(R.string.night_birds)
        )
        
        // Usar IDs genéricos (1-6) en lugar de R.raw
        val soundResIds = arrayOf(1, 2, 3, 4, 5, 6)
        
        val currentIndex = selectedSoundName?.let { name ->
            sounds.indexOf(name)
        } ?: -1
        
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.select_sound)
            .setSingleChoiceItems(sounds, currentIndex) { dialog, which ->
                selectedSoundResId = soundResIds[which]
                selectedSoundName = sounds[which]
                updateSoundDisplay()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun updateSoundDisplay() {
        if (selectedSoundName != null) {
            tvSelectedSound.text = context.getString(R.string.sound_selected, selectedSoundName)
        } else {
            tvSelectedSound.text = context.getString(R.string.no_sound_selected)
        }
    }

    private fun saveTimer() {
        val name = etName.text.toString().ifEmpty { "Timer" }
        val duration = npDuration.value

        val newTimer = SavedTimer(
            id = timer?.id ?: 0,
            name = name,
            durationMinutes = duration,
            soundResId = selectedSoundResId,
            soundName = selectedSoundName
        )

        onSave(newTimer)
    }
}

