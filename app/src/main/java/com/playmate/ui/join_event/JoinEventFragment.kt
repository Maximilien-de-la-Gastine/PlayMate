package com.playmate.ui.join_event

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.playmate.MarkerDBHelper
import com.playmate.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import com.playmate.databinding.FragmentJoinEventBinding

@Suppress("DEPRECATION")
class JoinEventFragment : Fragment(), LocationListener, MapEventsReceiver {

    private var _binding: FragmentJoinEventBinding? = null
    private lateinit var mapViewJoinEvent: MapView
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
        val binding = FragmentJoinEventBinding.inflate(inflater, container, false)
        _binding = binding
        val root: View = binding.root

        // Initialise la vue de la carte
        mapViewJoinEvent = binding.mapViewJoinEvent // Met à jour la référence à la carte renommée
        Configuration.getInstance().userAgentValue = requireActivity().packageName
        mapViewJoinEvent.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapViewJoinEvent.setMultiTouchControls(true)

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermission()

        mapViewJoinEvent.overlays.add(0, MapEventsOverlay(this))

        showMarkersFromDatabase()
        centerMapOnUserLocation()
        singleTapConfirmedHelper(p = null)

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

    @Deprecated("Deprecated in Java")
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

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500L, 0.5f, this, Looper.getMainLooper())
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        }
    }

    override fun onLocationChanged(location: Location) {
        if (isAdded) { // Vérifie si le fragment est attaché à un contexte
            if (followUserLocation) {
                centerMapOnUserLocation()
            }
            updateMapLocation(location)
        }
    }


    private var userMarker: Marker? = null
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateMapLocation(location: Location) {
        val userLocation = GeoPoint(location.latitude, location.longitude)

        if (userMarker == null) {
            userMarker = Marker(mapViewJoinEvent)
            userMarker?.position = userLocation
            userMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            userMarker?.icon = requireContext().resources.getDrawable(R.drawable.baseline_run_circle_24)
            mapViewJoinEvent.overlays.add(userMarker)
        } else {
            userMarker?.position = userLocation
        }
    }

    private fun centerMapOnUserLocation() {
        if (isAdded && locationPermissionGranted) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    mapViewJoinEvent.controller.setCenter(userLocation)
                    mapViewJoinEvent.controller.setZoom(19.5)
                    followUserLocation = true
                }
            } else {
                // La permission n'est pas accordée, demandez-la à nouveau
                requestLocationPermission()
            }
        } else {
            // Gérer le cas où la permission de localisation n'est pas accordée
            requestLocationPermission()
        }
    }

    private fun showMarkersFromDatabase() {
        val markerDBHelper = MarkerDBHelper(requireContext())
        val markersCursor = markerDBHelper.getAllMarkers()

        while (markersCursor.moveToNext()) {
            val id = markersCursor.getDouble(markersCursor.getColumnIndexOrThrow("id"))
            val latitude = markersCursor.getDouble(markersCursor.getColumnIndexOrThrow("latitude"))
            val longitude = markersCursor.getDouble(markersCursor.getColumnIndexOrThrow("longitude"))
            val sportName = markersCursor.getString(markersCursor.getColumnIndexOrThrow("sport"))
            val date = markersCursor.getString(markersCursor.getColumnIndexOrThrow("date"))
            val time = markersCursor.getString(markersCursor.getColumnIndexOrThrow("time"))
            val duration = markersCursor.getString(markersCursor.getColumnIndexOrThrow("duration"))
            val maxPeople = markersCursor.getString(markersCursor.getColumnIndexOrThrow("max_people"))
            val requiredEquipment = markersCursor.getString(markersCursor.getColumnIndexOrThrow("required_equipment"))
            val requiredLevel = markersCursor.getString(markersCursor.getColumnIndexOrThrow("required_level"))
            val participating = markersCursor.getString(markersCursor.getColumnIndexOrThrow("participating"))
            val userId = markersCursor.getString(markersCursor.getColumnIndexOrThrow("user_id"))

            val geoPoint = GeoPoint(latitude, longitude)
            val marker = Marker(mapViewJoinEvent)
            marker.position = geoPoint

            // Titre du marqueur avec le nom du sport
            marker.title = sportName

            // Description du marqueur avec les autres informations
            val markerDescription = "Id: $id \nUser id: $userId \nDate: $date\nTime: $time\nDuration: $duration\nMax People: $maxPeople\nEquipment: $requiredEquipment\nLevel: $requiredLevel\nNumber of participation: $participating"
            marker.snippet = markerDescription

            mapViewJoinEvent.overlays.add(marker)

            marker.setOnMarkerClickListener { _, _ ->
                showParticipantDialog(marker)
                true // Indiquer que le clic a été consommé
            }
        }
    }


    private fun addParticipantToEvent(marker: Marker) {
        val eventIdString = marker.snippet
        val idRegex = Regex("Id: (\\d+)") // Expression régulière pour obtenir l'ID
        val matchResult = idRegex.find(eventIdString ?: "")
// EMPECHER UN UTILISATEUR DE S'INSCRIRE DEUX FOIS AU MEME EVENEMENT ---------------------------------------------------------
        matchResult?.let { result ->
            val eventId = result.groupValues[1].toLongOrNull()
            val maxPeopleRegex = Regex("Max People: (\\d+)") // Expression régulière pour obtenir le nombre maximum de participants
            val maxPeopleMatchResult = maxPeopleRegex.find(eventIdString ?: "")
            val maxPeople = maxPeopleMatchResult?.groupValues?.get(1)?.toIntOrNull()

            if (eventId != null && maxPeople != null) {
                val markerDBHelper = MarkerDBHelper(requireContext())

                // Récupérer le nombre de participants actuel pour cet événement
                val currentParticipants = markerDBHelper.getNumberOfParticipants(eventId)

                // Vérifier si le nombre de participants actuel est inférieur au nombre maximal
                if (currentParticipants < maxPeople) {
                    markerDBHelper.incrementParticipants(eventId)
                    Toast.makeText(requireContext(), "Inscription réussie !", Toast.LENGTH_SHORT).show()
                    showMarkersFromDatabase()
                } else {
                    Toast.makeText(requireContext(), "Nombre maximal de participants atteint pour cet événement.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }




    private fun showParticipantDialog(marker: Marker) {
        val title = "Voulez-vous rejoindre la seance de " + marker.title
        val snippet = marker.snippet

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(snippet)
            .setPositiveButton("Oui") { dialog, _ ->
                addParticipantToEvent(marker)
                dialog.dismiss()
            }
            .setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return false
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false
    }




    private var followUserLocation = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val followButton = view.findViewById<Button>(R.id.centerButton)
        followButton.setOnClickListener {
            // Activer le suivi de l'utilisateur lorsqu'on appuie sur le bouton
            followUserLocation = true
            centerMapOnUserLocation() // Centrer la carte sur la position de l'utilisateur
        }

        mapViewJoinEvent.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    // L'utilisateur fait défiler la carte, désactiver le suivi automatique de l'utilisateur
                    followUserLocation = false
                }
            }
            false // Retourne false pour ne pas interrompre le traitement des événements par la carte
        }
    }
}