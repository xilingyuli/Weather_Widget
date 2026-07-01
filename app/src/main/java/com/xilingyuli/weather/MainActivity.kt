package com.xilingyuli.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xilingyuli.weather.data.model.SimpleLocation
import com.xilingyuli.weather.data.service.GeoCodingService
import com.xilingyuli.weather.data.service.LocationProvider
import com.xilingyuli.weather.databinding.ActivityMainBinding
import com.xilingyuli.weather.ui.LocationWeather
import com.xilingyuli.weather.ui.WeatherCityAdapter
import com.xilingyuli.weather.ui.WeatherViewModel
import com.xilingyuli.weather.widget.WeatherRefreshWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by lazy { WeatherViewModel(application) }
    private lateinit var adapter: WeatherCityAdapter

    private val locationProvider by lazy { LocationProvider(this) }
    private val geoCodingService by lazy {
        GeoCodingService(com.xilingyuli.weather.BuildConfig.QW_API_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WeatherRefreshWorker.schedule(this)

        adapter = WeatherCityAdapter(
            onRefreshCurrent = { viewModel.refreshCurrent(it.location) },
            onRefreshDaily = { viewModel.refreshDaily(it.location) },
            onRefreshHourly = { viewModel.refreshHourly(it.location) },
        )
        binding.recyclerCities.layoutManager = LinearLayoutManager(this)
        binding.recyclerCities.adapter = adapter

        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                adapter.submitList(state.locations)
                binding.btnSwitchSource.text = "数据源: ${state.activeDataSource.displayName}"
            }
        }

        binding.btnSwitchSource.setOnClickListener {
            viewModel.switchDataSource()
        }

        binding.btnLocate.setOnClickListener {
            requestLocationUpdate()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION,
            )
        }
    }

    private fun requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION,
            )
            return
        }
        lifecycleScope.launch {
            binding.btnLocate.isEnabled = false
            binding.btnLocate.text = "定位中..."
            withContext(Dispatchers.IO) {
                val raw = locationProvider.getNetworkLocation()
                if (raw == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "获取位置失败，请检查网络", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }
                val geo = geoCodingService.reverseLookup(raw.latitude, raw.longitude)
                withContext(Dispatchers.Main) {
                    if (geo.location != null) {
                        geo.location.let { loc ->
                            viewModel.updateLocation(
                                loc.copy(id = com.xilingyuli.weather.ui.Defaults.LOCATION_AUTO_ID)
                            )
                        }
                        Toast.makeText(this@MainActivity, "定位成功: ${geo.location.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "逆地理失败: ${geo.error}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            binding.btnLocate.isEnabled = true
            binding.btnLocate.text = "定位"
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdate()
        }
    }

    companion object {
        private const val REQUEST_LOCATION = 1001
    }
}
