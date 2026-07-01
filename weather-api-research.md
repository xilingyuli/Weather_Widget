# 天气 API 调研报告

> 调研日期: 2026-06-21
> 目标: 为 Android 天气 App 寻找免费、数据可靠、低频调用友好的天气数据 API
> 核心诉求: 数据源首选中国气象局(CMA)，准确且免费

---

# 一、数据源调研

## 1. 中国气象数据网 data.cma.cn

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

## 2. 华风爱科 weathercn.com (中国气象局官方授权)

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

API 请求示例:
```
GET https://api.weathercn.com/v1/weather/now?key=YOUR_KEY&location=beijing
```

---

## 3. 和风天气 dev.qweather.com (数据源含 CMA)

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

免费额度阶梯 (2025年3月起):

| 月请求量 | 单价 |
|----------|------|
| 0~5万次 | 免费 |
| 之后 95万次 | 0.0007元/次 |
| 之后 400万次 | 0.0005元/次 |

---

## 4. 彩云天气 caiyunapp.com (CMA 战略合作伙伴)

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

## 5. Open-Meteo (开源免费，无需 API Key ⭐)

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

---

## 6. OpenWeatherMap

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

## 7. WeatherAPI.com

| 项目 | 说明 |
|------|------|
| 官网 | https://www.weatherapi.com |
| 免费额度 | **10 万次/月** |
| 注册要求 | 邮箱注册即可 |
| 数据内容 | 实况、3天预报、历史、空气质量等 |
| 限制 | 免费版需标注"数据来源 WeatherAPI.com" |

**结论: 备选，但非中国数据源**

---

## 综合推荐方案

### 🥇 主方案：和风天气 (QWeather)

```
├── 中国气象局权威数据
├── 5万次/月免费（低频完全够用）
├── 官方 Android SDK
├── 支持城市名/经纬度/城市ID 查询
└── 实况+7天预报+逐时+AQI+预警+生活指数 全功能
```

架构建议：
- 主数据源：和风天气，提供中国区权威实时天气、预报、AQI、预警、生活指数
- 缓存：SP 持久化 + ViewModel 内存缓存，每次打开刷新 + 后台 30min
- 频率：每次打开App刷新 + 后台30-60min一次
- 无需降级方案，5万次/月对个人App绰绰有余

**成本：** 月均300次左右 → **完全免费**

### 🥈 备选：彩云天气 (Caiyun)

分钟级降水特色强，综合接口一次请求拿全数据，适合和和风天气搭配使用做补充。

### 🥉 备选：华风爱科 (WeatherCN)

CMA官方授权，500次/天，额度偏紧但数据权威。

---

## 各家数据源溯源（中国境内）

### 和风天气 QWeather

| 数据类型 | 数据来源 |
|---------|---------|
| 中国境内天气实况/预报 | **中国气象局 (CMA)** — 战略合作伙伴，二次分发 CMA 原始数据 |
| 分钟级降水 | 中国气象局雷达数据 |
| 空气质量 | 中国环境监测总站 + CMA |
| 预警 | 中央气象台 (NMC) |
| 全球数据 | ECMWF、NOAA 等国际数值模式 |
| 响应标注 | 每个 API 响应的 `refer.sources` 明确标注 "QWeather"、"NMC"、"ECMWF" |

**角色：** CMA 数据的商业分发与优化层。和风拿到 CMA 原始数据后做了格式标准化、接口友好化（城市名查询等）、分辨率提升（3-5km），再分发给开发者。

### 彩云天气 Caiyun

| 数据类型 | 数据来源 |
|---------|---------|
| 中国天气数据 | **中国气象科学数据共享服务网** (CMA 体系) |
| 预警 | **国家预警信息发布中心** |
| 台湾地区 | 台湾交通部中央气象局 |
| 全球数据 | NOAA (美国)、JMA (日本)、OpenWeatherMap、世界各国气象组织 |
| 空气质量 | PM25.in、环保部、WMO |
| 核心特色 | 自研 AI 预报算法，融合多源数据做分钟级降水预测 |

**角色：** CMA 战略合作伙伴 + AI 预报引擎。彩云不只是转发 CMA 数据，而是用自有算法融合 CMA 雷达数据、社会化观测数据（用户反馈），产出分钟级降水预报。

### 华风爱科 WeatherCN

| 数据类型 | 数据来源 |
|---------|---------|
| 中国境内 | **中国气象局官方授权** — 强制使用 CMA LocalSource (id=7) |
| 全球 | **AccuWeather** 全球数据合作 |
| 背景 | 华风气象传媒集团运营，同属 CMA 公共气象服务体系 |
| 合规 | 中国天气网 (weather.com.cn) 同一运营方，CMA 体系内官方服务商 |

