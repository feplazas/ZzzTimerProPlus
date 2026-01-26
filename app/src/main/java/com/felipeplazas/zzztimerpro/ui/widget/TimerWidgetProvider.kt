package com.felipeplazas.zzztimerpro.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.felipeplazas.zzztimerpro.R

/**
 * Implementation of App Widget functionality.
 */
class TimerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        // Handle widget click events
        if (intent?.action == ACTION_START_TIMER) {
            // Usar el nombre completo de la clase para evitar ambigüedad
            val mainIntent = Intent(context, com.felipeplazas.zzztimerpro.ui.main.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context?.startActivity(mainIntent)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.timer_widget)

        // Set up click listeners
        val startIntent = Intent(context, TimerWidgetProvider::class.java).apply {
            action = ACTION_START_TIMER
        }

        views.setOnClickPendingIntent(R.id.widget_container,
            android.app.PendingIntent.getBroadcast(
                context,
                0,
                startIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                        android.app.PendingIntent.FLAG_IMMUTABLE
            )
        )

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_START_TIMER = "ACTION_START_TIMER"
    }
}