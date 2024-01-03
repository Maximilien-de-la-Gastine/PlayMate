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
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.playmate.DataBase
import com.playmate.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import com.playmate.databinding.FragmentJoinEventBinding
import com.playmate.ui.add_event.AddEventFragment

@Suppress("DEPRECATION")
class JoinEventFragment : Fragment(), LocationListener, MapEventsReceiver {

    private var _binding: FragmentJoinEventBinding? = null
    private lateinit var mapViewJoinEvent: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var userLocationMarker: Marker
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
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                AddEventFragment.PERMISSION_REQUEST_LOCATION
            )
        } else {
            locationPermissionGranted = true
            showUserLocation()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AddEventFragment.PERMISSION_REQUEST_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                locationPermissionGranted = true
                showUserLocation()
            } else {
                // La permission a été refusée
                // Gérer les fonctionnalités qui dépendent de la permission ici
            }
        }
    }

    private fun showUserLocation() {
        if (locationPermissionGranted) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500L, 1f, this)
                userLocationMarker = Marker(mapViewJoinEvent)
                val icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_run_circle_24)
                userLocationMarker.icon = icon
                mapViewJoinEvent.overlays.add(userLocationMarker)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val userLocation = GeoPoint(location.latitude, location.longitude)
        if (followUserLocation) {
            centerMapOnUserLocation()
        }
        userLocationMarker.position = userLocation
        mapViewJoinEvent.controller.setCenter(userLocation)
        mapViewJoinEvent.invalidate()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Gérer les changements d'état de la localisation
    }

    override fun onProviderEnabled(provider: String) {
        // La localisation est activée
    }

    override fun onProviderDisabled(provider: String) {
        // La localisation est désactivée
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

    private val markerIdMap = HashMap<Marker, Long>()
    private val markerMaxPeople = HashMap<Long, Int>()

    private fun showMarkersFromDatabase() {
        val markerDBHelper = DataBase(requireContext())
        val markersCursor = markerDBHelper.getAllMarkers()

        while (markersCursor.moveToNext()) {
            val markerId = markersCursor.getLong(markersCursor.getColumnIndexOrThrow("marker_id"))
            val latitude = markersCursor.getDouble(markersCursor.getColumnIndexOrThrow("latitude"))
            val longitude = markersCursor.getDouble(markersCursor.getColumnIndexOrThrow("longitude"))
            val sportName = markersCursor.getString(markersCursor.getColumnIndexOrThrow("sport"))
            val date = markersCursor.getString(markersCursor.getColumnIndexOrThrow("date"))
            val time = markersCursor.getString(markersCursor.getColumnIndexOrThrow("time"))
            val duration = markersCursor.getString(markersCursor.getColumnIndexOrThrow("duration"))
            val maxPeople = markersCursor.getInt(markersCursor.getColumnIndexOrThrow("max_people"))
            val requiredEquipment = markersCursor.getString(markersCursor.getColumnIndexOrThrow("required_equipment"))
            val requiredLevel = markersCursor.getString(markersCursor.getColumnIndexOrThrow("required_level"))
            val participating = markersCursor.getString(markersCursor.getColumnIndexOrThrow("participating"))
            val userName = markersCursor.getString(markersCursor.getColumnIndexOrThrow("user_name"))
            val address = markersCursor.getString(markersCursor.getColumnIndexOrThrow("address"))

            val geoPoint = GeoPoint(latitude, longitude)
            val marker = Marker(mapViewJoinEvent)
            marker.position = geoPoint

            // Titre du marqueur avec le nom du sport
            marker.title = "Voulez-vous rejoindre cette session de $sportName"

            markerIdMap[marker] = markerId
            markerMaxPeople[markerId] = maxPeople



            // Description du marqueur avec les autres informations
            val markerDescription =
                    "Createur: $userName \n" +
                    "Date: $date\n" +
                    "Time: $time\n" +
                    "Duration: $duration\n" +
                    "Max People: $maxPeople\n" +
                    "Equipment: $requiredEquipment\n" +
                    "Level: $requiredLevel\n" +
                    "Number of participation: $participating\n" +
                    "Addresse: $address"
            marker.snippet = markerDescription


            marker.setOnMarkerClickListener { _, _ ->
                showParticipantDialog(marker)
                true
            }

            mapViewJoinEvent.overlays.add(marker)

        }
    }

    private fun showParticipantDialog(marker: Marker) {
        val eventId = markerIdMap[marker]

        AlertDialog.Builder(requireContext())
            .setTitle(marker.title)
            .setMessage(marker.snippet)
            .setPositiveButton("Oui") { dialog, _ ->
                eventId?.let { eventId ->
                    val userDBHelper = DataBase(requireContext())
                    val currentUsername = userDBHelper.getCurrentUsername()
                    if (currentUsername.isNotEmpty()) {
                        addParticipantToEvent(eventId, currentUsername)
                    } else {
                        // Gérer le cas où le nom d'utilisateur actuel n'est pas disponible
                        Toast.makeText(requireContext(), "Nom d'utilisateur introuvable.", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun addParticipantToEvent(eventId: Long, currentUsername: String) {
        val markerDBHelper = DataBase(requireContext())
        val userDBHelper = DataBase(requireContext())

        val maxPeople = markerMaxPeople[eventId] ?: 0
        val currentParticipants = markerDBHelper.getCurrentParticipation(eventId)
        val markerCreator = markerDBHelper.getMarkerCreatorUsername(eventId)

        if (currentParticipants < maxPeople) {
            if (userDBHelper.getCurrentUsername() != markerCreator) {
                // Vérifie si l'utilisateur a déjà rejoint cet événement
                val hasJoined = userDBHelper.hasUserJoinedEvent(currentUsername, eventId)
                if (!hasJoined) {
                    markerDBHelper.incrementParticipants(eventId)
                    userDBHelper.addUserToEvent(currentUsername, eventId) // Enregistre la participation de l'utilisateur à l'événement
                    Toast.makeText(requireContext(), "Inscription réussie !", Toast.LENGTH_SHORT).show()
                    showMarkersFromDatabase()
                } else {
                    Toast.makeText(requireContext(), "Vous avez déjà rejoint cet événement.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Vous ne pouvez pas vous inscrire à votre propre événement.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Nombre maximal de participants atteint pour cet événement. $currentParticipants", Toast.LENGTH_SHORT).show()
        }
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