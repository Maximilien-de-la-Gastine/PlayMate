package com.playmate.ui.join_event

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.playmate.MarkerDBHelper
import com.playmate.databinding.FragmentJoinEventBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class JoinEventFragment : Fragment(), MapEventsReceiver, LocationListener {

    private var _binding: FragmentJoinEventBinding? = null
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var locationManager: LocationManager
    private var userMarker: Marker? = null

    companion object {
        const val PERMISSION_REQUEST_LOCATION = 1
    }

    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentJoinEventBinding.inflate(inflater, container, false)
        _binding = binding
        val root: View = binding.root

        Configuration.getInstance().userAgentValue = requireContext().packageName

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                10f,
                this
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        }

        val markerDBHelper = MarkerDBHelper(requireContext())
        val markersCursor = markerDBHelper.getAllMarkers()

        if (markersCursor.moveToFirst()) {
            do {
                val latitude =
                    markersCursor.getDouble(markersCursor.getColumnIndex(MarkerDBHelper.COLUMN_LATITUDE))
                val longitude =
                    markersCursor.getDouble(markersCursor.getColumnIndex(MarkerDBHelper.COLUMN_LONGITUDE))

                val marker = Marker(mapView)
                marker.position = GeoPoint(latitude, longitude)
                mapView.overlays.add(marker)
            } while (markersCursor.moveToNext())
        }

        markersCursor.close()

        val centerButton = binding.centerButton // Replace 'centerButton' with your actual button ID
        centerButton.setOnClickListener {
            centerMapOnUserLocation()
        }

        return root
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return true
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return true
    }

    override fun onLocationChanged(location: Location) {
        val userLocation = GeoPoint(location.latitude, location.longitude)
        centerMapOnLocation(userLocation)

        // Add or update user's marker position
        if (userMarker == null) {
            userMarker = Marker(mapView)
            userMarker?.position = userLocation
            mapView.overlays.add(userMarker)
        } else {
            userMarker?.position = userLocation
        }
    }

    private fun centerMapOnUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let {
                val userLocation = GeoPoint(it.latitude, it.longitude)
                mapController.setCenter(userLocation)
                mapController.setZoom(19.5)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        }
    }

    private fun centerMapOnLocation(geoPoint: GeoPoint) {
        mapController.setCenter(geoPoint)
        mapController.setZoom(15.0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, continue operations that require this permission
                // Restart the fragment or execute location-related operations here
            } else {
                // Permission denied
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mapView.overlays.remove(userMarker)
        userMarker = null
    }
}
