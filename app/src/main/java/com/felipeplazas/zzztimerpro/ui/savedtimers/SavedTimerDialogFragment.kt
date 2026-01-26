package com.felipeplazas.zzztimerpro.ui.savedtimers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.SavedTimer
import com.felipeplazas.zzztimerpro.databinding.DialogSavedTimerEditBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SavedTimerDialogFragment : DialogFragment() {

    private var _binding: DialogSavedTimerEditBinding? = null
    private val binding get() = _binding!!

    private var editingTimer: SavedTimer? = null
    private var selectedSoundResId: Int? = null

    companion object {
        private const val ARG_TIMER = "timer"

        fun newInstance(timer: SavedTimer? = null): SavedTimerDialogFragment {
            return SavedTimerDialogFragment().apply {
                arguments = Bundle().apply {
                    timer?.let { putSerializable(ARG_TIMER, it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        editingTimer = arguments?.getSerializable(ARG_TIMER) as? SavedTimer
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSavedTimerEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        editingTimer?.let { timer ->
            binding.timerNameInput.setText(timer.name)
            binding.durationSlider.value = timer.durationMinutes.toFloat()
            binding.fadeSlider.value = timer.fadeDurationMinutes.toFloat()
            selectedSoundResId = timer.soundResId
        }

        updateDurationText(binding.durationSlider.value.toInt())
        updateFadeText(binding.fadeSlider.value.toInt())

        binding.durationSlider.addOnChangeListener { _, value, _ ->
            updateDurationText(value.toInt())
        }

        binding.fadeSlider.addOnChangeListener { _, value, _ ->
            updateFadeText(value.toInt())
        }
    }

    private fun setupClickListeners() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            saveTimer()
        }

        binding.soundSelector.setOnClickListener {
            // TODO: Show sound picker dialog
        }
    }

    private fun updateDurationText(minutes: Int) {
        binding.durationText.text = getString(R.string.minutes_format, minutes)
    }

    private fun updateFadeText(minutes: Int) {
        binding.fadeText.text = getString(R.string.minutes_format, minutes)
    }

    private fun saveTimer() {
        val name = binding.timerNameInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.timerNameInput.error = getString(R.string.timer_name)
            return
        }

        val timer = SavedTimer(
            id = editingTimer?.id ?: 0,
            name = name,
            durationMinutes = binding.durationSlider.value.toInt(),
            soundResId = selectedSoundResId,
            fadeDurationMinutes = binding.fadeSlider.value.toInt(),
            usedCount = editingTimer?.usedCount ?: 0,
            lastUsedAt = editingTimer?.lastUsedAt,
            createdAt = editingTimer?.createdAt ?: System.currentTimeMillis()
        )

        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(requireContext())

            if (editingTimer != null) {
                database.savedTimerDao().updateTimer(timer)
                Snackbar.make(requireView(), R.string.timer_updated, Snackbar.LENGTH_SHORT).show()
            } else {
                database.savedTimerDao().insertTimer(timer)
                Snackbar.make(requireView(), R.string.timer_saved, Snackbar.LENGTH_SHORT).show()
            }

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

