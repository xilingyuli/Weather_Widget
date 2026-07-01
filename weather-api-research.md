# 天气 API 调研报告

> 调研日期: 2026-06-21
> 目标: 为 Android 天气 App 寻找免费、数据可靠、低频调用友好的天气数据 API
> 核心诉求: 数据源首选中国气象局(CMA)，准确且免费

---

## 一、中国气象局官方数据源

### 1. 中国气象数据网 data.cma.cn

| 项目 | 说明 |
|------|------|
| 官网 | https://data.cma.cn |
| API文档 | https://data.cma.cn/Market/instructions.html |
| 数据内容 | 地面、高空、卫星、雷达、数值预报模式产品等 |
| 注册要求 | 需实名认证(个人或单位) |
| 免费额度 | 每类资料 API 仅免费试用 **7天** |
| 调用方式 | RESTful API, 返回 JSON, 需要 userId + pwd 认证 |
| 适合场景 | 科研、专业气象分析；不适合轻量级移动 App |

**结论: ❌ 不推荐用于 Android App**
- 免费试用仅 7 天，之后需付费
- API 设计面向科研和专业用户，调用复杂 (需要站点 ID、UTC 时间等)
- 实时天气查询不方便，需要知道气象站编号

---

### 2. 华风爱科 weathercn.com (中国气象局官方授权)

| 项目 | 说明 |
|------|------|
| 官网 | https://platform.weathercn.com |
| API文档 | https://platform.weathercn.com/apidoc/ |
| 数据源 | 中国气象局官方数据 + AccuWeather 全球数据 |
| 免费额度 | **500 次/天**，5 次/秒 |
| 注册要求 | 个人开发者实名认证 |
| 数据内容 | 实况天气、逐日预报、分钟级降水、空气质量、灾害预警等 |
| 开发规范 | 需使用 GZIP 压缩、随机化刷新频率、缓存机制等 |

**结论: ⚠️ 备选方案**
- 数据来源为中国气象局，权威可靠
- 500 次/天对低频个人 App 足够
- 但免费额度较少，且需实名认证
- 适合作为数据源之一，但不够充裕

**API 请求示例:**
```
GET https://api.weathercn.com/v1/weather/now?key=YOUR_KEY&location=beijing
```

---

### 3. 和风天气 dev.qweather.com (数据源含 CMA)

| 项目 | 说明 |
|------|------|
| 官网 | https://dev.qweather.com |
| API文档 | https://dev.qweather.com/docs/api/ |
| 数据源 | 中国气象局 + 全球数值模式, 分辨率 3-5 km |
| 免费额度 | **每月 5 万次请求** (0-50000/月免费) |
| 注册要求 | 需实名认证为个人开发者 |
| 数据内容 | 实况天气、7天预报、逐小时预报、空气质量 AQI、灾害预警、分钟级降水、生活指数等 |
| Android SDK | 官方提供 Android SDK |
| 调用限制 | 超过 5 万次/月后按量计费 (0.0007元/次) |

**结论: ✅ 首选推荐**
- 数据来源：中国气象局，权威准确
- 免费额度充裕：5 万次/月，低频 App 完全够用
- 官方提供 Android SDK，集成方便
- 覆盖全国 3000+ 市县区
- 支持城市名/城市 ID/经纬度查询
- 个人开发者友好

**API 请求示例:**
```
GET https://devapi.qweather.com/v7/weather/now?location=101010100&key=YOUR_KEY
```

**免费额度阶梯 (2025年3月起):**
| 月请求量 | 单价 |
|----------|------|
| 0~5万次 | 免费 |
| 之后 95万次 | 0.0007元/次 |
| 之后 400万次 | 0.0005元/次 |

---

### 4. 彩云天气 caiyunapp.com (CMA 战略合作伙伴)

| 项目 | 说明 |
|------|------|
| 官网 | https://caiyunapp.com/api/weather |
| API文档 | https://docs.caiyunapp.com/weather-api/ |
| 数据源 | 中国气象局战略合作伙伴，分钟级预报 |
| 免费额度 | **1000 次/天** (或 10000 次/6个月，新政策待确认) |
| 注册要求 | 需注册开发者账号 |
| 特色 | 分钟级降水预报，精度达 1 公里 |
| 免费版限制 | 不支持空气质量、预警、生活指数等 |

**结论: ⚠️ 备选方案**
- 分钟级降水预报是特色，但免费额度较少
- 免费版功能受限，缺少预警、AQI 等关键数据
- 适合需要短临降水的场景

---

## 二、全球免费 API 备选方案

### 5. Open-Meteo (开源免费，无需 API Key ⭐)

