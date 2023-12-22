package com.playmate.ui.add_event

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.playmate.R
import com.playmate.databinding.FragmentAddEventBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Calendar

class AddEventFragment : Fragment(), LocationListener, MapEventsReceiver {

    private var _binding: FragmentAddEventBinding? = null
    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager
    private var locationPermissionGranted = false
    private val markersList: MutableList<GeoPoint> = mutableListOf()


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
        if (followUserLocation) {
            centerMapOnUserLocation()
        }
        updateMapLocation(location)
    }


    private var userMarker: Marker? = null
    private fun updateMapLocation(location: Location) {
        val userLocation = GeoPoint(location.latitude, location.longitude)

        if (userMarker == null) {
            userMarker = Marker(mapView)
            userMarker?.position = userLocation
            userMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(userMarker)
        } else {
            userMarker?.position = userLocation
        }

        // Zoom à votre niveau préféré sans recentrer la carte
        mapView.controller.setZoom(20.0)
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false
    }

    fun addMarker(geoPoint: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = "Nouvelle balise : $geoPoint" // Ajout des coordonnées dans le titre du marqueur
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
        mapView.invalidate()

        // Ajouter le GeoPoint à la liste
        markersList.add(geoPoint)

        // Affichage des coordonnées dans un Toast
        val coordinates = "Latitude : ${geoPoint.latitude}, Longitude : ${geoPoint.longitude}"
        Toast.makeText(requireContext(), coordinates, Toast.LENGTH_SHORT).show()
    }

    private fun showConfirmationDialog(geoPoint: GeoPoint) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Voulez-vous ajouter une balise à cet emplacement ?")

        builder.setPositiveButton("Oui") { dialog, which ->
            // Code à exécuter si l'utilisateur confirme (par exemple, ouvrir le formulaire d'ajout)
            // Ici, vous pouvez lancer une autre activité ou fragment pour le formulaire
            // Utilisez geoPoint pour obtenir les coordonnées du clic sur la carte
            showAddEventForm(geoPoint)
        }

        builder.setNegativeButton("Non") { dialog, which ->
            // Code à exécuter si l'utilisateur annule
        }

        builder.show()
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        p?.let { point ->
            showConfirmationDialog(point)
        }
        return true
    }

    private fun showAddEventForm(geoPoint: GeoPoint) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ajouter un événement")

        // Création du layout pour le formulaire
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_event_form, null)
        builder.setView(view)

        // Déclaration des vues du formulaire
        val eventNameInput = view.findViewById<EditText>(R.id.eventNameInput)
        val sportInput = view.findViewById<Spinner>(R.id.sportInput)
        val dateInput = view.findViewById<TextView>(R.id.dateInput)
        val timeInput = view.findViewById<TextView>(R.id.hourInput)
        val durationInput = view.findViewById<EditText>(R.id.durationInput)
        val maxPeopleInput = view.findViewById<EditText>(R.id.maxParticipantsInput)
        val requiredEquipmentInput = view.findViewById<EditText>(R.id.equipmentInput)
        val requiredLevelInput = view.findViewById<Spinner>(R.id.requiredLevelInput)
        val latitudeInput = view.findViewById<EditText>(R.id.latitudeInput)
        val longitudeInput = view.findViewById<EditText>(R.id.longitudeInput)

        // Options de sports disponibles
        val sports = arrayOf("Choisir un sport", "Football", "Basketball", "Tennis", "Course à pied", "Spikeball")


        // Création de l'adaptateur pour le Spinner
        val sportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sports)
        sportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sportInput.adapter = sportAdapter
        sportInput.setSelection(0, false)
        sportInput.prompt = "Choisir un sport"

        //option pour le niveau
        val level = arrayOf("Niveau requis", "Tous les niveaux", "Débutant", "Intermediaire", "Elevé")

        //spinner du niveau
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, level)
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        requiredLevelInput.adapter = levelAdapter
        requiredLevelInput.setSelection(0, false)
        requiredLevelInput.prompt = "Choisir le niveau"

        // Remplir les champs de latitude et longitude (non modifiables)
        latitudeInput.setText(geoPoint.latitude.toString())
        longitudeInput.setText(geoPoint.longitude.toString())
        latitudeInput.isEnabled = false
        longitudeInput.isEnabled = false

        // Sélection de la date avec DatePicker
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear" // +1 car les mois commencent à 0
                dateInput.text = selectedDate
            },
            year,
            month,
            day
        )

        dateInput.isFocusable = false
        dateInput.setOnClickListener {
            datePickerDialog.show()
        }

        timeInput.isFocusable = false

        timeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    timeInput.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
                },
                hour,
                minute,
                true
            )
            timePickerDialog.show()
        }

        builder.setPositiveButton("Ajouter") { dialog, _ ->
            val eventName = eventNameInput.text.toString()
            val selectedSport = sportInput.selectedItem.toString()
            val date = dateInput.text.toString()
            val time = timeInput.text.toString()
            val duration = durationInput.text.toString()
            val maxPeople = maxPeopleInput.text.toString()
            val requiredEquipment = requiredEquipmentInput.text.toString()
            val requiredLevel = requiredLevelInput.selectedItem.toString()

            // Création de l'objet Event avec les informations saisies
            val event = Event(
                eventName,
                selectedSport,
                date,
                time,
                duration,
                maxPeople.toIntOrNull() ?: 0,
                requiredEquipment,
                requiredLevel,
                geoPoint.latitude,
                geoPoint.longitude
            )

            addMarker(geoPoint)

            // Affichage d'un Toast pour informer l'utilisateur que l'événement a été ajouté
            Toast.makeText(requireContext(), "Événement ajouté : $eventName", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
        }

        builder.setNegativeButton("Annuler") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }







    private var followUserLocation = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Trouver la référence de la barre de recherche EditText
        val searchEditText = view.findViewById<EditText>(R.id.searchBar)

        // Ajouter un écouteur pour détecter l'action de recherche (IME_ACTION_SEARCH)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = searchEditText.text.toString().trim()
                if (searchText.isNotEmpty()) {
                    performSearch(searchText, this)
                    // Fermez le clavier ici si nécessaire
                    return@setOnEditorActionListener true
                }
            }
            false
        }

        val followButton = view.findViewById<Button>(R.id.centerButton)
        followButton.setOnClickListener {
            // Activer le suivi de l'utilisateur lorsqu'on appuie sur le bouton
            followUserLocation = true
            centerMapOnUserLocation() // Centrer la carte sur la position de l'utilisateur
        }

        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    // L'utilisateur fait défiler la carte, désactiver le suivi automatique de l'utilisateur
                    followUserLocation = false
                }
            }
            false // Retourne false pour ne pas interrompre le traitement des événements par la carte
        }
    }

    private fun centerMapOnUserLocation() {
        if (locationPermissionGranted) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    mapView.controller.setCenter(userLocation)
                    mapView.controller.setZoom(20.0)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}