package com.apphealthwellnessgeneral.saludbienestargeneral

data class sleepData(
    val id: Int,
    val data: String,
    val startTime: Long,
    val endTime: Long,
    val sleepQuality: Int
)