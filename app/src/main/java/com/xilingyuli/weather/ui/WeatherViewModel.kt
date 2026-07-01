package com.xilingyuli.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xilingyuli.weather.data.datasource.DataSourceType
import com.xilingyuli.weather.data.model.WeatherNow
import com.xilingyuli.weather.data.repository.SettingsRepository
import com.xilingyuli.weather.data.repository.WeatherRepository
import com.xilingyuli.weather.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherNow: WeatherNow? = null,
    val error: String? = null,
    val activeDataSource: DataSourceType = DataSourceType.WEATHER_CN,
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeatherRepository = AppContainer.getWeatherRepository()
    private val settingsRepository: SettingsRepository = AppContainer.getSettingsRepository()

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.activeDataSourceFlow.collect { type ->
                _uiState.value = _uiState.value.copy(activeDataSource = type)
            }
        }
        refreshWeather()
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getCurrentWeather(Defaults.BEIJING)
            result.fold(
                onSuccess = { now ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        weatherNow = now,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "未知错误",
                    )
                },
            )
        }
    }

    fun switchDataSource(type: DataSourceType) {
        viewModelScope.launch {
            settingsRepository.setActiveDataSourceType(type)
            refreshWeather()
        }
    }
}
