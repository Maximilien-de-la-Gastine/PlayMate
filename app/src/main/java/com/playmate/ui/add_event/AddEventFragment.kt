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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import android.widget.AutoCompleteTextView
import com.playmate.DataBase

class AddEventFragment : Fragment(), LocationListener, MapEventsReceiver {

    private var _binding: FragmentAddEventBinding? = null
    private lateinit var mapViewAddEvent: MapView
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
        val binding = FragmentAddEventBinding.inflate(inflater, container, false)
        _binding = binding
        val root: View = binding.root

        // Initialise la vue de la carte
        mapViewAddEvent = binding.mapViewAddEvent
        Configuration.getInstance().userAgentValue = requireActivity().packageName
        mapViewAddEvent.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapViewAddEvent.setMultiTouchControls(true)

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        requestLocationPermission()

        mapViewAddEvent.overlays.add(0, MapEventsOverlay(this))

        centerMapOnUserLocation()

        return root
    }

    private fun requestLocationPermission() {
        if (isAdded && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                locationPermissionGranted = true
                showUserLocation()
            } else {
                //
            }
        }
    }

    private fun showUserLocation() {
        if (locationPermissionGranted) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100L, 0.5f, this)
                userLocationMarker = Marker(mapViewAddEvent)
                val icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_run_circle_24)
                userLocationMarker.icon = icon
                mapViewAddEvent.overlays.add(userLocationMarker)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val userLocation = GeoPoint(location.latitude, location.longitude)
        if (followUserLocation) {
            centerMapOnUserLocation()
        }
        userLocationMarker.position = userLocation
        mapViewAddEvent.invalidate()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }

    private fun centerMapOnUserLocation() {
        val currentContext = context ?: return

        if (locationPermissionGranted) {
            if (ContextCompat.checkSelfPermission(
                    currentContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    mapViewAddEvent.controller.setCenter(userLocation)
                    mapViewAddEvent.controller.setZoom(19.5)
                    followUserLocation = true
                }
            } else {
                requestLocationPermission()
            }
        } else {
            requestLocationPermission()
        }
    }


    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false
    }

    private fun addMarker(geoPoint: GeoPoint) {

        val marker = Marker(mapViewAddEvent)
        marker.position = geoPoint

        marker.title = "Nouvelle balise : ${geoPoint.latitude}, ${geoPoint.longitude}"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        mapViewAddEvent.overlays.add(marker)
        mapViewAddEvent.invalidate()

        mapViewAddEvent.controller.setCenter(geoPoint)

        val coordinates = "Latitude : ${geoPoint.latitude}, Longitude : ${geoPoint.longitude}"
        Toast.makeText(requireContext(), coordinates, Toast.LENGTH_SHORT).show()

        displayUserMarkers()
    }


    private fun addMarkerToDatabase(geoPoint: GeoPoint, event: Event, userName: String) {
        val dbHelper = DataBase(requireContext())
        val insertedId = dbHelper.addMarkerWithDetails(
            geoPoint.latitude,
            geoPoint.longitude,
            event.eventName,
            event.sport,
            event.date,
            event.time,
            event.duration,
            event.maxPeople,
            event.requiredEquipment,
            event.requiredLevel,
            participating = 1,
            userName = userName,
            event.address
        )
        if (insertedId != -1L) {

        } else {

        }
    }

    private fun showConfirmationDialog(geoPoint: GeoPoint) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Voulez-vous ajouter une balise à cet emplacement ?")

        builder.setPositiveButton("Oui") { dialog, which ->
            showAddEventForm(geoPoint)
        }

        builder.setNegativeButton("Non") { dialog, which ->
        }

        builder.show()
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        p?.let { point ->
            showConfirmationDialog(point)
        }
        return true
    }

    @SuppressLint("MissingInflatedId")
    fun showAddEventForm(geoPoint: GeoPoint) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Ajouter un événement")

        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.add_event_form, null)
        builder.setView(view)

        val eventNameInput = view.findViewById<EditText>(R.id.eventNameInput)
        val sportInput = view.findViewById<Spinner>(R.id.sportInput)
        val dateInput = view.findViewById<TextView>(R.id.dateInput)
        val timeInput = view.findViewById<TextView>(R.id.hourInput)
        val durationInput = view.findViewById<EditText>(R.id.durationInput)
        val maxPeopleInput = view.findViewById<EditText>(R.id.maxParticipantsInput)
        val requiredEquipmentInput = view.findViewById<EditText>(R.id.equipmentInput)
        val requiredLevelInput = view.findViewById<Spinner>(R.id.requiredLevelInput)
        val addressInput = view.findViewById<TextView>(R.id.addressInput)

        val sports = arrayOf("Choisir un sport", "Football", "Basketball", "Tennis", "Course à pied", "Spikeball")

        val sportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sports)
        sportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sportInput.adapter = sportAdapter
        sportInput.setSelection(0, false)
        sportInput.prompt = "Choisir un sport"

        val level = arrayOf("Niveau requis", "Tous les niveaux", "Débutant", "Intermediaire", "Elevé")

        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, level)
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        requiredLevelInput.adapter = levelAdapter
        requiredLevelInput.setSelection(0, false)
        requiredLevelInput.prompt = "Choisir le niveau"

        val retrofit = Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(NominatimService::class.java)
        val call = service.searchPlace("${geoPoint.latitude},${geoPoint.longitude}")

        call.enqueue(object : Callback<NominatimResponse> {
            override fun onResponse(
                call: Call<NominatimResponse>,
                response: Response<NominatimResponse>
            ) {
                if (response.isSuccessful) {
                    val places = response.body()
                    if (!places.isNullOrEmpty()) {
                        val place = places[0]
                        val address = place.display_name

                        addressInput.text = address
                    } else {
                        // le cas où aucun résultat n'a été trouvé pour ces coordonnées
                    }
                } else {
                    // les erreurs de réponse
                }
            }

            override fun onFailure(call: Call<NominatimResponse>, t: Throwable) {
                // les échecs de requête
            }
        })

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
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
            val address = addressInput.text.toString()

            val userDBHelper = DataBase(requireContext())
            val userName = userDBHelper.getCurrentUsername()

            if (eventName.isNotBlank() && selectedSport != "Choisir un sport" && date.isNotBlank() &&
                time.isNotBlank() && duration.isNotBlank() && maxPeople.isNotBlank() && requiredLevel != "Niveau requis"
            ) {
                val event = Event(
                    eventName,
                    selectedSport,
                    date,
                    time,
                    duration.toIntOrNull() ?: 0,
                    maxPeople.toIntOrNull() ?: 0,
                    requiredEquipment,
                    requiredLevel,
                    address
                )

                addMarkerToDatabase(geoPoint, event, userName)
                addMarker(geoPoint)
                followUserLocation = false

                Toast.makeText(requireContext(), "Événement ajouté : $eventName", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Annuler") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private val markerIdMap = HashMap<Marker, Double>()
    private fun displayUserMarkers() {
        val userDBHelper = DataBase(requireContext())
        val userName = userDBHelper.getCurrentUsername()

        val dbHelper = DataBase(requireContext())
        val userMarkers = dbHelper.getMarkersByUsername(userName)

        while (userMarkers.moveToNext()) {
            val markerId = userMarkers.getDouble(userMarkers.getColumnIndexOrThrow("marker_id"))
            val latitude = userMarkers.getDouble(userMarkers.getColumnIndexOrThrow("latitude"))
            val longitude = userMarkers.getDouble(userMarkers.getColumnIndexOrThrow("longitude"))
            val sportName = userMarkers.getString(userMarkers.getColumnIndexOrThrow("sport"))
            val date = userMarkers.getString(userMarkers.getColumnIndexOrThrow("date"))
            val time = userMarkers.getString(userMarkers.getColumnIndexOrThrow("time"))
            val duration = userMarkers.getString(userMarkers.getColumnIndexOrThrow("duration"))
            val maxPeople = userMarkers.getString(userMarkers.getColumnIndexOrThrow("max_people"))
            val requiredEquipment = userMarkers.getString(userMarkers.getColumnIndexOrThrow("required_equipment"))
            val requiredLevel = userMarkers.getString(userMarkers.getColumnIndexOrThrow("required_level"))
            val participating = userMarkers.getString(userMarkers.getColumnIndexOrThrow("participating"))
            val userName = userMarkers.getString(userMarkers.getColumnIndexOrThrow("user_name"))

            val geoPoint = GeoPoint(latitude, longitude)
            val marker = Marker(mapViewAddEvent)
            marker.position = geoPoint

            marker.title = "Votre seance de $sportName"

            markerIdMap[marker] = markerId

            val markerDescription =
                "Createur: $userName \n" +
                        "Date: $date\n" +
                        "Time: $time\n" +
                        "Duration: $duration\n" +
                        "Max People: $maxPeople\n" +
                        "Equipment: $requiredEquipment\n" +
                        "Level: $requiredLevel\n" +
                        "Number of participation: $participating"
            marker.snippet = markerDescription

            marker.setOnMarkerClickListener { _, _ ->
                showParticipantDialog(marker)
                true
            }

            mapViewAddEvent.overlays.add(marker)
        }
    }

    private fun showParticipantDialog(marker: Marker) {
        val title = marker.title
        val snippet = marker.snippet
        val clickedMarkerId = markerIdMap[marker]

        val markerDBHelper = DataBase(requireContext())

        val alertDialogBuilder = android.app.AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(snippet)
        alertDialogBuilder.setPositiveButton("Modifier votre événement") { dialog, _ ->
            clickedMarkerId?.let { markerId ->
                val eventDetails = markerDBHelper.getEventDetails(markerId)

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Modifier l'événement")

                val inflater = LayoutInflater.from(requireContext())
                val view = inflater.inflate(R.layout.add_event_form, null)
                builder.setView(view)

                val eventNameInput = view.findViewById<EditText>(R.id.eventNameInput)
                val sportInput = view.findViewById<Spinner>(R.id.sportInput)
                val dateInput = view.findViewById<TextView>(R.id.dateInput)
                val timeInput = view.findViewById<TextView>(R.id.hourInput)
                val durationInput = view.findViewById<EditText>(R.id.durationInput)
                val maxPeopleInput = view.findViewById<EditText>(R.id.maxParticipantsInput)
                val requiredEquipmentInput = view.findViewById<EditText>(R.id.equipmentInput)
                val requiredLevelInput = view.findViewById<Spinner>(R.id.requiredLevelInput)
                val addressInput = view.findViewById<EditText>(R.id.addressInput)


                val sports = arrayOf("Football", "Basketball", "Tennis", "Course à pied", "Spikeball")
                val sportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sports)
                sportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                sportInput.adapter = sportAdapter

                val level = arrayOf("Tous les niveaux", "Débutant", "Intermediaire", "Elevé")
                val requiredLevelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, level)
                requiredLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                requiredLevelInput.adapter = requiredLevelAdapter

                eventNameInput.setText(eventDetails.eventName)
                sportInput.setSelection(getSportIndex(eventDetails.sport))
                dateInput.text = eventDetails.date
                timeInput.text = eventDetails.time
                durationInput.setText(eventDetails.duration.toString())
                maxPeopleInput.setText(eventDetails.maxPeople.toString())
                requiredEquipmentInput.setText(eventDetails.requiredEquipment)
                requiredLevelInput.setSelection(getLevelIndex(eventDetails.requiredLevel))
                addressInput.setText(eventDetails.address)

                builder.setPositiveButton("Enregistrer") { dialog, _ ->
                    val modifiedEvent = Event(
                        eventNameInput.text.toString(),
                        sportInput.selectedItem.toString(),
                        dateInput.text.toString(),
                        timeInput.text.toString(),
                        durationInput.text.toString().toIntOrNull() ?: 0,
                        maxPeopleInput.text.toString().toIntOrNull() ?: 0,
                        requiredEquipmentInput.text.toString(),
                        requiredLevelInput.selectedItem.toString(),
                        addressInput.text.toString()
                    )

                    val success = markerDBHelper.updateEvent(markerId, modifiedEvent)
                    if (success) {
                        displayUserMarkers()
                        Toast.makeText(requireContext(), "Événement modifié avec succès", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Échec de la modification de l'événement", Toast.LENGTH_SHORT).show()
                    }
                }

                builder.setNegativeButton("Annuler") { dialog, _ ->
                    dialog.dismiss()
                }

                val modifyDialog = builder.create()
                modifyDialog.show()
            }
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Annuler") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialogBuilder.setNeutralButton("Supprimer l'événement") { dialog, _ ->
            clickedMarkerId?.let { markerId ->
                val success = markerDBHelper.deleteMarker(markerId)
                if (success) {
                    displayUserMarkers()
                    refreshMarkersOnMap()
                    Toast.makeText(requireContext(), "Événement supprimé avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Échec de la suppression de l'événement", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        alertDialogBuilder.show()
    }

    private fun getSportIndex(sport: String): Int {
        val sportsArray = arrayOf("Football", "Basketball", "Tennis", "Course à pied", "Spikeball")
        return sportsArray.indexOf(sport)
    }
    private fun getLevelIndex(level: String): Int {
        val levelsArray = arrayOf("Tous les niveaux", "Débutant", "Intermediaire", "Elevé")
        return levelsArray.indexOf(level)
    }

    private fun refreshMarkersOnMap() {
        mapViewAddEvent.overlays.clear()
        displayUserMarkers()
    }

    private var followUserLocation = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchEditText = view.findViewById<AutoCompleteTextView>(R.id.searchBar)
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        searchEditText.setAdapter(adapter)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                getAutocompleteSuggestions(searchText, adapter)
            }
        })

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = searchEditText.text.toString().trim()
                if (searchText.isNotEmpty()) {
                    performSearch(searchText, this)
                    imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                    return@setOnEditorActionListener true
                }
            }
            false
        }

        val followButton = view.findViewById<Button>(R.id.centerButton)
        followButton.setOnClickListener {
            followUserLocation = true
            centerMapOnUserLocation()
        }

        mapViewAddEvent.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    followUserLocation = false
                }
            }
            false
        }

        displayUserMarkers()
    }

    private fun getAutocompleteSuggestions(query: String, adapter: ArrayAdapter<String>) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://votre-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(NominatimService::class.java)
        val call = service.getSuggestions(query)

        call.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val suggestions = response.body()
                    suggestions?.let {
                        adapter.clear()
                        adapter.addAll(it)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    // les erreurs de réponse de l'API
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                // les échecs de requête
            }
        })
    }
}