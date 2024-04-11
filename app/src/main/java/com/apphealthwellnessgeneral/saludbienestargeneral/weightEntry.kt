package com.apphealthwellnessgeneral.saludbienestargeneral

data class weightEntry(
    val weight: Float,
    val bmi: Float,
    val category: String,
    val dateAndTime: Long, // Make sure this is of type Long
    val email: String
)