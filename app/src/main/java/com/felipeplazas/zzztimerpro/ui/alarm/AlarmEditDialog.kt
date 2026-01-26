package com.felipeplazas.zzztimerpro.ui.alarm

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.data.local.ScheduledAlarm
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import com.google.android.material.switchmaterial.SwitchMaterial

class AlarmEditDialog(
    context: Context,
    private val alarm: ScheduledAlarm?,
    private val onSave: (ScheduledAlarm) -> Unit
) : Dialog(context) {

    private lateinit var etName: EditText
    private lateinit var timePicker: TimePicker
    private lateinit var checkboxes: List<CheckBox>
    private lateinit var switchSmartWake: SwitchMaterial
    private lateinit var switchMathChallenge: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_alarm_edit, null)

        etName = view.findViewById(R.id.etAlarmName)
        timePicker = view.findViewById(R.id.timePicker)
        switchSmartWake = view.findViewById(R.id.switchSmartWake)
        switchMathChallenge = view.findViewById(R.id.switchMathChallenge)

        // Day checkboxes
        checkboxes = listOf(
            view.findViewById(R.id.cbMonday),
            view.findViewById(R.id.cbTuesday),
            view.findViewById(R.id.cbWednesday),
            view.findViewById(R.id.cbThursday),
            view.findViewById(R.id.cbFriday),
            view.findViewById(R.id.cbSaturday),
            view.findViewById(R.id.cbSunday)
        )

        // Set existing values if editing
        alarm?.let {
            etName.setText(it.name)
            timePicker.hour = it.hour
            timePicker.minute = it.minute
            switchSmartWake.isChecked = it.smartWakeEnabled
            switchMathChallenge.isChecked = it.mathChallengeEnabled

            val repeatDays = it.repeatDays.split(",").mapNotNull { day -> day.toIntOrNull() }
            repeatDays.forEach { day ->
                if (day in checkboxes.indices) {
                    checkboxes[day].isChecked = true
                }
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(if (alarm == null) R.string.add_alarm else R.string.edit_alarm)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                saveAlarm()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show()
    }

    private fun saveAlarm() {
        val name = etName.text.toString().ifEmpty { context.getString(R.string.alarm_name) }
        val hour = timePicker.hour
        val minute = timePicker.minute
        val repeatDays = checkboxes
            .mapIndexedNotNull { index, checkbox -> if (checkbox.isChecked) index else null }
            .joinToString(",")

        val newAlarm = ScheduledAlarm(
            id = alarm?.id ?: 0,
            name = name,
            hour = hour,
            minute = minute,
            enabled = alarm?.enabled ?: true,
            repeatDays = repeatDays,
            smartWakeEnabled = switchSmartWake.isChecked,
            mathChallengeEnabled = switchMathChallenge.isChecked
        )

        onSave(newAlarm)
    }
}