**角色：** CMA 体系内的官方商业服务商。境内数据强制走 CMA LocalSource。

### 总结

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
- 彩云 = CMA 战略合作 + 自有 AI 算法，分钟降水最强

---

# 二、API 使用方式

## 和风天气 QWeather

| 维度 | 详情 |
|------|------|
| 请求方式 | RESTful GET，JWT/Bearer Token 认证 |
| 输入 | `location`=LocationID 或 `经度,纬度` (十进制, 小数点后2位) + `lang` + `unit` |
| 输出格式 | JSON (Gzip 压缩) |
| 实况 | `/v7/weather/now` → `obsTime,temp,feelsLike,icon,text,windDir,windScale,humidity,precip,pressure,vis,cloud,dew` |
| 逐日预报 | `/v7/weather/3d\|7d\|10d\|15d\|30d` → 逐日 `tempMax, tempMin, iconDay, textDay, windDirDay, humidity, precip, uvIndex, moonPhase` |
| 逐时预报 | `/v7/weather/24h\|72h\|168h` → 逐时 `temp, icon, text, windDir, windScale, humidity, precip, pop` |
| 分钟降水 | `/v7/minutely/5m` → 未来2小时逐分钟降水强度 |
| 预警 | `/v7/warning/now?location=` → 预警列表 (title, severity, text) |
| AQI | `/v7/air/now` → `aqi, category, pm2p5, pm10, no2, so2, co, o3` |
| 生活指数 | `/v7/indices/1d?type=` → 穿衣/洗车/感冒/紫外线等指数 |
| GeoAPI | `/v7/geo/city/lookup?location=北京` → 城市名→LocationID/经纬度 |
| 数据源标记 | 响应中 `refer.sources` 明确标注数据来源 |
| Android SDK | 官方提供 (API + Android SDK) |

## 彩云天气 Caiyun

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
| forecast_keypoint | 返回面向用户的关键天气变化短文本 |
| 免费版限制 | 不支持空气质量、预警、生活指数的部分高级数据 |

## Open-Meteo

| 维度 | 详情 |
|------|------|
| 请求方式 | RESTful GET，**无需认证**（商业版需 API Key） |
| 输入 | `latitude, longitude` (WGS84) + `hourly`/`daily`/`current` 变量列表 |
| 输出格式 | JSON / CSV / XLSX |
| 实况 | `current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m` |
| 逐日预报 | `daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code,sunrise,sunset,uv_index_max` (最多16天) |
| 逐时预报 | `hourly=temperature_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m` (最多16天) |
| 空气质量 | 独立 API `/v1/air-quality` → `european_aqi, us_aqi, pm2_5, pm10, nitrogen_dioxide` |
| 地理编码 | 独立 API `/v1/geocoding?name=Beijing` |
| 历史数据 | `/v1/archive` → ERA5 再分析数据 (1940年至今) |
| 可用模型 | CMA GRAPES Global 15km / ECMWF IFS 9km / GFS / ICON 等30+模型 |
| 限制 | 非商业 1万次/天; 无中文按城市名搜索，只能用经纬度 |

## 快速对比总表

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
| Android SDK | 官方提供 | 无 |
| 综合接口一次返回 | ❌ 分接口 | ✅ |
| 最适合场景 | **主数据源** | 分钟降水特色补充 |

## 请求示例对比

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

## 位置 ID 体系分析

**三家 Location ID 溯源：**

| 来源 | 北京ID | 海淀区ID | 朝阳区ID | 归属 |
|------|--------|---------|---------|------|
| 和风 LocationID | `101010100` | `101010200` | `101010300` | 和风自有，继承自中国天气网旧体系 |
| 华风 LocationKey | `101924` | — | — | 华风/AccuWeather 自有 |
| GB/T 2260 (Adcode) | `110000` | `110108` | `110105` | ✅ **国家标准** |
| 彩云 | 无ID，纯经纬度 | — | — | — |

和风 LocationID 结构：`101` + 2位省码 + 2位市码 + 2位区码，并非国家标准。

GB/T 2260（Adcode）是国家标准行政区划代码：6位数字，全国统一，粒度到区县级。**Adcode 最适合作为统一内部 ID。**

**三数据源位置参数要求：**

