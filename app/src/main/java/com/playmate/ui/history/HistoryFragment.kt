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
                val sport = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_SPORT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_TIME))
                val duration = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_DURATION))
                val maxPeople = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_MAX_PEOPLE))
                val requiredEquipment = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_REQUIRED_EQUIPMENT))
                val requiredLevel = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_REQUIRED_LEVEL))
                val participating = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_PARTICIPATING))

                markerInfo.append("Date: $date\n")
                markerInfo.append("Sport: $sport\n")
                markerInfo.append("L'heure: $time\n")
                markerInfo.append("Duree: $duration h\n")
                markerInfo.append("Nombre de personne maximum: $maxPeople\n")
                markerInfo.append("Equipement necessaire: $requiredEquipment\n")
                markerInfo.append("Niveau demande: $requiredLevel\n")
                markerInfo.append("Nombre de personne qui sont venues: $participating\n")
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