| 项目 | 说明 |
|------|------|
| 官网 | https://open-meteo.com |
| API文档 | https://open-meteo.com/en/docs |
| 数据源 | ECMWF, NOAA, DWD, CMA 等 15+ 国家气象局 |
| 免费额度 | **非商业使用：10,000 次/天，300,000 次/月** |
| 注册要求 | **无需注册，无需 API Key** |
| 数据内容 | 16天预报、历史数据(1940年起)、空气质量、地理编码 |
| 分辨率 | 全球 11 km，区域最高 1 km |
| 响应速度 | 通常 <10ms |
| 开源 | 完全开源 (AGPLv3) |

**结论: ⭐ 首选定为 "零成本全球备选"**
- 最适合原型开发和低频个人 App
- 无需注册、无需 API Key，零门槛
- 包含中国 CMA 数据源
- 缺点：不支持中文城市名搜索，需使用经纬度
- 缺点：非商业免费，商业需付费订阅

**API 请求示例:**
```
GET https://api.open-meteo.com/v1/forecast?latitude=39.9042&longitude=116.4074&current=temperature_2m&hourly=temperature_2m&timezone=Asia/Shanghai
```

---

### 6. OpenWeatherMap

| 项目 | 说明 |
|------|------|
| 官网 | https://openweathermap.org |
| 免费额度 | **100 万次/月**，60 次/分钟 |
| 注册要求 | 邮箱注册即可，无需信用卡 |
| 数据内容 | 实况天气、5天预报(3小时间隔)、空气质量、地理编码 |
| Android SDK | 社区 SDK 丰富 |
| 缺点 | 中国数据非 CMA 来源，准确度不如国内 API |

**结论: 适合全球应用，中国地区数据精度一般**

---

### 7. WeatherAPI.com

| 项目 | 说明 |
|------|------|
| 官网 | https://www.weatherapi.com |
| 免费额度 | **10 万次/月** |
| 注册要求 | 邮箱注册即可 |
| 数据内容 | 实况、3天预报、历史、空气质量等 |
| 限制 | 免费版需标注"数据来源 WeatherAPI.com" |

**结论: 备选，但非中国数据源**

---

## 三、综合推荐方案

### 🥇 主方案：和风天气 (QWeather)

```
├── 中国气象局权威数据
├── 5万次/月免费（低频完全够用）
├── 官方 Android SDK
├── 支持城市名/经纬度/城市ID 查询
└── 实况+7天预报+逐时+AQI+预警+生活指数 全功能
```

**架构建议：**
- 主数据源：和风天气，提供中国区权威实时天气、预报、AQI、预警、生活指数
- 缓存：Room 缓存实况30min / 预报2h
- 频率：每次打开App刷新 + 后台30-60min一次
- 无需降级方案，5万次/月对个人App绰绰有余

**成本：** 月均300次左右 → **完全免费**

### 🥈 备选：彩云天气 (Caiyun)

分钟级降水特色强，综合接口一次请求拿全数据，适合和和风天气搭配使用做补充。

### 🥉 备选：华风爱科 (WeatherCN)

CMA官方授权，500次/天，额度偏紧但数据权威。

---

## 四、Android 项目技术选型建议

| 组件 | 推荐 | 说明 |
|------|------|------|
| 网络请求 | Retrofit + OkHttp | Android 标准网络库 |
| JSON 解析 | Kotlinx Serialization / Moshi | - |
| 本地缓存 | Room | 离线缓存天气数据 |
| 图片加载 | Coil | 轻量级图片加载 |
| DI | Hilt | 依赖注入 |
| 架构 | MVVM + Repository | 标准 Android 架构 |
| 地图 | 高德/百度地图 SDK | 显示天气位置 |

---

---

## 六、三方案能力对比（和风天气 / Open-Meteo / 彩云天气）

### 6.1 和风天气 QWeather

| 维度 | 详情 |
|------|------|
| 请求方式 | RESTful GET，JWT/Bearer Token 认证 |
| 输入 | `location`=LocationID 或 `经度,纬度` (十进制, 小数点后2位) + `lang` + `unit` |
| 输出格式 | JSON (Gzip 压缩) |
| 实况 | `/v7/weather/now` → `obsTime,temp,feelsLike,icon,text,windDir,windScale,humidity,precip,pressure,vis,cloud,dew` |
| 逐日预报 | `/v7/weather/3d|7d|10d|15d|30d` → 逐日 `tempMax, tempMin, iconDay, textDay, windDirDay, humidity, precip, uvIndex, moonPhase` |
| 逐时预报 | `/v7/weather/24h|72h|168h` → 逐时 `temp, icon, text, windDir, windScale, humidity, precip, pop` |
| 分钟降水 | `/v7/minutely/5m` → 未来2小时逐分钟降水强度 |
| 预警 | `/v7/warning/now?location=` → 预警列表 (title, severity, text) |
| AQI | `/v7/air/now` → `aqi, category, pm2p5, pm10, no2, so2, co, o3` |
| 生活指数 | `/v7/indices/1d?type=` → 穿衣/洗车/感冒/紫外线等指数 |
| GeoAPI | `/v7/geo/city/lookup?location=北京` → 城市名→LocationID/经纬度 |
| 数据源标记 | 响应中 `refer.sources` 明确标注数据来源 |
| Android SDK | 官方提供 (API + Android SDK) |

