package com.playmate.ui.history

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.playmate.DataBase
import com.playmate.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

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

        // Créer une liste d'événements en fonction des curseurs obtenus
        val createdEventsList = createEventListFromCursor(createdEventsCursor)
        val participatingEventsList = createEventListFromCursor(participatingEventsCursor)

        // Fusionner les deux listes si nécessaire
        val allEventsList = createdEventsList + participatingEventsList

        val adapter = EventAdapter(allEventsList) // EventAdapter est votre adaptateur personnalisé
        recyclerView.adapter = adapter
    }

    // Fonction pour obtenir les informations sur les événements à partir du curseur
    private fun createEventListFromCursor(cursor: Cursor): List<EventList> {
        val eventList = mutableListOf<EventList>()

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val sport = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_SPORT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_TIME))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_DURATION))
                val maxPeople = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_MAX_PEOPLE))
                val requiredEquipment = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_REQUIRED_EQUIPMENT))
                val requiredLevel = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_REQUIRED_LEVEL))
                val participating = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_PARTICIPATING))

                val event = EventList(
                    sport = sport,
                    date = date,
                    time = time,
                    duration = duration,
                    maxPeople = maxPeople,
                    requiredEquipment = requiredEquipment,
                    requiredLevel = requiredLevel,
                    participating = participating
                )

                eventList.add(event)
            }
        }

        return eventList
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
