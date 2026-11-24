package com.kkr.juken5livemonitor.screen.monitor.Potrait.Racing2.model

data class DashboardData(
    val rpm: Int = 0,
    val speed: Int = 0,
    val afr: Float = 14.7f,
    val battery: Float = 14.2f,
    val eot: Int = 0,
    val ait: Int = 0,
    val map: Int = 0,
    val ignition: Float = 0f,
    val injector: Float = 0f,
    val coreActive: Int = 0,
    val tps: Float = 0f,
    val mapName: String = "Map 1"
)
