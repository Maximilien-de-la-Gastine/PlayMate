package com.playmate.ui.add_event

data class NominatimPlace(
    val display_name: String,
    val lat: String,
    val lon: String
)

class NominatimResponse : ArrayList<NominatimPlace>()

