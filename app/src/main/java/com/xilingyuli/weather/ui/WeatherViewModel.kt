package com.xilingyuli.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xilingyuli.weather.data.datasource.DataSourceType
import com.xilingyuli.weather.data.model.WeatherCurrent
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.data.model.WeatherLocation
import com.xilingyuli.weather.data.repository.LocationRepository
import com.xilingyuli.weather.data.repository.SettingsRepository
import com.xilingyuli.weather.data.repository.WeatherRepository
import com.xilingyuli.weather.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationWeather(
    val location: WeatherLocation,
    val current: WeatherCurrent? = null,
    val daily: List<WeatherDaily>? = null,
    val hourly: List<WeatherCurrent>? = null,
    val isLoadingCurrent: Boolean = false,
    val isLoadingDaily: Boolean = false,
    val isLoadingHourly: Boolean = false,
    val error: String? = null,
)

data class WeatherUiState(
    val locations: List<LocationWeather> = emptyList(),
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
        loadLocations()
    }

    fun loadLocations() {
        val locations = repository.getLocations()
        _uiState.value = _uiState.value.copy(
            locations = locations.map { LocationWeather(location = it) }
        )
    }

    fun refreshCurrent(location: WeatherLocation) {
        val idx = findIndex(location.id) ?: return
        update(idx) { it.copy(isLoadingCurrent = true, error = null) }
        viewModelScope.launch {
            repository.getCurrentWeather(location).fold(
                onSuccess = { current -> update(idx) { it.copy(current = current, isLoadingCurrent = false) } },
                onFailure = { e -> update(idx) { it.copy(isLoadingCurrent = false, error = e.message) } },
            )
        }
    }

    fun refreshDaily(location: WeatherLocation) {
        val idx = findIndex(location.id) ?: return
        update(idx) { it.copy(isLoadingDaily = true, error = null) }
        viewModelScope.launch {
            repository.getDailyForecast(location, 7).fold(
                onSuccess = { daily -> update(idx) { it.copy(daily = daily, isLoadingDaily = false) } },
                onFailure = { e -> update(idx) { it.copy(isLoadingDaily = false, error = e.message) } },
            )
        }
    }

    fun refreshHourly(location: WeatherLocation) {
        val idx = findIndex(location.id) ?: return
        update(idx) { it.copy(isLoadingHourly = true, error = null) }
        viewModelScope.launch {
            repository.getHourlyForecast(location, 24).fold(
                onSuccess = { hourly -> update(idx) { it.copy(hourly = hourly, isLoadingHourly = false) } },
                onFailure = { e -> update(idx) { it.copy(isLoadingHourly = false, error = e.message) } },
            )
        }
    }

    fun switchDataSource() {
        val current = _uiState.value.activeDataSource
        val next = when (current) {
            DataSourceType.WEATHER_CN -> DataSourceType.QWEATHER
            DataSourceType.QWEATHER -> DataSourceType.CAIYUN
            DataSourceType.CAIYUN -> DataSourceType.WEATHER_CN
        }
        viewModelScope.launch {
            settingsRepository.setActiveDataSourceType(next)
        }
    }

    fun addLocation(location: WeatherLocation) {
        repository.addLocation(location)
        val newList = _uiState.value.locations + LocationWeather(location = location)
        _uiState.value = _uiState.value.copy(locations = newList)
    }

    fun updateLocation(location: WeatherLocation) {
        val idx = findIndex(location.id) ?: return
        update(idx) { it.copy(location = location) }
        viewModelScope.launch {
            AppContainer.getLocationRepository().updateLocation(location)
        }
    }

    fun getActiveDataSourceLabel(): String = _uiState.value.activeDataSource.displayName

    private fun findIndex(locationId: String): Int? {
        val idx = _uiState.value.locations.indexOfFirst { it.location.id == locationId }
        return if (idx >= 0) idx else null
    }

    private fun update(index: Int, transform: (LocationWeather) -> LocationWeather) {
        val list = _uiState.value.locations.toMutableList()
        if (index in list.indices) {
            list[index] = transform(list[index])
            _uiState.value = _uiState.value.copy(locations = list)
        }
    }
}
