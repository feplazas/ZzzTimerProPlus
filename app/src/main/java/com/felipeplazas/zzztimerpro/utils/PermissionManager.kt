package com.felipeplazas.zzztimerpro.utils

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

/**
 * Gestor centralizado de permisos runtime para Android.
 *
 * Proporciona helpers para:
 * - POST_NOTIFICATIONS (Android 13+)
 * - Exact Alarms (Android 12+)
 *
 * @see shouldRequestPostNotifications para verificar si se debe solicitar
 * @see hasExactAlarmPermission para verificar estado de alarmas exactas
 */
object PermissionManager {
    fun shouldRequestPostNotifications(): Boolean = Build.VERSION.SDK_INT >= 33

    fun hasPostNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun requestPostNotifications(launcher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= 33) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.canScheduleExactAlarms()
        } else true
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:" + context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
