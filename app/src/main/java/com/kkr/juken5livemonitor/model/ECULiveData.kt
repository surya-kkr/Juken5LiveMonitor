package com.kkr.juken5livemonitor.model

data class ECULiveData(

    val tps: Int = 0,

    val battery: Double = 0.0,

    val core: Int = 0,

    val rpm: Int = 0,

    val eot: Double = 0.0,

    val fuel: String = "",

    val afr: Double = 0.0,

    val baseMap: String = "",

    val injector: String = "",

    val ignition: Double = 0.0,

    val map: Int = 0,

    val iat: Double = 0.0,

    val speed: Float = 0f

)