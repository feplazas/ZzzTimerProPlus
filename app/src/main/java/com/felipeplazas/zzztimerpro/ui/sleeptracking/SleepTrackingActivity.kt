package com.felipeplazas.zzztimerpro.ui.sleeptracking

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.databinding.ActivitySleepTrackingBinding
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.SleepSession
import com.felipeplazas.zzztimerpro.services.SleepTrackingService
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import com.felipeplazas.zzztimerpro.utils.SleepScoreCalculator
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast

class SleepTrackingActivity : BaseActivity() {

    private lateinit var binding: ActivitySleepTrackingBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: com.felipeplazas.zzztimerpro.data.repository.SleepTrackingRepository
    private var currentSessionId: Long? = null
    private var isTracking = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        repository = com.felipeplazas.zzztimerpro.data.repository.SleepTrackingRepository(database)

        setupToolbar()
        setupListeners()
        loadRecentSession()
        checkPermissions()
        checkAndRecoverInterruptedSessions()
        com.felipeplazas.zzztimerpro.utils.StarAnimationHelper.startStarAnimations(this)
    }
    
    override fun onResume() {
        super.onResume()
        // Aggregate any pending sessions when activity resumes
        lifecycleScope.launch {
            repository.aggregateAllPendingSessions()
            loadRecentSession()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnStartStop.setOnClickListener {
            if (isTracking) {
                stopTracking()
            } else {
                startTracking()
            }
        }

        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, SleepHistoryActivity::class.java))
        }
    }

    private fun checkPermissions() {
        val missingPermissions = getMissingPermissions()
        
        if (missingPermissions.isNotEmpty()) {
            showPermissionsExplanationDialog(missingPermissions)
        }
    }
    
    private fun getMissingPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }


        
        return permissions
    }
    
    private fun showPermissionsExplanationDialog(permissions: List<String>) {
        val permissionNames = permissions.map { permission ->
            when (permission) {
                Manifest.permission.RECORD_AUDIO -> getString(R.string.permission_microphone)
                else -> permission
            }
        }
        
        val message = buildString {
            append(getString(R.string.sleep_tracking_permissions_explanation))
            append("\n\n")
            append(getString(R.string.required_permissions))
            append("\n")
            permissionNames.forEach { name ->
                append("• $name\n")
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.permissions_required)
            .setMessage(message)
            .setPositiveButton(R.string.grant_permissions) { _, _ ->
                requestPermissions(permissions)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    this,
                    R.string.sleep_tracking_requires_permissions,
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun requestPermissions(permissions: List<String>) {
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            
            permissions.forEachIndexed { index, permission ->
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission)
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                Toast.makeText(
                    this,
                    R.string.permissions_granted_success,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showPermissionsDeniedDialog(deniedPermissions)
            }
        }
    }
    
    private fun showPermissionsDeniedDialog(deniedPermissions: List<String>) {
        val permissionNames = deniedPermissions.map { permission ->
            when (permission) {
                Manifest.permission.RECORD_AUDIO -> getString(R.string.permission_microphone)
                else -> permission
            }
        }
        
        val message = buildString {
            append(getString(R.string.permissions_denied_explanation))
            append("\n\n")
            append(getString(R.string.denied_permissions))
            append("\n")
            permissionNames.forEach { name ->
                append("• $name\n")
            }
            append("\n")
            append(getString(R.string.enable_permissions_manually))
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.permissions_denied)
            .setMessage(message)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun openAppSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = android.net.Uri.fromParts("package", packageName, null)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                R.string.cannot_open_settings,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startTracking() {
        // Verificar permisos antes de iniciar
        val missingPermissions = getMissingPermissions()
        if (missingPermissions.isNotEmpty()) {
            showPermissionsExplanationDialog(missingPermissions)
            return
        }
        
        lifecycleScope.launch {
            try {
                val session = SleepSession(
                    startTime = System.currentTimeMillis(),
                    endTime = null
                )

                currentSessionId = database.sleepSessionDao().insertSession(session)

                val intent = Intent(this@SleepTrackingActivity, SleepTrackingService::class.java).apply {
                    action = SleepTrackingService.ACTION_START_TRACKING
                    putExtra(SleepTrackingService.EXTRA_SESSION_ID, currentSessionId)
                }
                startService(intent)

                isTracking = true
                updateUI()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@SleepTrackingActivity,
                    "Error starting sleep tracking: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun stopTracking() {
        lifecycleScope.launch {
            currentSessionId?.let { sessionId ->
                val session = database.sleepSessionDao().getSessionById(sessionId)
                session?.let {
                    val endTime = System.currentTimeMillis()
                    val updatedSession = it.copy(
                        endTime = endTime,
                        completed = true
                    )
                    // Save initial completion state
                    database.sleepSessionDao().updateSession(updatedSession)
                    
                    // Perform full aggregation using repository
                    repository.aggregateSessionStats(sessionId)
                }
            }

            val intent = Intent(this@SleepTrackingActivity, SleepTrackingService::class.java).apply {
                action = SleepTrackingService.ACTION_STOP_TRACKING
            }
            startService(intent)

            isTracking = false
            currentSessionId = null
            updateUI()
            loadRecentSession()
        }
    }

    private fun checkAndRecoverInterruptedSessions() {
        lifecycleScope.launch {
            try {
                // Recover latest session if interrupted
                val dao = database.sleepSessionDao()
                val incompleteSessions = dao.getSessionsByDateRange(0, System.currentTimeMillis())
                    .filter { it.endTime == null || it.endTime == 0L }

                incompleteSessions.forEach { session ->
                    // Find last cycle for this session
                    val cycles = database.sleepCycleDao().getCyclesBySessionSync(session.id)
                    if (cycles.isNotEmpty()) {
                        val lastCycle = cycles.last()
                        val endTime = lastCycle.endTime
                        
                        val updatedSession = session.copy(
                            endTime = endTime,
                            completed = true
                        )
                        // Save initial recovery state
                        dao.updateSession(updatedSession)
                        
                        // Perform full aggregation using the repository (includes fallback logic)
                        repository.aggregateSessionStats(updatedSession.id)
                    } else {
                        // Dead session with no data, delete it
                        dao.deleteSession(session)
                    }
                }
                
                // Refresh data
                loadRecentSession()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun loadRecentSession() {
        lifecycleScope.launch {
            database.sleepSessionDao().getCompletedSessions(1).collect { sessions ->
                if (sessions.isNotEmpty()) {
                    displaySessionSummary(sessions[0])
                }
            }
        }
    }

    private fun displaySessionSummary(session: SleepSession) {
        binding.tvSleepScore.text = "${session.sleepScore ?: 0}"
        
        binding.tvSleepDurationValue.text = "${session.totalMinutes ?: 0}"
        binding.tvDeepSleepValue.text = "${session.deepSleepMinutes ?: 0}"
        binding.tvLightSleepValue.text = "${session.lightSleepMinutes ?: 0}"
        binding.tvRemSleepValue.text = "${session.remSleepMinutes ?: 0}"

        val score = session.sleepScore ?: 0
        val qualityLabel = when {
            score >= 85 -> getString(R.string.quality_excellent)
            score >= 70 -> getString(R.string.quality_good)
            score >= 50 -> getString(R.string.quality_fair)
            else -> getString(R.string.quality_poor)
        }
        binding.tvSleepQuality.text = qualityLabel
    }
    
    private fun updateUI() {
        if (isTracking) {
            binding.btnStartStop.text = getString(R.string.stop_tracking)
            binding.tvTrackingStatus.text = getString(R.string.sleep_tracking_active)
            binding.btnViewHistory.isEnabled = false // Disable history while tracking
        } else {
            binding.btnStartStop.text = getString(R.string.start_tracking)
            binding.tvTrackingStatus.text = getString(R.string.sleep_tracking)
            binding.btnViewHistory.isEnabled = true
        }
    }
}
