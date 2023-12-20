package com.playmate.ui.add_event

data class NominatimPlace(
    val display_name: String,
    val lat: String,
    val lon: String
)

// Cette classe représente une liste de résultats de recherche
class NominatimResponse : ArrayList<NominatimPlace>()

