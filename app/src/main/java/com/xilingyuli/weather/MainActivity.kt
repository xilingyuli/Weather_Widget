package com.xilingyuli.weather

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xilingyuli.weather.databinding.ActivityMainBinding
import com.xilingyuli.weather.ui.WeatherViewModel
import com.xilingyuli.weather.widget.WeatherRefreshWorker
import kotlinx.coroutines.flow.collectLatest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by lazy {
        WeatherViewModel(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WeatherRefreshWorker.schedule(this)

        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                if (state.weatherNow != null) {
                    binding.tempText.text = "${state.weatherNow.temp.toInt()}°"
                    binding.conditionText.text = state.weatherNow.condition
                    binding.detailText.text = buildString {
                        append("体感 ${state.weatherNow.feelsLike.toInt()}°  ")
                        append("湿度 ${state.weatherNow.humidity}%  ")
                        append("${state.weatherNow.windDir} ${state.weatherNow.windSpeed.toInt()}km/h")
                    }
                    binding.sourceText.text = state.weatherNow.sourceLabel
                }
                if (state.isLoading) {
                    binding.tempText.text = "加载中"
                }
                if (state.error != null) {
                    binding.tempText.text = "错误"
                    binding.conditionText.text = state.error
                }
            }
        }

        binding.refreshButton.setOnClickListener {
            viewModel.refreshWeather()
        }
    }
}