### 6.2 Open-Meteo

| 维度 | 详情 |
|------|------|
| 请求方式 | RESTful GET，**无需认证**（商业版需 API Key） |
| 输入 | `latitude, longitude` (WGS84) + `hourly`/`daily`/`current` 变量列表 |
| 输出格式 | JSON / CSV / XLSX |
| 实况 | `current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m` |
| 逐日预报 | `daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code,sunrise,sunset,uv_index_max` (最多16天) |
| 逐时预报 | `hourly=temperature_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m` (最多16天) |
| 15分钟级 | `minutely_15=temperature_2m,precipitation` (仅欧美高分辨率区域) |
| 空气质量 | 独立 API `/v1/air-quality` → `european_aqi, us_aqi, pm2_5, pm10, nitrogen_dioxide` |
| 地理编码 | 独立 API `/v1/geocoding?name=Beijing` |
| 历史数据 | `/v1/archive` → ERA5 再分析数据 (1940年至今) |
| 可用模型 | CMA GRAPES Global 15km / ECMWF IFS 9km / GFS / ICON / AROME 等30+模型可选 |
| 中国CMA数据 | 支持 CMA GRAPES Global 模型 (15km, 10天预报, 每6小时更新) |
| 命中精度 | <10ms 响应，自选 `cell_selection` 策略 (land/sea/nearest) |
| 限制 | 非商业 1万次/天; 无中文按城市名搜索，只能用经纬度 |

### 6.3 彩云天气 Caiyun

| 维度 | 详情 |
|------|------|
| 请求方式 | RESTful GET，Token 认证 (URL Path 参数) |
| 输入 | `纬度,经度` (经度在前) + 查询参数 |
| 输出格式 | JSON |
| 端点 | `https://api.caiyunapp.com/v2.6/{token}/{longitude},{latitude}/weather` |
| 实况 | `result.realtime.temperature, humidity, skycon(CLEAR_DAY等), visibility, wind.speed/direction, pressure, apparent_temperature, precipitation.local.intensity, air_quality.aqi.chn/usa, life_index.ultraviolet/comfort` |
| 分钟降水 | `result.minutely` → 未来2小时逐分钟降水概率/强度 + 概要描述 |
| 逐时预报 | `result.hourly.temperature, precipitation, skycon, wind, humidity, cloudrate` (默认48h) |
| 逐日预报 | `result.daily.temperature(avg/max/min), skycon, precipitation(probability), wind` (默认7天) |
| 预警 | `result.alert` → 预警标题、内容、等级、发布时间 |
| 综合接口 | 一次请求同时返回 `realtime+minutely+hourly+daily+alert`，适合移动端 |
| 更新频率 | 实况 1min / 分钟降水 1min / 逐时 15min / 逐日 2h |
| 分辨率 | 实况 1km / 分钟降水 1km / 逐时 5km / 逐日 12km |
| forecast_keypoint | 返回面向用户的关键天气变化短文本（如"未来两小时不会下雨"） |
| 免费版限制 | 不支持空气质量、预警、生活指数的部分高级数据 (需看具体订阅) |
| 独特优势 | 一次综合请求拿全所有数据，减少 App 网络请求次数 |

### 6.4 快速对比总表

| 对比项 | 和风天气 | 彩云天气 |
|--------|----------|----------|
| 免费额度 | 5万次/月 | 1000次/天 |
| 需API Key | 是 (实名认证) | 是 |
| CMA数据 | ✅ 中国气象局 | ✅ 战略合作 |
| 中文城市搜索 | ✅ (GeoAPI) | ✅ |
| 分钟降水 | ✅ | ✅ (核心特色) |
| 空气质量AQI | ✅ | ✅ (免费版受限) |
| 灾害预警 | ✅ | ✅ |
| 生活指数 | ✅ | ✅ (免费版受限) |
| 历史数据 | 最近10天 | ❌ (无免费) |
| Android SDK | 官方提供 | 无 |
| 综合接口一次返回 | ❌ 分接口 | ✅ |
| 综合请求一次返回 | ❌ (分接口) | ✅ (current+hourly+daily) | ✅ (综合接口) |
| 数据标注来源 | ✅ 响应明确标注 | ✅ CC BY 4.0 | ✅ 需标注彩云LOGO |
| 最适合场景 | **主数据源**，全功能中国天气 | 零门槛备选/降级 | 分钟降水特色补充 |

### 6.5 请求示例对比

**和风天气 - 实时天气:**
```
GET https://devapi.qweather.com/v7/weather/now?location=101010100
Authorization: Bearer YOUR_TOKEN
→ {"code":"200","now":{"obsTime":"...","temp":"24","feelsLike":"26","text":"多云","windDir":"东南风","humidity":"72","precip":"0.0",...}}
```

