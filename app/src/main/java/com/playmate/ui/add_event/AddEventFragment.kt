package com.playmate.ui.add_event

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.playmate.databinding.FragmentAddEventBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class AddEventFragment : Fragment(), LocationListener, MapEventsReceiver {

    private var _binding: FragmentAddEventBinding? = null
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private var locationPermissionGranted = false

    companion object {
        const val PERMISSION_REQUEST_LOCATION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAddEventBinding.inflate(inflater, container, false)
        _binding = binding
        val root: View = binding.root

        // Initialize the map view
        mapView = binding.mapView
        Configuration.getInstance().userAgentValue = requireActivity().packageName
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.setMultiTouchControls(true)

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermission()

        mapView.overlays.add(0, MapEventsOverlay(this))

        return root
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        } else {
            locationPermissionGranted = true
            showUserLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                locationPermissionGranted = true
                showUserLocation()
            } else {
                // Permission was denied. Disable the functionality that depends on this permission.
            }
        }
    }

    private fun showUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { location ->
                updateMapLocation(location)
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10f, this, Looper.getMainLooper())
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        }
    }

    override fun onLocationChanged(location: Location) {
        updateMapLocation(location)
    }

    private fun updateMapLocation(location: Location) {
        val userLocation = GeoPoint(location.latitude, location.longitude)
        mapView.controller.setCenter(userLocation)
        mapView.controller.setZoom(18.0) // Zoom level à ajuster selon vos besoins.

        // Ajouter un marqueur à la position de l'utilisateur
        val startMarker = Marker(mapView)
        startMarker.position = userLocation
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(startMarker)
        mapView.invalidate()
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        TODO("Not yet implemented")
    }

    private fun addMarker(geoPoint: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun showConfirmationDialog(geoPoint: GeoPoint) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Voulez-vous ajouter une balise à cet emplacement ?")

        builder.setPositiveButton("Oui") { dialog, which ->
            // Code à exécuter si l'utilisateur confirme (par exemple, ouvrir le formulaire d'ajout)
            // Ici, vous pouvez lancer une autre activité ou fragment pour le formulaire
            // Utilisez geoPoint pour obtenir les coordonnées du clic sur la carte
            addMarker(geoPoint)
        }

        builder.setNegativeButton("Non") { dialog, which ->
            // Code à exécuter si l'utilisateur annule
        }

        builder.show()
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        p?.let { point ->
            // Appeler la fonction pour afficher la boîte de dialogue de confirmation
            showConfirmationDialog(point)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}