package com.xilingyuli.weather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.format.DateFormat
import android.widget.RemoteViews
import com.xilingyuli.weather.MainActivity
import com.xilingyuli.weather.R
import java.util.Calendar

class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = buildRemoteViews(context, appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_MODE) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (widgetId >= 0) {
                toggleMode(context, widgetId)
                WeatherRefreshWorker.refreshNow(context)
            }
        }
    }

    companion object {
        private const val ACTION_TOGGLE_MODE = "com.xilingyuli.weather.TOGGLE_MODE"
        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_MODE = "mode_"

        fun toggleMode(context: Context, widgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getBoolean(KEY_MODE + widgetId, false)
            prefs.edit().putBoolean(KEY_MODE + widgetId, !current).apply()
        }

        fun isHourlyMode(context: Context, widgetId: Int): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_MODE + widgetId, false)
        }

        fun buildRemoteViews(context: Context, widgetId: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.weather_widget)

            val cal = Calendar.getInstance()
            val dateStr = DateFormat.format("MM/dd", cal) as String
            val timeStr = DateFormat.format("kk:mm", cal) as String
            views.setTextViewText(R.id.widget_date, dateStr)
            views.setTextViewText(R.id.widget_time, timeStr)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            val toggleIntent = Intent(context, WeatherWidget::class.java).apply {
                action = ACTION_TOGGLE_MODE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context, widgetId, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_bottom, togglePendingIntent)

            return views
        }
    }
}
