package com.xilingyuli.weather.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.xilingyuli.weather.R
import com.xilingyuli.weather.di.AppContainer
import com.xilingyuli.weather.ui.Defaults
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class WeatherRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val repository = AppContainer.getWeatherRepository()
            val result = runBlocking { repository.getCurrentWeather(Defaults.BEIJING) }
            result.fold(
                onSuccess = { now ->
                    updateWidget(now.temp.toInt().toString(), now.condition)
                    Result.success()
                },
                onFailure = {
                    updateWidget("--", "点击重试")
                    Result.retry()
                },
            )
        } catch (e: Exception) {
            updateWidget("--", "点击重试")
            Result.retry()
        }
    }

    private fun updateWidget(temp: String, condition: String) {
        val views = RemoteViews(applicationContext.packageName, R.layout.weather_widget)
        views.setTextViewText(R.id.widget_temp, "$temp°")
        views.setTextViewText(R.id.widget_condition, condition)
        val manager = android.appwidget.AppWidgetManager.getInstance(applicationContext)
        val widgetIds = manager.getAppWidgetIds(
            android.content.ComponentName(applicationContext, WeatherWidget::class.java),
        )
        manager.updateAppWidget(widgetIds, views)
    }

    companion object {
        private const val WORK_NAME = "weather_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeatherRefreshWorker>(
                30, TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun refreshNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<WeatherRefreshWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}

class WeatherRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        WeatherRefreshWorker.refreshNow(context)
    }
}
