package com.xilingyuli.weather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xilingyuli.weather.R
import com.xilingyuli.weather.data.model.WeatherCurrent
import com.xilingyuli.weather.data.model.WeatherDaily
import com.xilingyuli.weather.databinding.ItemCityWeatherBinding

class WeatherCityAdapter(
    private val onRefreshCurrent: (LocationWeather) -> Unit,
    private val onRefreshDaily: (LocationWeather) -> Unit,
    private val onRefreshHourly: (LocationWeather) -> Unit,
) : RecyclerView.Adapter<WeatherCityAdapter.ViewHolder>() {

    private var items: List<LocationWeather> = emptyList()

    fun submitList(list: List<LocationWeather>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCityWeatherBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val b: ItemCityWeatherBinding) :
        RecyclerView.ViewHolder(b.root) {

        private var currentItem: LocationWeather? = null

        init {
            b.btnCurrent.setOnClickListener { currentItem?.let { onRefreshCurrent(it) } }
            b.btnDaily.setOnClickListener { currentItem?.let { onRefreshDaily(it) } }
            b.btnHourly.setOnClickListener { currentItem?.let { onRefreshHourly(it) } }
        }

        fun bind(item: LocationWeather) {
            currentItem = item
            val loc = item.location
            val cityLabel = if (loc.district != null) {
                "${loc.city} ${loc.district}"
            } else {
                loc.city.ifBlank { loc.name }
            }
            b.itemCityName.text = cityLabel

            b.itemCurrentText.text = formatCurrent(item.current, item.isLoadingCurrent)
            b.itemDailyText.text = formatDaily(item.daily, item.isLoadingDaily)
            b.itemHourlyText.text = formatHourly(item.hourly, item.isLoadingHourly)
            b.itemErrorText.text = item.error.orEmpty()
        }

        private fun formatCurrent(c: WeatherCurrent?, loading: Boolean): String {
            if (loading) return "实况: 加载中..."
            if (c == null) return "实况: 无数据"
            val parts = mutableListOf("${c.temp.toInt()}° ${c.condition}")
            c.humidity?.let { parts.add("湿度$it%") }
            c.windDir?.let { parts.add("$it") }
            parts.add("${c.windSpeed.toInt()}km/h")
            return "实况: ${parts.joinToString("  ")}"
        }

        private fun formatDaily(d: List<WeatherDaily>?, loading: Boolean): String {
            if (loading) return "逐日: 加载中..."
            if (d == null || d.isEmpty()) return "逐日: 无数据"
            val lines = d.take(3).map { day ->
                val date = day.validTime?.take(10)?.substringAfterLast("-") ?: "?"
                "${date}日 ${day.tempMin.toInt()}~${day.tempMax.toInt()}° ${day.condition}"
            }
            return "逐日: ${lines.joinToString(" | ")}"
        }

        private fun formatHourly(h: List<WeatherCurrent>?, loading: Boolean): String {
            if (loading) return "逐时: 加载中..."
            if (h == null || h.isEmpty()) return "逐时: 无数据"
            val lines = h.take(4).map { hour ->
                val time = hour.validTime?.substring(11, 16) ?: "?"
                "${time} ${hour.temp.toInt()}° ${hour.condition}"
            }
            return "逐时: ${lines.joinToString(" | ")}"
        }
    }
}
