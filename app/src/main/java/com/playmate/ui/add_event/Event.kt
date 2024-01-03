package com.playmate.ui.add_event

data class Event(
    val eventName: String,
    val sport: String,
    val date: String,
    val time: String,
    val duration: Int,
    val maxPeople: Int,
    val requiredEquipment: String,
    val requiredLevel: String,
    val address: String
)

