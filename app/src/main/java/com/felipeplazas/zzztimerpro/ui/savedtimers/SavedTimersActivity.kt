package com.felipeplazas.zzztimerpro.ui.savedtimers

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.SavedTimer
import com.felipeplazas.zzztimerpro.data.repository.SavedTimersRepository
import com.felipeplazas.zzztimerpro.databinding.ActivitySavedTimersBinding
import com.felipeplazas.zzztimerpro.ui.main.MainActivity
import com.felipeplazas.zzztimerpro.ui.savedtimers.adapters.SavedTimerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SavedTimersActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedTimersBinding
    private lateinit var adapter: SavedTimerAdapter
    private lateinit var repository: SavedTimersRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedTimersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = SavedTimersRepository.getInstance(this)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        loadTimers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.saved_timers)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = SavedTimerAdapter(
            onTimerClick = { timer -> startTimer(timer) },
            onTimerEdit = { timer -> showTimerOptions(timer) },
            onTimerDelete = { timer -> confirmDeleteTimer(timer) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SavedTimersActivity)
            adapter = this@SavedTimersActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddTimer.setOnClickListener {
            showTimerDialog(null)
        }
    }

    private fun loadTimers() {
        lifecycleScope.launch {
            repository.getAllTimers().collect { timers ->
                adapter.submitList(timers)
                binding.emptyView.visibility = if (timers.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (timers.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun startTimer(timer: SavedTimer) {
        // Update usage count
        lifecycleScope.launch {
            val updatedTimer = timer.copy(
                usedCount = timer.usedCount + 1,
                lastUsedAt = System.currentTimeMillis()
            )
            repository.updateTimer(updatedTimer)
        }

        // Return to MainActivity with timer data
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("TIMER_DURATION_MINUTES", timer.durationMinutes)
            putExtra("TIMER_SOUND_RES_ID", timer.soundResId)
            putExtra("TIMER_FADE_DURATION", timer.fadeDurationMinutes)
            putExtra("START_TIMER", true)
        }
        startActivity(intent)
        finish()
    }

    private fun showTimerOptions(timer: SavedTimer) {
        val options = arrayOf(
            getString(R.string.edit_timer),
            getString(R.string.delete_timer)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(timer.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showTimerDialog(timer)
                    1 -> confirmDeleteTimer(timer)
                }
            }
            .show()
    }

    private fun showTimerDialog(timer: SavedTimer?) {
        val dialog = SavedTimerDialogFragment.newInstance(timer)
        dialog.show(supportFragmentManager, "SavedTimerDialog")
    }

    private fun confirmDeleteTimer(timer: SavedTimer) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_timer)
            .setMessage(getString(R.string.confirm_delete_timer, timer.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteTimer(timer)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteTimer(timer: SavedTimer) {
        lifecycleScope.launch {
            repository.deleteTimer(timer)
            Snackbar.make(binding.root, R.string.timer_deleted, Snackbar.LENGTH_SHORT).show()
        }
    }
}
