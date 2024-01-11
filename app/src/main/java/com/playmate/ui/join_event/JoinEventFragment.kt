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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Suppress("DEPRECATION")
class JoinEventFragment : Fragment(), LocationListener, MapEventsReceiver {

    private var _binding: FragmentJoinEventBinding? = null
    private lateinit var mapViewJoinEvent: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var userLocationMarker: Marker
    private var locationPermissionGranted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentJoinEventBinding.inflate(inflater, container, false)
        _binding = binding
        val root: View = binding.root

        // Initialise la vue de la carte
        mapViewJoinEvent = binding.mapViewJoinEvent
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
        if (isAdded && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), AddEventFragment.PERMISSION_REQUEST_LOCATION)
        } else {
            locationPermissionGranted = true
            showUserLocation()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AddEventFragment.PERMISSION_REQUEST_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                locationPermissionGranted = true
                showUserLocation()
            } else {
                // fonctionnalités qui dépendent de la permission
            }
        }
    }

    private fun showUserLocation() {
        if (locationPermissionGranted && isAdded) {
            val currentContext = context ?: return

            if (ContextCompat.checkSelfPermission(currentContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100L, 0.5f, this, Looper.getMainLooper())
                userLocationMarker = Marker(mapViewJoinEvent)
                val icon = ContextCompat.getDrawable(currentContext, R.drawable.baseline_run_circle_24)
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

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // les changements d'état de la localisation
    }

    override fun onProviderEnabled(provider: String) {
        // La localisation est activée
    }

    override fun onProviderDisabled(provider: String) {
        // La localisation est désactivée
    }

    private fun centerMapOnUserLocation() {
        if (isAdded && locationPermissionGranted) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    mapViewJoinEvent.controller.setCenter(userLocation)
                    mapViewJoinEvent.controller.setZoom(19.5)
                    followUserLocation = true
                }
            } else {
                requestLocationPermission()
            }
        } else {
            requestLocationPermission()
        }
    }

    private val markerIdMap = HashMap<Marker, Long>()
    private val markerMaxPeople = HashMap<Long, Int>()

    private fun showMarkersFromDatabase() {
        val currentDateTime = getCurrentDateTime()
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
            val userScore = markerDBHelper.getUserScore(userName)

            val geoPoint = GeoPoint(latitude, longitude)
            val marker = Marker(mapViewJoinEvent)
            marker.position = geoPoint

            val dateTimeString = "$date $time"
            val markerDateTime = parseDateTime(dateTimeString)

            if (markerDateTime > currentDateTime) {
                marker.title = "Voulez-vous rejoindre cette session de $sportName"

                markerIdMap[marker] = markerId
                markerMaxPeople[markerId] = maxPeople

                val markerDescription =
                            "Createur: $userName \n" +
                            "Note du createur de la seance: $userScore sur 5\n" +
                            "Date: $date\n" +
                            "Heure: $time\n" +
                            "Duree: $duration\n" +
                            "Nombre de personne maximum: $maxPeople\n" +
                            "Equipment: $requiredEquipment\n" +
                            "Niveau: $requiredLevel\n" +
                            "Nombre de participant: $participating\n" +
                            "Addresse: $address"
                marker.snippet = markerDescription


                marker.setOnMarkerClickListener { _, _ ->
                    showParticipantDialog(marker)
                    true
                }

                mapViewJoinEvent.overlays.add(marker)

                marker.setOnMarkerClickListener { _, _ ->
                    showParticipantDialog(marker)
                    true
                }

                mapViewJoinEvent.overlays.add(marker)
            }
        }
    }

    private fun getCurrentDateTime(): LocalDateTime {
        return LocalDateTime.now()
    }

    private fun parseDateTime(dateTimeString: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return LocalDateTime.parse(dateTimeString, formatter)
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
                val hasJoined = userDBHelper.hasUserJoinedEvent(currentUsername, eventId)
                if (!hasJoined) {
                    markerDBHelper.incrementParticipants(eventId)
                    userDBHelper.addUserToEvent(currentUsername, eventId)
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
            followUserLocation = true
            centerMapOnUserLocation()
        }

        mapViewJoinEvent.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    followUserLocation = false
                }
            }
            false
        }
    }
}