**彩云天气 - 综合:**
```
GET https://api.caiyunapp.com/v2.6/TOKEN/116.3176,39.9760/weather?alert=true&dailysteps=1&hourlysteps=24
→ {"status":"ok","result":{"realtime":{...},"minutely":{...},"hourly":{...},"daily":{...},"alert":{...},"forecast_keypoint":"..."}}
```

---

## 七、各家数据源溯源（中国境内）

### 7.1 和风天气 QWeather

| 数据类型 | 数据来源 |
|---------|---------|
| 中国境内天气实况/预报 | **中国气象局 (CMA)** — 战略合作伙伴，二次分发 CMA 原始数据 |
| 分钟级降水 | 中国气象局雷达数据 |
| 空气质量 | 中国环境监测总站 + CMA |
| 预警 | 中央气象台 (NMC) |
| 全球数据 | ECMWF、NOAA 等国际数值模式 |
| 响应标注 | 每个 API 响应的 `refer.sources` 明确标注 "QWeather"、"NMC"、"ECMWF" |

**角色：** CMA 数据的商业分发与优化层。和风拿到 CMA 原始数据后做了格式标准化、接口友好化（城市名查询等）、分辨率提升（3-5km），再分发给开发者。不是直接转发 CMA 原始接口。

### 7.2 彩云天气 Caiyun

| 数据类型 | 数据来源 |
|---------|---------|
| 中国天气数据 | **中国气象科学数据共享服务网** (CMA 体系) |
| 预警 | **国家预警信息发布中心** |
| 台湾地区 | 台湾交通部中央气象局 |
| 全球数据 | NOAA (美国)、JMA (日本)、OpenWeatherMap、世界各国气象组织 |
| 空气质量 | PM25.in、环保部、WMO |
| 核心特色 | 自研 AI 预报算法，不是简单转发 CMA，而是融合多源数据做分钟级降水预测 |

**角色：** CMA 战略合作伙伴 + AI 预报引擎。彩云不只是转发 CMA 数据，而是用自有算法融合 CMA 雷达数据、社会化观测数据（用户反馈），产出分钟级降水预报，这是它与和风的本质差异。

### 7.3 华风爱科 WeatherCN

| 数据类型 | 数据来源 |
|---------|---------|
| 中国境内 | **中国气象局官方授权** — 强制使用 CMA LocalSource (id=7) |
| 全球 | **AccuWeather** 全球数据合作 |
| 背景 | 华风气象传媒集团运营，同属 CMA 公共气象服务体系 |
| 合规 | 中国天气网 (weather.com.cn) 同一运营方，CMA 体系内官方服务商 |

**角色：** CMA 体系内的官方商业服务商。与和风/彩云不同，华风爱科是 CMA 体系内部成员，不是"外部战略合作"关系，而是"直属官方授权"。境内数据强制走 CMA LocalSource。

### 7.4 总结

```
CMA 数据流向：
┌─────────────────────────────────────────┐
│          中国气象局 (CMA)                │
│   ├─ 气象科学数据共享服务网 (data.cma.cn) │
│   └─ 公共气象服务中心                     │
└──────────┬──────────────────────────────┘
           │
     ┌─────┴─────┬──────────┬──────────┐
     ▼           ▼          ▼          ▼
  data.cma.cn  华风爱科   和风天气    彩云天气
  (官方直出)  (体系内官方) (战略合作)  (战略合作+AI)
  
  数据精度: 原始     ←      CMA 源数据     →    AI增强
  接口友好: 差       ←      好             →   优(综合接口)
```

- **三者中国境内核心数据源都是 CMA**
- 华风 = CMA 体系内官方，最权威但接口最不友好
- 和风 = CMA 战略合作，做了数据优化和接口封装，最均衡
- 彩云 = CMA 战略合作 + 自有 AI 算法，分钟降水最强，但免费额度不足

---

## 八、App 代码架构设计

### 8.1 整体分层

```
┌─────────────────────────────────────────────────┐
│                  Widget 层                        │
│  WeatherWidget(AppWidgetProvider)                │
│  └─ onUpdate → PendingIntent → WorkManager       │
├─────────────────────────────────────────────────┤
│                  UI 层 (App内)                     │
│  Activity / Compose / Fragment                    │
│  └─ 观察 ViewModel.StateFlow                     │
├─────────────────────────────────────────────────┤
│              ViewModel / UseCase 层               │
│  WeatherViewModel / RefreshWeatherUseCase         │
│  └─ 调用 Repository，对外暴露 StateFlow           │
├─────────────────────────────────────────────────┤
│           中间层：WeatherRepository                │
│  （统一接口，业务层只依赖此接口）                    │
│  └─ 内部路由到当前激活的 DataSource                │
├──────────┬──────────┬──────────┬─────────────────┤
│   QWImpl  │ CaiyunImpl│ WCNImpl  │ 未来其他实现     │
│  (和风)   │ (彩云)    │ (华风)   │ (n选1可插拔)     │
├──────────┴──────────┴──────────┴─────────────────┤
│               Data Layer                          │
│  Retrofit + OkHttp + 各数据源 API 适配             │
│  Room (本地缓存) / DataStore (配置)               │
└─────────────────────────────────────────────────┘
```

