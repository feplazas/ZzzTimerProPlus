package com.felipeplazas.zzztimerpro.ui.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.felipeplazas.zzztimerpro.R
import com.felipeplazas.zzztimerpro.ui.main.MainActivity

/**
 * Worker that displays CPAP equipment maintenance reminders.
 */
class CpapReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        private const val CHANNEL_ID = "cpap_reminders"
        private const val NOTIFICATION_ID_MASK_DAILY = 5001
        private const val NOTIFICATION_ID_TUBE_WEEKLY = 5002
        private const val NOTIFICATION_ID_FILTER = 5003
        private const val NOTIFICATION_ID_MASK_REPLACE = 5004
    }

    override fun doWork(): Result {
        val reminderType = inputData.getString("reminder_type") ?: return Result.failure()
        
        createNotificationChannel()
        
        val (title, message, notificationId) = when (reminderType) {
            "mask_daily" -> Triple(
                context.getString(R.string.cpap_therapy_settings),
                context.getString(R.string.cpap_notification_mask_clean),
                NOTIFICATION_ID_MASK_DAILY
            )
            "tube_weekly" -> Triple(
                context.getString(R.string.cpap_therapy_settings),
                context.getString(R.string.cpap_notification_tube_clean),
                NOTIFICATION_ID_TUBE_WEEKLY
            )
            "filter" -> Triple(
                context.getString(R.string.cpap_therapy_settings),
                context.getString(R.string.cpap_notification_filter),
                NOTIFICATION_ID_FILTER
            )
            "mask_replace" -> Triple(
                context.getString(R.string.cpap_therapy_settings),
                context.getString(R.string.cpap_notification_mask_replace),
                NOTIFICATION_ID_MASK_REPLACE
            )
            else -> return Result.failure()
        }
        
        showNotification(title, message, notificationId)
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_cpap)
            val descriptionText = context.getString(R.string.notification_channel_cpap_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            notificationId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_breathing)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
