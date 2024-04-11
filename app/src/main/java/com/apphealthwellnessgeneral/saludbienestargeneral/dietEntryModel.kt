package com.apphealthwellnessgeneral.saludbienestargeneral


data class dietEntryModel(
    val foodName: String,
    val mealType: String,
    val calories: Int,
    val portion: String,
    val timestamp: String,
    val date: String,
    val time: String,
    val recommendation: String
)