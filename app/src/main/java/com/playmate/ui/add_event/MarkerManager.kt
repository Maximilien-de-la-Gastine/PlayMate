package com.playmate.ui.add_event

import org.osmdroid.util.GeoPoint

object MarketManager {
    private val markersList: MutableList<GeoPoint> = mutableListOf()

    fun getMarkersListFromAddEventFragment(): List<GeoPoint> {
        return markersList.toList()
    }

    fun addMarker(geoPoint: GeoPoint) {
        // Ajouter le GeoPoint à la liste des marqueurs
        markersList.add(geoPoint)
    }
}

