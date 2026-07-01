package com.xilingyuli.weather.ui

import com.xilingyuli.weather.data.model.QwLocation
import com.xilingyuli.weather.data.model.WcnLocation

object Defaults {
    val BEIJING_HAIDIAN = WcnLocation(
        id = "北京市_海淀区_110108",
        name = "海淀区",
        province = "北京市",
        city = "北京市",
        district = "海淀区",
        country = "中国",
        latitude = 39.956,
        longitude = 116.310,
        locationKey = "101924",
    )

    val PINGDINGSHAN_WEIDONG = QwLocation(
        id = "河南省_卫东区_410403",
        name = "卫东区",
        province = "河南省",
        city = "平顶山市",
        district = "卫东区",
        country = "中国",
        latitude = 33.735,
        longitude = 113.335,
        locationId = "101705000",
    )

    val LOCATION_AUTO_ID = "auto_current"
}