### 8.2 统一中间层接口定义

所有数据源实现同一套接口，业务层（ViewModel/Widget）只依赖此接口：

```
interface WeatherDataSource {
    // 核心 - 小组件最常用
    suspend fun getCurrentWeather(location: Location): Result<WeatherNow>

    // 预报
    suspend fun getDailyForecast(location: Location, days: Int): Result<List<WeatherDaily>>
    suspend fun getHourlyForecast(location: Location, hours: Int): Result<List<WeatherHourly>>

    // 可选增强（部分数据源可能不支持）
    suspend fun getMinutePrecipitation(location: Location): Result<MinutePrecipitation?>
    suspend fun getAirQuality(location: Location): Result<AirQuality?>
    suspend fun getAlerts(location: Location): Result<List<Alert>>
    suspend fun getLifeIndices(location: Location): Result<LifeIndices?>
}
```

**统一数据模型（脱敏数据源差异）：**

| 模型 | 核心字段 | 用途 |
|------|---------|------|
| WeatherNow | temp, feelsLike, condition(枚举), humidity, windSpeed, windDir, pressure, precip, vis, updateTime, sourceLabel | 小组件主显示 |
| WeatherDaily | date, tempMax, tempMin, conditionDay, conditionNight, windDir, windScale, precip, uvIndex | App内预报列表 |
| WeatherHourly | time, temp, condition, windDir, windScale, precip, pop(降水概率) | App内逐时曲线 |
| MinutePrecipitation | summary(文本描述), intensity(Array), timePoints(Array) | 分钟降水图 |
| AirQuality | aqi, category, pm2p5, pm10, o3, no2, so2, co | App内详情 |
| Alert | title, severity, content, startTime, endTime | 预警卡片 |
| LifeIndices | uvIndex, comfort, carWash, dressing,感冒 | App内生活建议 |

关键设计：condition 用内部枚举统一映射，各 DataSource 实现内部做自家 code → 内部枚举 的转换。

### 8.3 Widget 更新机制

```
用户添加小组件
    ↓
AppWidgetProvider.onUpdate()
    ↓
PendingIntent → WorkManager (OneTimeWorkRequest)
    ↓ [周期性]
PeriodicWorkRequest (最小间隔15min)
    ↓
WeatherRefreshWorker.doWork()
    ↓
WeatherRepository.getCurrentWeather(location)
    ↓
Result<WeatherNow> → 成功则 RemoteViews.update() + AppWidgetManager.updateAppWidget()
                    → 失败则 RemoteViews 显示"点击重试"，不抛错
```

**关键设计点：**
- 用 WorkManager 而非单纯 PendingIntent：WorkManager 自动处理 Doze 省电模式、网络约束、重试策略
- Widget 更新间隔：正常 30min（兼顾实时性和配额）。用户可配置 15min/30min/60min
- 数据源切换：WorkManager 每次触发时都从 DataStore 读当前用户选择的数据源，重启 worker 即可
- Widget 点击事件：点击 Widget → PendingIntent → 打开 App 主 Activity；点击"刷新"区域 → 触发立即刷新 WorkRequest
- 首次安装后：App 启动时进行一次初始化刷新，之后按周期 Worker 刷新

### 8.4 数据源 n 选 1 切换方案

```
用户设置 → 选择数据源 → DataStore 记录
    ↓
Repository 初始化时/每次调用时 → 读 DataStore → 获取当前激活的 DataSource 实现
    ↓
调用 dataSource.getCurrentWeather(location)
    ↓
切换即时生效，下次 API 调用走新数据源
```

**设计细节：**
- DataSourceManager 持有所有 DataSource 实例，按 DataSourceType 索引
- Repository 从 DataStore 读取当前类型，路由到对应实例
- 切换后自动清空本地 Room 缓存，避免显示旧数据源残留
- 每个 DataSource 实现各自管理自己的 API Key、认证、限流
- 数据源可用性探测：每个 DataSource 实现一个 `suspend fun healthCheck(): Boolean`，用于切换时的前置校验

### 8.5 需要补充规划的内容

