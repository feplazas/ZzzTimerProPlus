package com.felipeplazas.zzztimerpro.ui.alarm

import android.app.AlertDialog
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.databinding.ActivityAlarmsBinding
import com.felipeplazas.zzztimerpro.data.local.AppDatabase
import com.felipeplazas.zzztimerpro.data.local.ScheduledAlarm
import com.felipeplazas.zzztimerpro.services.AlarmScheduler
import com.felipeplazas.zzztimerpro.ui.BaseActivity
import com.felipeplazas.zzztimerpro.license.LicenseManager
import com.felipeplazas.zzztimerpro.data.repository.ScheduledAlarmsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlarmsActivity : BaseActivity() {

    private lateinit var binding: ActivityAlarmsBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var repository: ScheduledAlarmsRepository
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var licenseManager: LicenseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start floating star animations
        com.felipeplazas.zzztimerpro.utils.StarAnimationHelper.startStarAnimations(this)

        repository = ScheduledAlarmsRepository.getInstance(this)
        alarmScheduler = AlarmScheduler(this)
        licenseManager = LicenseManager(this)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadAlarms()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onToggle = { alarm, isEnabled ->
                lifecycleScope.launch {
                    repository.setEnabled(alarm.id, isEnabled)
                    if (isEnabled) {
                        alarmScheduler.scheduleAlarm(alarm.copy(enabled = true))
                    } else {
                        alarmScheduler.cancelAlarm(alarm.id)
                    }
                }
            },
            onEdit = { alarm ->
                showAlarmDialog(alarm)
            },
            onDelete = { alarm ->
                deleteAlarm(alarm)
            }
        )

        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(this@AlarmsActivity)
            adapter = alarmAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddAlarm.setOnClickListener {
            if (!licenseManager.isPremium()) {
                // Check alarm count for free version
                lifecycleScope.launch {
                    val alarmCount = alarmAdapter.itemCount
                    if (alarmCount >= 1) {
                        showPremiumDialog()
                        return@launch
                    }
                    showAlarmDialog(null)
                }
            } else {
                showAlarmDialog(null)
            }
        }
    }

    private fun loadAlarms() {
        lifecycleScope.launch {
            repository.getAllAlarms().collectLatest { alarms ->
                alarmAdapter.submitList(alarms)
            }
        }
    }

    private fun showAlarmDialog(alarm: ScheduledAlarm?) {
        val dialog = AlarmEditDialog(this, alarm) { updatedAlarm ->
            lifecycleScope.launch {
                if (alarm == null) {
                    val id = repository.insertAlarm(updatedAlarm)
                    alarmScheduler.scheduleAlarm(updatedAlarm.copy(id = id))
                } else {
                    repository.updateAlarm(updatedAlarm)
                    alarmScheduler.scheduleAlarm(updatedAlarm)
                }
            }
        }
        dialog.show()
    }

    private fun deleteAlarm(alarm: ScheduledAlarm) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_alarm)
            .setMessage(R.string.delete_alarm_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    repository.deleteAlarm(alarm)
                    alarmScheduler.cancelAlarm(alarm.id)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showPremiumDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.premium_required)
            .setMessage(R.string.alarm_limit_free_version)
            .setPositiveButton(R.string.upgrade) { _, _ ->
                checkPremiumAccess()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