| | 和风 QWeather | 彩云 Caiyun | 华风 WeatherCN |
|---|---|---|---|
| **查天气需传** | LocationID 或 `lat,lng` | `lon,lat`（经度在前） | LocationKey |
| **城市搜索** | GeoAPI 输入"北京"返回列表 | 无（需自行转经纬度） | 输入城市名或 `lat,lng` 返回 Key |
| **坐标系** | WGS-84 | WGS-84 | 中国大陆需 GCJ-02 |
| **城市唯一标识** | LocationID: `101010100` | 无，用经纬度 | LocationKey: 字符串 ID |

## 接入方式 & 测试记录

| 数据源 | Android SDK | HTTP 方式 | 认证方式 | 免费额度 |
|-------|:---:|:---------:|---------|---------|
| **和风 QWeather** | ✅ 官方 JAR | ✅ REST API | API KEY 或 JWT | 5万次/月 |
| **彩云 Caiyun** | ❌ 无 | ✅ REST API | Token (URL Path) 或 AppKey+Secret | 1万次/6个月 |
| **华风 WeatherCN** | ❌ 无 | ✅ REST API | API KEY (`?apikey=`) | 500次/天 |

**决策：三数据源统一走 HTTP 方式，不用 SDK。**

### API Key & Host 变化记录

| Data Source | Host | Auth | Notes |
|-------------|------|------|-------|
| **WeatherCN** | `openapi.weathercn.com` | `?apikey=<key>` | 非旧版 HMAC-based；文档见 `platform.weathercn.com/apidoc/` |
| **QWeather** | `{id}.re.qweatherapi.com` | `X-QW-Api-Key` header | 公共 `devapi.qweather.com` 2026年已废弃 |
| **Caiyun** | `api.caiyunapp.com/v2.6` | Token in URL path | 解决额度问题后可用 |

### 测试结果 (2026-06-22)

- **WeatherCN**: Beijing (LocationKey `101924`) → 22°C, rain ✓
- **QWeather**: Beijing (ID `101010100`) → 22°C, light rain ✓
- **Caiyun**: Beijing lat-lng → 21.46°C, PARTLY_CLOUDY_NIGHT ✓

---

# 三、项目架构设计

## 技术选型

| 组件 | 选择 | 说明 |
|------|------|------|
| UI | XML layout | 不用 Compose |
| 网络请求 | OkHttp | 不用 Retrofit，三数据源统一 HTTP |
| JSON 解析 | Gson | - |
| 本地缓存 | SharedPreferences | 不做 Room |
| DI | 手动工厂 | `AppContainer` 单例，不用 Hilt |
| 架构 | MVVM + Repository | ViewModel 持有 StateFlow |

```
┌─────────────────────────────────────────────────┐
│                  Widget 层                        │
│  WeatherWidget(AppWidgetProvider)                │
│  └─ onUpdate → PendingIntent → WorkManager       │
├─────────────────────────────────────────────────┤
│                  UI 层 (App内)                     │
│  Activity (XML layout)                              │
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
│  OkHttp + 各数据源 API 适配                         │
│  SharedPreferences (本地缓存) / DataStore (配置)    │
└─────────────────────────────────────────────────┘
```

## 统一数据源接口

```
interface WeatherDataSource {
    suspend fun getCurrentWeather(location: Location): Result<WeatherNow>
    suspend fun getDailyForecast(location: Location, days: Int): Result<List<WeatherDaily>>
    suspend fun getHourlyForecast(location: Location, hours: Int): Result<List<WeatherHourly>>
    suspend fun getMinutePrecipitation(location: Location): Result<MinutePrecipitation?>
    suspend fun getAirQuality(location: Location): Result<AirQuality?>
    suspend fun getAlerts(location: Location): Result<List<Alert>>
    suspend fun getLifeIndices(location: Location): Result<LifeIndices?>
}
```

## 统一数据模型

| 模型 | 核心字段 | 用途 |
|------|---------|------|
| WeatherNow | temp, feelsLike, condition(枚举), humidity, windSpeed, windDir, pressure, precip, vis, updateTime, sourceLabel | 小组件主显示 |
| WeatherDaily | date, tempMax, tempMin, conditionDay, conditionNight, windDir, windScale, precip, uvIndex | App内预报列表 |
| WeatherHourly | time, temp, condition, windDir, windScale, precip, pop(降水概率) | App内逐时曲线 |
| MinutePrecipitation | summary(文本描述), intensity(Array), timePoints(Array) | 分钟降水图 |
| AirQuality | aqi, category, pm2p5, pm10, o3, no2, so2, co | App内详情 |
| Alert | title, severity, content, startTime, endTime | 预警卡片 |
| LifeIndices | uvIndex, comfort, carWash, dressing, 感冒 | App内生活建议 |

