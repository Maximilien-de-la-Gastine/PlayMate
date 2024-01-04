package com.playmate.ui.history

data class EventList(
    val id: Long,
    val sport: String,
    val date: String,
    val time: String,
    val duration: String,
    val maxPeople: String,
    val requiredEquipment: String,
    val requiredLevel: String,
    val participating: String,
    val address: String,
    var isRated: Boolean = false,
    val creatorUsername: String,
    val isPastEvent: Boolean
)

