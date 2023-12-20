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
import android.view.inputmethod.EditorInfo
import android.widget.EditText
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
        updateMapLocation(location)
    }

    private fun updateMapLocation(location: Location) {
        val userLocation = GeoPoint(location.latitude, location.longitude)
        mapView.controller.setCenter(userLocation)
        mapView.controller.setZoom(10.0) // Zoom level à ajuster selon vos besoins.

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
            // Appeler la fonction pour afficher la boîte de dialogue de confirmation
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
        val sportInput = view.findViewById<EditText>(R.id.sportInput)
        val dateInput = view.findViewById<EditText>(R.id.dateInput)
        val timeInput = view.findViewById<EditText>(R.id.hourInput)
        val durationInput = view.findViewById<EditText>(R.id.durationInput)
        val maxPeopleInput = view.findViewById<EditText>(R.id.maxParticipantsInput)
        val requiredEquipmentInput = view.findViewById<EditText>(R.id.equipmentInput)
        val requiredLevelInput = view.findViewById<EditText>(R.id.requiredLevelInput)
        val latitudeInput = view.findViewById<EditText>(R.id.latitudeInput)
        val longitudeInput = view.findViewById<EditText>(R.id.longitudeInput)

        // Remplir les champs de latitude et longitude (non modifiables)
        latitudeInput.setText(geoPoint.latitude.toString())
        longitudeInput.setText(geoPoint.longitude.toString())
        latitudeInput.isEnabled = false
        longitudeInput.isEnabled = false

        builder.setPositiveButton("Ajouter") { dialog, _ ->
            val eventName = eventNameInput.text.toString()
            val sport = sportInput.text.toString()
            val date = dateInput.text.toString()
            val time = timeInput.text.toString()
            val duration = durationInput.text.toString()
            val maxPeople = maxPeopleInput.text.toString()
            val requiredEquipment = requiredEquipmentInput.text.toString()
            val requiredLevel = requiredLevelInput.text.toString()

            // Création de l'objet Event avec les informations saisies
            val event = Event(
                eventName,
                sport,
                date,
                time,
                duration,
                maxPeople.toIntOrNull() ?: 0,
                requiredEquipment,
                requiredLevel,
                geoPoint.latitude,
                geoPoint.longitude
            )

            // Ajout de la logique pour enregistrer l'événement ici
            // Par exemple, ajouter l'objet Event à une liste d'événements
            // eventsList.add(event)

            // Ajouter le marqueur une fois l'événement confirmé et créé
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}