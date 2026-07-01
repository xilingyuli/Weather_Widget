package com.xilingyuli.weather.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
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
import com.xilingyuli.weather.data.model.WeatherCurrent
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.di.AppContainer
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class WeatherRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val repository = AppContainer.getWeatherRepository()
            val mainLocation = repository.getMainLocation() ?: return Result.failure()

            val currentResult = runBlocking { repository.getCurrentWeather(mainLocation) }
            val hourlyResult = runBlocking { repository.getHourlyForecast(mainLocation, 24) }
            val dailyResult = runBlocking { repository.getDailyForecast(mainLocation, 7) }

            val manager = AppWidgetManager.getInstance(applicationContext)
            val widgetIds = manager.getAppWidgetIds(
                ComponentName(applicationContext, WeatherWidget::class.java)
            )

            for (widgetId in widgetIds) {
                val views = WeatherWidget.buildRemoteViews(applicationContext, widgetId)

                currentResult.getOrNull()?.let { now ->
                    views.setTextViewText(R.id.widget_temp, "${now.temp.toInt()}°")
                    views.setTextViewText(R.id.widget_condition, now.condition)
                }

                val isHourly = WeatherWidget.isHourlyMode(applicationContext, widgetId)
                if (isHourly) {
                    hourlyResult.getOrNull()?.let { fillHourlySlots(views, it) }
                } else {
                    dailyResult.getOrNull()?.let { fillDailySlots(views, it) }
                }

                manager.updateAppWidget(widgetId, views)
            }

            if (currentResult.isFailure && hourlyResult.isFailure && dailyResult.isFailure) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun fillHourlySlots(views: RemoteViews, hourly: List<WeatherCurrent>) {
        val slotIds = intArrayOf(
            R.id.widget_slot1, R.id.widget_slot2, R.id.widget_slot3,
            R.id.widget_slot4, R.id.widget_slot5,
        )
        for (i in slotIds.indices) {
            val item = hourly.getOrNull(i)
            if (item != null) {
                val time = item.validTime?.substring(11, 16) ?: "--:--"
                views.setTextViewText(slotIds[i], "$time\n${item.temp.toInt()}°")
            } else {
                views.setTextViewText(slotIds[i], "--:--\n--°")
            }
        }
    }

    private fun fillDailySlots(views: RemoteViews, daily: List<WeatherDaily>) {
        val slotIds = intArrayOf(
            R.id.widget_slot1, R.id.widget_slot2, R.id.widget_slot3,
            R.id.widget_slot4, R.id.widget_slot5,
        )
        for (i in slotIds.indices) {
            val item = daily.getOrNull(i)
            if (item != null) {
                val date = item.validTime?.substring(5, 10) ?: "--/--"
                views.setTextViewText(slotIds[i], "$date\n${item.tempMin.toInt()}~${item.tempMax.toInt()}°")
            } else {
                views.setTextViewText(slotIds[i], "--/--\n--°")
            }
        }
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