## 位置数据模型（Sealed Class）

```kotlin
sealed class WeatherLocation {
    // 统一ID格式: 省名_区县名_adcode，如 "北京市_海淀区_110108"
    abstract val id: String
    abstract val name: String         // 海淀区
    abstract val province: String     // 北京市（省级）
    abstract val city: String         // 北京市（市级）
    abstract val district: String?    // 海淀区（区县级，市级时为null）
    abstract val country: String      // 中国
    abstract val latitude: Double
    abstract val longitude: Double    // WGS-84
}

// 彩云/纯经纬度
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

// 和风：携带 LocationID
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

// 华风：携带 LocationKey
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

DataSource 适配规则：
- QWImpl: 优先用 `locationId`，fallback 经纬度
- WcnImpl: 优先用 `locationKey`，fallback 经纬度（自行转 GCJ-02）
- CaiyunImpl: 始终用经纬度

## Widget 更新机制

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
                    → 失败则 RemoteViews 显示"点击重试"
```

关键设计点：
- WorkManager 自动处理 Doze/网络约束/重试
- 刷新间隔 30min（兼顾实时性和配额），可配置
- 数据源切换时从 DataStore 读，重启 worker 即刻生效

## 数据源 n选1 切换方案

```
用户设置 → 选择数据源 → DataStore 记录
    ↓
Repository → 读 DataStore → 获取当前激活的 DataSource 实现
    ↓
调用 dataSource.getCurrentWeather(location)
    ↓
切换即时生效，下次 API 调用走新数据源
```

设计细节：
- DataSourceManager 持有所有 DataSource 实例，按 DataSourceType 索引
- 切换后自动清空本地缓存
- 每个 DataSource 实现各自管理 API Key/认证/限流
- 切换前置 `healthCheck()` 探活

## 存储命名规划

SP 命名：
- 总: `weather_locations` — 所有已添加城市列表
- 每城市: `weather_loc_{id}` — 如 `weather_loc_北京市_海淀区_110108`

缓存 Key 命名（带时间级前缀）：
```kotlin
now              // 当前实况
d_20260622       // 日级预报
h_20260622_15    // 小时级预报
m_20260622_1515  // 分钟级数据
last_update      // 最新刷新时间戳
```

- 前缀 `d_` / `h_` / `m_` 一眼区分层级，后段标准时间格式，不存在误用
- `now` 和 `last_update` 无前缀，最常读写

## 设计决策记录

### 数据存储方案

**选型：SharedPreferences + ViewModel 层内存缓存，不用 Room。**

理由：天气数据量极小（几个城市 ×几天），SP 无性能瓶颈。读写模式简单（全部写入/全部读取），不需要 Room Entity/DAO/Migration 的复杂度。

**JSON 结构：彩云风格（按变量分数组），非扁平化。**

```
{
  "temperature": [{ "date": "2026-06-22", "max":30, "min":22, "avg":26 }, ...],
  "precipitation": [{ "date": "2026-06-22", "max":0, "probability":10 }, ...],
  "skycon": [{ "date": "2026-06-22", "value": "CLEAR_DAY" }, ...],
  "wind": [{ "date": "2026-06-22", "speed":10, "direction":180 }, ...]
}
```

理由：按变量分数组比扁平记录更适合按维度查询（只看温度曲线/降水概率），与彩云 API 原始结构一致，DataSource 内映射成本最低。

**Key 命名：语义化前缀区分时间粒度，不用毫秒时间戳。**
- `now` — 实时数据
- `d_YYYYMMDD` — 日级预报
- `h_YYYYMMDD_HH` — 小时级预报
- `m_YYYYMMDD_HHmm` — 分钟级数据
- `last_update` — 最新刷新时间戳

写入时直接覆盖同名 key，天然保证最新数据。Widget 读取直接按 key 取，不用遍历找最新。

### 位置 ID 演化

**最终方案：GB/T 2260 Adcode（国家标准行政区划代码）作为统一内部 ID。**

演化链：
1. 初始：各自用数据源 ID，基类存三个字段
2. 发现和风 LocationID 并非国家标准，是和风自有编码
3. 找到 GB/T 2260 Adcode（如 `110108`），6 位区县级粒度，全国统一，和风也支持直接用 Adcode 查询
4. 最终 ID 格式：`省名_区名_adcode`，如 `北京市_海淀区_110108`。仅有市级用 `北京市_110000`

