package com.playmate.ui.join_event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.playmate.databinding.FragmentJoinEventBinding
import com.playmate.ui.add_event.MarketManager
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class JoinEventFragment : Fragment(), MapEventsReceiver {

    private var _binding: FragmentJoinEventBinding? = null
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentJoinEventBinding.inflate(inflater, container, false)
        _binding = binding
        val root: View = binding.root

        // Initialize map view
        Configuration.getInstance().userAgentValue = requireContext().packageName
        mapView = binding.mapView2
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller
        mapController.setZoom(15.0)

        // Retrieve markers list from MarkerManager (assuming MarkerManager is accessible)
        val markersList = MarketManager.getMarkersListFromAddEventFragment()

        // Add markers from the list to the map
        markersList.forEach { geoPoint ->
            val marker = Marker(mapView)
            marker.position = geoPoint
            // Customize marker if needed (title, icon, etc.)
            mapView.overlays.add(marker)
        }

        return root
    }


    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        TODO("Not yet implemented")
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
