package com.playmate.ui.history

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.playmate.DataBase
import com.playmate.databinding.FragmentHistoryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryFragment : Fragment(), EventAdapter.RatingChangeListener {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = binding.recyclerViewEvents
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dbHelper = DataBase(requireContext())
        val currentUserName = dbHelper.getCurrentUsername()

        val createdEventsCursor = dbHelper.getMarkersByUsername(currentUserName)
        val participatingEventsCursor = dbHelper.getMarkersByParticipant(currentUserName)

        val createdEventsList = createEventListFromCursor(createdEventsCursor)
        val participatingEventsList = createEventListFromCursor(participatingEventsCursor)

        val allEventsList = createdEventsList + participatingEventsList

        val currentCalendar = Calendar.getInstance()

        val filteredEventsList = allEventsList.filter { event ->
            val eventCalendar = Calendar.getInstance().apply {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                time = dateFormat.parse("${event.date} ${event.time}")
            }
            eventCalendar.before(currentCalendar)
        }

        val adapter = EventAdapter(filteredEventsList, this, currentUserName, dbHelper)
        recyclerView.adapter = adapter
    }

    private fun createEventListFromCursor(cursor: Cursor): List<EventList> {
        val eventList = mutableListOf<EventList>()

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(DataBase.COLUMN_MARKER_ID))
                val sport = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_SPORT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_TIME))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_DURATION))
                val maxPeople = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_MAX_PEOPLE))
                val requiredEquipment = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_REQUIRED_EQUIPMENT))
                val requiredLevel = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_REQUIRED_LEVEL))
                val participating = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_PARTICIPATING))
                val address = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_ADDRESS))
                val creatorUsername = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_USER_NAME))

                val event = EventList(
                    id = id,
                    sport = sport,
                    date = date,
                    time = time,
                    duration = duration,
                    maxPeople = maxPeople,
                    requiredEquipment = requiredEquipment,
                    requiredLevel = requiredLevel,
                    participating = participating,
                    address = address,
                    creatorUsername = creatorUsername
                )

                eventList.add(event)
            }
        }

        return eventList
    }

    override fun onRatingChanged(rating: Int) {
        rateEventCreatorsFromParticipatedEvents(rating)
    }

    fun rateEventCreatorsFromParticipatedEvents(rating: Int) {
        val dbHelper = DataBase(requireContext())
        val currentUserName = dbHelper.getCurrentUsername()

        val participatingEventsCursor = dbHelper.getMarkersByParticipant(currentUserName)

        participatingEventsCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val eventId = cursor.getLong(cursor.getColumnIndexOrThrow(DataBase.COLUMN_MARKER_ID))
                val creatorUsername = dbHelper.getMarkerCreatorUsername(eventId)

                if (creatorUsername.isNotEmpty()) {
                    val success = dbHelper.rateUser(creatorUsername, rating)
                    if (success) {
                        // La notation a été enregistrée avec succès pour ce créateur
                    } else {
                        // La notation a échoué pour ce créateur
                    }
                }
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
