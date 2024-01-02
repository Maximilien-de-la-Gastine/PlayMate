package com.playmate.ui.history

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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
        val textView: TextView = binding.textHistory
        val dbHelper = DataBase(requireContext())
        val markerInfo = StringBuilder()

        val currentUserName = dbHelper.getCurrentUsername()

        // Récupérer les événements créés par l'utilisateur actuel
        val createdEventsCursor = dbHelper.getMarkersByUsername(currentUserName)
        markerInfo.append("Événements créés par $currentUserName :\n\n")
        markerInfo.append(getEventInfoFromCursor(createdEventsCursor))

        // Récupérer les événements auxquels l'utilisateur participe
        val participatingEventsCursor = dbHelper.getMarkersByParticipant(currentUserName)
        markerInfo.append("\n\nÉvénements auxquels participe $currentUserName :\n\n")
        markerInfo.append(getEventInfoFromCursor(participatingEventsCursor))

        textView.text = markerInfo.toString()
    }

    // Fonction pour obtenir les informations sur les événements à partir du curseur
    private fun getEventInfoFromCursor(cursor: Cursor): String {
        val markerInfo = StringBuilder()
        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val markerId = cursor.getLong(cursor.getColumnIndexOrThrow(DataBase.COLUMN_MARKER_ID))
                val eventName = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_EVENT_NAME))
                val sport = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_SPORT))
                val userName = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_USER_NAME))
                // ... Autres champs que vous souhaitez récupérer

                markerInfo.append("Marker ID: $markerId\n")
                markerInfo.append("Event Name: $eventName\n")
                markerInfo.append("Sport: $sport\n")
                markerInfo.append("User ID: $userName\n")
                markerInfo.append("\n")
            }
        }
        return markerInfo.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
