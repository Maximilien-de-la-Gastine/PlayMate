package com.playmate.ui.history

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

        // Récupérer les informations des marqueurs depuis la base de données
        val dbHelper = DataBase(requireContext())
        val markerCursor = dbHelper.getAllMarkers()

        // Créer une chaîne pour stocker les informations des marqueurs
        val markerInfo = StringBuilder()

        // Parcourir le curseur pour récupérer les informations des marqueurs
        markerCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val markerId = cursor.getLong(cursor.getColumnIndexOrThrow(DataBase.COLUMN_MARKER_ID))
                val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DataBase.COLUMN_LATITUDE))
                val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DataBase.COLUMN_LONGITUDE))
                val eventName = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_EVENT_NAME))
                val sport = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_SPORT))
                val userName = cursor.getString(cursor.getColumnIndexOrThrow(DataBase.COLUMN_USER_NAME))
                // ... Autres champs que vous souhaitez récupérer

                // Ajouter les informations du marqueur à la chaîne de texte
                markerInfo.append("Marker ID: $markerId\n")
                markerInfo.append("Event Name: $eventName\n")
                markerInfo.append("Sport: $sport\n")
                markerInfo.append("User ID: $userName\n")
                // ... Ajouter d'autres informations si nécessaire
                markerInfo.append("\n") // Ajouter une séparation entre les marqueurs
            }
        }

        // Afficher les informations des marqueurs dans le TextView
        textView.text = markerInfo.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
