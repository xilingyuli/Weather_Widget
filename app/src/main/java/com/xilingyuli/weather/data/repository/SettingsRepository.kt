package com.xilingyuli.weather.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xilingyuli.weather.data.datasource.DataSourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "weather_settings")

class SettingsRepository(val context: Context) {

    companion object {
        private val ACTIVE_DATA_SOURCE = intPreferencesKey("active_data_source")
    }

    val activeDataSourceFlow: Flow<DataSourceType> = context.settingsStore.data.map { prefs ->
        val ordinal = prefs[ACTIVE_DATA_SOURCE] ?: DataSourceType.WEATHER_CN.ordinal
        DataSourceType.entries.getOrElse(ordinal) { DataSourceType.WEATHER_CN }
    }

    suspend fun getActiveDataSourceType(): DataSourceType {
        return activeDataSourceFlow.first()
    }

    suspend fun setActiveDataSourceType(type: DataSourceType) {
        context.settingsStore.edit { prefs ->
            prefs[ACTIVE_DATA_SOURCE] = type.ordinal
        }
    }
}