SP 表名：`weather_loc_{id}`，如 `weather_loc_北京市_海淀区_110108`。

### 位置数据模型

**基类只放通用字段，数据源专有字段放到子类（Sealed Class）。**

- 基类字段：id、name、province、city、district、country、latitude、longitude
- 和风子类 `QwLocation` 额外存 `locationId: String`
- 华风子类 `WcnLocation` 额外存 `locationKey: String`
- 彩云用 `SimpleLocation`，不需要额外字段
- 命名用全中文可读（province/city/district），不用 adm1/adm2
- 时区不放位置信息里，属于时间范畴，DataSource 内部自行处理

### 其他架构决策

- 降级与切换合并：只做手动切换，不做自动降级。限流/超量视为失败类型，失败后手动切
- 多城市预留：数据结构和接口支持，功能延后
- 位置获取：定位和城市封装成统一对象，核心逻辑不关心来源
- 技术栈：传统 XML layout + 手动工厂 DI，不选 Compose/Hilt

## 补充规划

| 类别 | 当前缺失 | 建议方案 |
|------|---------|---------|
| 数据源健康检查 | Key 过期无感知 | 切换前 `healthCheck()` 探活 |
| 限流与配额管理 | 超量后无提示 | DataSource 内部计数，接近阈值告警 |
| 降级策略 | 主数据源不可用 | 支持降级链 A→B→C |
| 多城市管理 | 仅单城市 | SP/DataStore 存城市列表 |
| 离线缓存 | 无网络时空白 | SP 缓存最近数据 + 标注"离线" |
| 权限流程 | 未规划 | 首次引导授权，拒绝后手动选城市 |
| 主题适配 | Widget 深色模式 | RemoteViews 双套布局 |
| 冷启动优化 | 首次无数据 | App 初始化时立即刷新一次 |
| 小组件配置 | 添加时选城市 | Widget 配置 Activity |
| API Key 管理 | 存哪 | DataStore，编译时从 properties 打入 |
| 数据源鉴权差异 | 形式不统一 | DataSourceImpl 内部各自处理 |
| 错误标准化 | 错误码不同 | 统一 Error 密封类 |

## MVP 架构路径

```
Phase 1 (验证期):
  固定数据源(和风) + 单城市 + Widget + 无缓存

Phase 2 (完善期):
  SP 缓存 + 多城市管理 + 多数据源架构 + 设置页

Phase 3 (增强期):
  数据源切换 UI + 降级策略 + 限流管理 + 深色 Widget
```

## 实际实现记录

### 架构选型
- Data source interface: `WeatherDataSource` with unified models
- DataSource n-select-1: DataStore records user selection, `WeatherRepository` routes
- Location: sealed class `WeatherLocation` with source-specific subclasses
- DI: Manual via `AppContainer` singleton (no Hilt)
- Widget: RemoteViews directly (no Glance)
- UI: XML layouts (`activity_main.xml`, `weather_widget.xml`)
- Network: Plain OkHttp for all three data sources
- Background: WorkManager `PeriodicWorkRequest` (30min) + on-demand via `WeatherRefreshReceiver`
- Config: DataStore for settings
- Cache: SharedPreferences per location (Phase 2)

### Build System
- AGP: 9.2.0
- Gradle: 9.4.1
- JDK: 26.0.1 (Homebrew)
- API keys: via `buildConfigField` from `api_keys.properties` (gitignored)

### Known Issues & Fixes
- Auto-refresh on startup: ViewModel `init` didn't call `refreshWeather()` → added call in init
- adb pair protocol fault on macOS → workaround: manual pair + connect + install

### Key Decisions
- Default data source: WeatherCN (华风天气)
- Default city: Beijing (LocationKey `101924`)
- Coroutine in Worker: `runBlocking` for suspend calls from `Worker.doWork()`

### 文件结构
```
Weather/
├── app/build.gradle.kts
├── app/src/main/java/com/xilingyuli/weather/
│   ├── MainActivity.kt
│   ├── WeatherApp.kt
│   ├── data/
│   │   ├── datasource/
│   │   │   ├── WeatherDataSource.kt
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
│   │       └── WeatherCache.kt
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

## 参考链接

- 和风天气开发服务: https://dev.qweather.com
- 华风爱科天气平台: https://platform.weathercn.com
- OpenWeatherMap: https://openweathermap.org
- WeatherAPI: https://www.weatherapi.com
- 中国气象数据网: https://data.cma.cn
- 彩云天气 API: https://caiyunapp.com/api/weather