| 类别 | 当前缺失 | 建议方案 |
|------|---------|---------|
| **数据源健康检查** | 如果用户切到某个数据源但 Key 已过期怎么办 | 切换前 probe 一次 `healthCheck()`，失败则提示用户 |
| **限流与配额管理** | 各数据源免费额度不同，超量后表现不同 | 每个 DataSource 内统计调用次数，接近阈值时告警或自动降级 |
| **降级策略** | 主数据源挂了/超量了怎么办 | 支持配置降级链：A→B→C，自动尝试下一个 |
| **多城市管理** | 只考虑了单城市 | Room 表存城市列表，Widget 可配置绑定某个城市 |
| **定位策略** | 每次刷新都 GPS 定位？ | FusedLocationProvider 获取 + 上次位置兜底 + 手动选城市兜底 |
| **离线缓存** | 无网络时 Widget 空白 | Room 缓存最近一次数据，无网时显示缓存+标注"离线" |
| **权限流程** | 位置权限未规划 | 首次启动引导授权；拒绝后降级为手动选城市 |
| **主题适配** | Widget 深色模式 | 跟随系统 theme，RemoteViews 使用两套布局 |
| **国际化** | 天气描述仅中文 | 数据源多语言参数 + strings.xml 兜底 |
| **冷启动优化** | App 第一次安装，Widget 还没数据 | App 初始化时 WorkManager 立即执行一次刷新 |
| **小组件配置** | 用户添加 Widget 时选择城市 | Widget 配置 Activity (AppWidgetProviderInfo configure) |
| **API Key 管理** | Key 存在哪 | DataStore 加密存储，首次使用引导输入 |
| **数据源鉴权差异** | 和风 JWT，彩云 Token，形式不同 | DataSourceImpl 内部各自处理，对外透明 |
| **错误标准化** | 各数据源错误码不同 | 统一 Error 密封类，DataSourceImpl 内做映射 |

### 8.6 推荐 MVP 架构路径

```
Phase 1 (验证期):
  固定数据源(和风) + 单城市 + Widget + 无缓存
  → 确认 API 可用，小组件渲染正常

Phase 2 (完善期):
  Room 缓存 + 多城市管理 + 多数据源架构 + 设置页

Phase 3 (增强期):
  数据源切换 UI + 降级策略 + 限流管理 + 深色 Widget
```

---

### 8.7 位置 ID 体系分析 & 数据模型

**三家 Location ID 溯源：**

| 来源 | 北京ID | 海淀区ID | 朝阳区ID | 归属 |
|------|--------|---------|---------|------|
| 和风 LocationID | `101010100` | `101010200` | `101010300` | 和风自有，继承自中国天气网旧体系 |
| 华风 LocationKey | `101924` | — | — | 华风/AccuWeather 自有 |
| GB/T 2260 (Adcode) | `110000` | `110108` | `110105` | ✅ **国家标准** |
| 彩云 | 无ID，纯经纬度 | — | — | — |

**和风 LocationID 结构分析：** `101` + 2位省码 + 2位市码 + 2位区码
- `101 01 01 00` = 北京（区码00表市级本身）
- `101 01 01 02` = 海淀
- 并非国家标准，而是和风/中国天气网自有的天气服务编码

**GB/T 2260（Adcode）是国家标准行政区划代码：**
- 6位数字，全国统一，民政部维护，每年更新
- 粒度到区县级：海淀 `110108`、朝阳 `110105`
- 不依赖任何天气服务商，纯行政体系标识
- 和风 GeoAPI 也支持直接传 Adcode 查天气

**结论：Adcode (GB/T 2260) 最适合作为统一内部 ID**

理由：
1. 区县级粒度，正好满足"一个区内天气相对一致"的诉求
2. 国家标准的通用性，不绑定任何天气服务商
3. 和风本身支持用 Adcode 查天气（`location=110108`），未来其他数据源也可通过 Adcode 做映射
4. 对非中国地区可用经纬度 hash 兜底，扩展性好

**三数据源位置参数要求对比：**

| | 和风 QWeather | 彩云 Caiyun | 华风 WeatherCN |
|---|---|---|---|
| **查天气需传** | LocationID 或 `lat,lng` | `lon,lat`（经度在前） | LocationKey |
| **城市搜索** | GeoAPI 输入"北京"返回列表 | 无（需自行转经纬度） | 输入城市名或 `lat,lng` 返回 Key |
| **坐标系** | WGS-84 | WGS-84 | 中国大陆需 GCJ-02 |
| **城市唯一标识** | LocationID: `101010100` | 无，用经纬度 | LocationKey: 字符串 ID |

**统一位置数据模型（Sealed Class + 子类专有字段）：**

