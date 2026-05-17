package com.disciplinelock.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.disciplinelock.app.MainActivity
import com.disciplinelock.app.R

class DisciplineWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.disciplinelock.app.UPDATE_WIDGET"
        const val EXTRA_USAGE_MINUTES = "usage_minutes"
        const val EXTRA_LIMIT_MINUTES = "limit_minutes"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Initial setup, default values until service broadcasts actual data
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, 0, 30)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val usageMinutes = intent.getIntExtra(EXTRA_USAGE_MINUTES, 0)
            val limitMinutes = intent.getIntExtra(EXTRA_LIMIT_MINUTES, 30)
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, DisciplineWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, usageMinutes, limitMinutes)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        usageMinutes: Int,
        limitMinutes: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_discipline)
        
        val progress = if (limitMinutes > 0) ((usageMinutes.toFloat() / limitMinutes.toFloat()) * 100).toInt() else 0
        views.setTextViewText(R.id.widget_usage, "${usageMinutes}m / ${limitMinutes}m")
        views.setProgressBar(R.id.widget_progress, 100, Math.min(progress, 100), false)

        // Launch app on click
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_usage, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
