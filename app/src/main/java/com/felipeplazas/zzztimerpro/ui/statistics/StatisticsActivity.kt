package com.felipeplazas.zzztimerpro.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.repository.StatisticsRepository
import com.felipeplazas.zzztimerpro.databinding.ActivityStatisticsBinding
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import com.felipeplazas.zzztimerpro.license.LicenseManager
import android.view.View
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class StatisticsActivity : BaseActivity() {
    
    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var repository: StatisticsRepository
    private lateinit var licenseManager: LicenseManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        licenseManager = LicenseManager(this)
        
        // Ensure buttons have proper padding after background change
        binding.btnExportData.setPadding(48, 0, 48, 0)
        binding.btnClearStatistics.setPadding(48, 0, 48, 0)
        
        setupToolbar()
        setupRepository()
        setupListeners()
        loadStatistics()
        updateUIForLicense()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRepository() {
        val database = AppDatabase.getDatabase(this)
        repository = StatisticsRepository(database.timerSessionDao(), database.sleepSessionDao())
    }
    
    private fun setupListeners() {
        binding.btnExportData.setOnClickListener {
            if (!licenseManager.isPremium()) {
                Toast.makeText(
                    this,
                    R.string.export_locked_free_version,
                    Toast.LENGTH_SHORT
                ).show()
                checkPremiumAccess()
                return@setOnClickListener
            }
            exportData()
        }
        
        binding.btnClearStatistics.setOnClickListener {
            clearStatistics()
        }
    }
    
    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                // Load statistics data
                val totalMinutes = repository.getTotalMinutesUsed()
                val sessionsCompleted = repository.getCompletedSessionCount()
                val averageDuration = repository.getAverageDuration()
                val mostUsedSound = repository.getMostUsedSound()
                val mostCommonSchedule = repository.getMostCommonSchedule()
                
                // Update UI
                updateTotalUsage(totalMinutes)
                binding.tvSessionsCompleted.text = sessionsCompleted.toString()
                binding.tvAverageDuration.text = formatDuration(averageDuration.toInt())
                binding.tvMostUsedSound.text = mostUsedSound ?: getString(R.string.no_data_available)
                binding.tvMostCommonSchedule.text = mostCommonSchedule
                
                // Setup chart
                setupWeeklyChart()
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@StatisticsActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateTotalUsage(minutes: Int) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        
        val text = if (hours > 0) {
            "$hours ${getString(R.string.hours)} $remainingMinutes ${getString(R.string.minutes)}"
        } else {
            "$remainingMinutes ${getString(R.string.minutes)}"
        }
        
        binding.tvTotalUsage.text = text
    }
    
    private fun formatDuration(minutes: Int): String {
        return if (minutes >= 60) {
            val hours = minutes / 60
            val mins = minutes % 60
            "$hours ${getString(R.string.hours)} $mins ${getString(R.string.minutes)}"
        } else {
            "$minutes ${getString(R.string.minutes)}"
        }
    }
    
    private fun setupWeeklyChart() {
        lifecycleScope.launch {
            val entries = mutableListOf<BarEntry>()
            val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            
            // Load actual weekly data from repository (empty on first use)
            val weeklyData = repository.getWeeklyData()
            
            labels.forEachIndexed { index, day ->
                val value = weeklyData[day]?.toFloat() ?: 0f
                entries.add(BarEntry(index.toFloat(), value))
            }
            
            val dataSet = BarDataSet(entries, "Minutes").apply {
                color = getColor(R.color.primary_gold)
                valueTextColor = getColor(R.color.black)
                valueTextSize = 10f
            }
            
            val barData = BarData(dataSet)
            
            binding.chartWeekly.apply {
                data = barData
                description.isEnabled = false
                legend.isEnabled = false
                
                // Show "No data yet" if all values are 0
                val hasData = entries.any { it.y > 0 }
                if (!hasData) {
                    setNoDataText("No sleep data yet. Start tracking!")
                    setNoDataTextColor(getColor(R.color.text_secondary))
                }
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    textColor = getColor(R.color.primary_night_blue)
                    setDrawGridLines(false)
                }
                
                axisLeft.apply {
                    textColor = getColor(R.color.primary_night_blue)
                    axisMinimum = 0f
                    setDrawGridLines(true)
                    gridColor = getColor(R.color.primary_night_blue)
                }
                
                axisRight.isEnabled = false
                animateY(1000)
                invalidate()
            }
        }
    }
    
    private fun exportData() {
        lifecycleScope.launch {
            try {
                val csvData = repository.exportData()
                
                if (csvData.isBlank() || csvData.lines().size <= 1) {
                    Toast.makeText(this@StatisticsActivity, "No data to export", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Use Android Share Intent (no file permissions needed)
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Zzz Timer Pro+ Sleep Statistics")
                    putExtra(android.content.Intent.EXTRA_TEXT, csvData)
                }
                startActivity(android.content.Intent.createChooser(shareIntent, "Export Statistics via..."))
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@StatisticsActivity, R.string.error_export_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun clearStatistics() {
        // Show confirmation dialog
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete All Statistics?")
            .setMessage("This action cannot be undone. All your sleep and timer history will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        repository.clearAllStatistics()
                        Toast.makeText(this@StatisticsActivity, "Statistics cleared", Toast.LENGTH_SHORT).show()
                        loadStatistics() // Reload UI to show empty state
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@StatisticsActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateUIForLicense() {
        if (!licenseManager.isPremium()) {
            // Hide detailed chart for free version
            binding.chartWeekly.visibility = View.GONE
        } else {
            binding.chartWeekly.visibility = View.VISIBLE
        }
    }
}
