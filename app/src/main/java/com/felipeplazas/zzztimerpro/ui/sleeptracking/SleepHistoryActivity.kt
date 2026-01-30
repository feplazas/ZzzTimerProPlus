package com.felipeplazas.zzztimerpro.ui.sleeptracking

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.SleepSession
import com.felipeplazas.zzztimerpro.databinding.ActivitySleepHistoryBinding
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import com.felipeplazas.zzztimerpro.utils.LogExt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivitySleepHistoryBinding
    private lateinit var adapter: SleepHistoryAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start floating star animations
        com.felipeplazas.zzztimerpro.utils.StarAnimationHelper.startStarAnimations(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sleep History"

        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = SleepHistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadData() {
        lifecycleScope.launch {
            database.sleepSessionDao().getCompletedSessions(100).collectLatest { sessions ->
                if (sessions.isEmpty()) {
                    binding.emptyStateGroup.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyStateGroup.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    adapter.submitList(sessions)
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(com.felipeplazas.zzztimerpro.R.menu.menu_sleep_history, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            com.felipeplazas.zzztimerpro.R.id.action_clear_history -> {
                showClearHistoryConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showClearHistoryConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear Sleep History?")
            .setMessage("This will permanently delete all your recorded sleep sessions. This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteHistory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteHistory() {
        lifecycleScope.launch {
            try {
                database.sleepSessionDao().deleteAllSessions()
                android.widget.Toast.makeText(this@SleepHistoryActivity, "History cleared", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(this@SleepHistoryActivity, "Error clearing history", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