```kotlin
sealed class WeatherLocation {
    // 统一ID格式: 省名_区县名_adcode，如 "北京市_海淀区_110108"
    // 仅城市级: "北京市_110000"
    // 非中国:   "美国_洛杉矶_countryHash"
    abstract val id: String
    abstract val name: String         // 海淀区
    abstract val province: String     // 北京市（省级，带"市"字）
    abstract val city: String         // 北京市（市级）
    abstract val district: String?    // 海淀区（区县级，市级时为null）
    abstract val country: String      // 中国
    abstract val latitude: Double
    abstract val longitude: Double    // WGS-84，华风DataSource内转GCJ-02
}

// 彩云/纯经纬度/通用定位：无数据源专有字段
data class SimpleLocation(
    override val id: String,
    override val name: String,
    override val province: String,
    override val city: String,
    override val district: String?,
    override val country: String,
    override val latitude: Double,
    override val longitude: Double,
) : WeatherLocation()

// 和风城市定位：携带 LocationID
data class QwLocation(
    override val id: String,
    override val name: String,
    override val province: String,
    override val city: String,
    override val district: String?,
    override val country: String,
    override val latitude: Double,
    override val longitude: Double,
    val locationId: String,           // 101010200
) : WeatherLocation()

// 华风城市定位：携带 LocationKey
data class WcnLocation(
    override val id: String,
    override val name: String,
    override val province: String,
    override val city: String,
    override val district: String?,
    override val country: String,
    override val latitude: Double,
    override val longitude: Double,
    val locationKey: String,          // 101924
) : WeatherLocation()
```

**DataSource 适配规则：**
- QWImpl: `if (location is QwLocation) → 用 locationId`，否则 fallback 到经纬度
- WcnImpl: `if (location is WcnLocation) → 用 locationKey`，否则经纬度（自行转 GCJ-02）
- CaiyunImpl: 始终用经纬度，无需额外逻辑
- Repository 不感知子类，只传 `WeatherLocation` 给当前 DataSource

**ID 格式说明：**
| 级别 | 格式 | 示例 |
|------|------|------|
| 有区县 | 省名_区县名_adcode | `北京市_海淀区_110108` |
| 仅市级 | 省名_adcode | `北京市_110000` |
| 非中国 | 国名_城市名_hash | `美国_洛杉矶_us_hash` |

**SP 命名规划：**
- 总 SP: `weather_locations` — 所有已添加城市列表（key=locationId, value=序列化 WeatherLocation）
- 每城市 SP: `weather_loc_{id}` — 直接用 id 字段做 key
  - 海淀: `weather_loc_北京市_海淀区_110108`
  - 仅北京: `weather_loc_北京市_110000`
- 命名带省市区名，调试时一眼认出哪个位置

**缓存 Key 命名（带时间级前缀）：**

```kotlin
now              // 当前实况
d_20260622       // 日级预报（一次覆盖全天）
h_20260622_15    // 小时级预报（日期_小时，24h制）
m_20260622_1515  // 分钟级数据（日期_小时分钟）
last_update      // 最新刷新时间戳
```

- 前缀 `d_` / `h_` / `m_` 一眼区分层级，后段标准时间格式，不存在误用
- `now` 和 `last_update` 无前缀，最常读写

---

### 8.8 数据源接入方式 & 测试记录

| 数据源 | Android SDK | HTTP 方式 | 认证方式 | 免费额度 | 获取地址 |
|-------|:---:|:---------:|---------|---------|---------|
| **和风 QWeather** | ✅ 官方 JAR | ✅ REST API | API KEY (`X-QW-Api-Key` / `?key=`) 或 JWT | 5万次/月 | https://dev.qweather.com |
| **彩云 Caiyun** | ❌ 无 | ✅ REST API | Token (URL Path) 或 AppKey+Secret | 1万次/6个月 | https://platform.caiyunapp.com |
| **华风 WeatherCN** | ❌ 无 | ✅ REST API | API KEY (`?apikey=`) | 500次/天 | https://platform.weathercn.com |

**决策：三数据源统一走 HTTP 方式，不用 SDK。**

#### 彩云 API 测试记录

**第一次测试（演示 Token 已耗尽）：**
- 演示 Token: `TAkhjf8d1nlSlspN` → `quota is exhausted`

**第二次测试（用户 Token & AppKey）：**
- URL: `https://api.caiyunapp.com/v2.6/{token}/{lon},{lat}/weather?lang=zh_CN`
- Token: `REDACTED` → `quota is exhausted`（Token 有效，但无可用额度）
- AppKey: `REDACTED` + AppSecret → 同上
- **结论：请求格式完全正确，凭据有效，但账号免费额度未激活**
- 测试位置：北京昌平 `116.231204,40.220660`，平顶山卫东区 `113.3302298,33.7364538`
- 坐标系: WGS-84，彩云要求经度在前（`lon,lat`）

**AppKey 签名方式（未来编码参考）：**
```
签名字符串: GET:{path}:{query_str}:{app_key}:{nonce}:{timestamp}
签名算法: HMAC-SHA256(app_secret, string_to_sign) → URL Safe Base64
Headers: x-cy-nonce, x-cy-timestamp, x-cy-signature
```

**待办：** 用户需在 [platform.caiyunapp.com](https://platform.caiyunapp.com) → 充值页面 → 确认个人开发者认证是否已审核通过并生效，或手动选择免费套餐。

---

## 九、Implementation Progress

### Architecture & Design
- **Data source interface**: `WeatherDataSource` with unified `WeatherNow`, `WeatherDaily`, `WeatherHourly` models
- **DataSource n-select-1**: DataStore records user selection, `WeatherRepository` routes to active DataSource on each call
- **Location**: sealed class `WeatherLocation` with `WcnLocation`/`QwLocation`/`SimpleLocation` subclasses carrying data-source-specific IDs
- **DI**: Manual via `AppContainer` singleton (no Hilt)
- **Widget**: RemoteViews directly (no Glance dependency)
- **UI**: XML layouts (`activity_main.xml`, `weather_widget.xml`)
- **Network**: Plain OkHttp calls for all three data sources (no vendor SDKs)
- **Background refresh**: WorkManager `PeriodicWorkRequest` (30min) + `OneTimeWorkRequest` on-demand via `WeatherRefreshReceiver`
- **Config persistence**: DataStore for settings (data source selection)
- **Weather data cache**: SharedPreferences per location (Phase 2)

### Build System
- **AGP**: 9.2.0
- **Gradle**: 9.4.1
- **JDK**: 26.0.1 (Homebrew `REDACTED`)
- **Flags**: `android.builtInKotlin=false`, `android.newDsl=false`

### API Key & Host Changes (vs initial research)
| Data Source | Host | Auth | Notes |
|-------------|------|------|-------|
| **WeatherCN** | `openapi.weathercn.com` | `?apikey=<key>` | NOT the old HMAC-based `apidev.weathercn.com`; docs at `platform.weathercn.com/apidoc/` |
| **QWeather** | `{id}.re.qweatherapi.com` (per-developer custom) | `X-QW-Api-Key` header | Public `devapi.qweather.com` deprecated since 2026 |
| **Caiyun** | `api.caiyunapp.com/v2.6` | Token in URL path `/{token}/` | Token `REDACTED` working after quota issue resolved |

### Data Source Test Results (all working as of 2026-06-22)
- **WeatherCN**: Beijing (LocationKey `101924`) → 22°C, rain ✓
- **QWeather**: Beijing (ID `101010100`) → 22°C, light rain ✓
- **Caiyun**: Beijing lat-lng → 21.46°C, PARTLY_CLOUDY_NIGHT ✓

### Known Issues & Fixes
- **Auto-refresh on startup**: ViewModel `init` never called `refreshWeather()`, causing blank UI on launch. Fixed by adding `refreshWeather()` call in `WeatherViewModel.init`.
- **adb pair protocol fault**: `adb pair` on macOS returns "protocol fault (couldn't read status message)". Workaround: user manually paired from their machine, then APK deployed via `adb connect` + `adb install`.

### Key Decisions
- **Default data source**: WeatherCN (华风天气)
- **Default city**: Beijing (WeatherCN LocationKey `101924`)
- **API keys in BuildConfig**: via `buildConfigField` in `app/build.gradle.kts` (personal project, no financial loss if leaked)
- **Coroutine in Worker**: `runBlocking` to call suspend repository methods from `Worker.doWork()`

### Relevant Files
```
Weather/
├── app/build.gradle.kts              # Dependencies, BuildConfig fields, AGP config
├── app/src/main/java/com/xilingyuli/weather/
│   ├── MainActivity.kt               # Entry point, observes ViewModel
│   ├── WeatherApp.kt                 # Application class, initializes AppContainer
│   ├── data/
│   │   ├── datasource/
│   │   │   ├── WeatherDataSource.kt  # Unified interface
│   │   │   ├── WeatherCNDataSource.kt
│   │   │   ├── QWeatherDataSource.kt
│   │   │   ├── CaiyunDataSource.kt
│   │   │   └── DataSourceType.kt
│   │   ├── model/
│   │   │   ├── WeatherNow.kt
│   │   │   ├── WeatherDaily.kt
│   │   │   ├── WeatherHourly.kt
│   │   │   └── WeatherLocation.kt
│   │   └── repository/
│   │       ├── WeatherRepository.kt
│   │       ├── SettingsRepository.kt
│   │       └── WeatherCache.kt       # Added Phase 2
│   ├── di/
│   │   └── AppContainer.kt
│   ├── ui/
│   │   ├── WeatherViewModel.kt
│   │   └── Defaults.kt
│   └── widget/
│       ├── WeatherWidget.kt
│       ├── WeatherRefreshWorker.kt
│       └── WeatherRefreshReceiver.kt
└── res/layout/
    ├── activity_main.xml
    └── weather_widget.xml
```

---

## 五、参考链接

- 和风天气开发服务: https://dev.qweather.com
- 华风爱科天气平台: https://platform.weathercn.com
- OpenWeatherMap: https://openweathermap.org
- WeatherAPI: https://www.weatherapi.com
- 中国气象数据网: https://data.cma.cn
- 彩云天气 API: https://caiyunapp.com/api/weather
