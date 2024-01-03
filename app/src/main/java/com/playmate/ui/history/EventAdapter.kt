package com.playmate.ui.history

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.playmate.DataBase
import com.playmate.R

class EventAdapter(private val eventList: List<EventList>, private val ratingChangeListener: RatingChangeListener) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>(){

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views inside the item_event.xml layout
        val eventTitle: TextView = itemView.findViewById(R.id.text_event_title)
        val textEventSport: TextView = itemView.findViewById(R.id.text_event_sport)
        val textEventDate: TextView = itemView.findViewById(R.id.text_event_date)
        val textEventTime: TextView = itemView.findViewById(R.id.text_event_time)
        val eventDetails: LinearLayout = itemView.findViewById(R.id.layout_event_details)
        val textEventDuration: TextView = itemView.findViewById(R.id.text_event_duration)
        val textEventMaxPeople: TextView = itemView.findViewById(R.id.text_event_max_people)
        val textEventRequiredEquipement: TextView = itemView.findViewById(R.id.text_event_required_equipement)
        val textEventRequiredLevel: TextView = itemView.findViewById(R.id.text_event_required_level)
        val textEventParticipating: TextView = itemView.findViewById(R.id.text_event_participating)
        val textEventAddress: TextView = itemView.findViewById(R.id.text_event_address)
        val buttonRateEvent: Button = itemView.findViewById(R.id.buttonRateEvent)
        val seekBarRating: SeekBar = itemView.findViewById(R.id.seek_bar_rating)
        val buttonSubmitRating: Button = itemView.findViewById(R.id.buttonSubmitRating)
        val textRating: TextView = itemView.findViewById(R.id.text_rating)



    }

    interface RatingChangeListener {
        fun onRatingChanged(rating: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        // Bind data to views in the ViewHolder
        holder.eventTitle.text = event.date

        holder.textEventSport.text = "Votre sport : ${event.sport}"
        holder.textEventDate.text = "La seance a eu lieu le : ${event.date}"
        holder.textEventTime.text = "Votre horaire etait a : ${event.time}"
        holder.textEventDuration.text = "Elle a duree : ${event.duration} h"
        holder.textEventMaxPeople.text = "Il y avait maximum : ${event.maxPeople} personne(s)"
        holder.textEventRequiredEquipement.text = "L'equipement necessaire etait : ${event.requiredEquipment}"
        holder.textEventRequiredLevel.text = "Le niveau requis etait: ${event.requiredLevel}"
        holder.textEventParticipating.text = "Vous etiez : ${event.participating} participant"
        holder.textEventAddress.text = "La seance etait a l'addresse suivante : ${event.address}"

        holder.eventTitle.setOnClickListener {
            toggleEventDetails(holder.eventDetails)
        }

        holder.buttonRateEvent.setOnClickListener {
            // Demander à l'utilisateur s'il souhaite noter la séance
            showRatingDialog(holder)
        }

        // Dans onBindViewHolder
        holder.seekBarRating.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                val ratingValue = progress
                holder.textRating.text = "$ratingValue sur 5"
                holder.buttonSubmitRating.visibility = View.VISIBLE
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Ce qui se passe lorsque l'utilisateur commence à déplacer le curseur
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Ce qui se passe lorsque l'utilisateur a terminé de déplacer le curseur
            }
        })


        holder.buttonSubmitRating.setOnClickListener {
            val rating = holder.seekBarRating.progress
            ratingChangeListener.onRatingChanged(rating)
            val message = "Note envoyée : $rating"
            // Afficher le message dans un Toast ou un autre composant d'affichage
            Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    private fun toggleEventDetails(layout: View) {
        // Toggle visibility of event details
        if (layout.visibility == View.VISIBLE) {
            layout.visibility = View.GONE
        } else {
            layout.visibility = View.VISIBLE
        }
    }

    private fun showRatingDialog(holder: EventViewHolder) {
        val ratingDialog = AlertDialog.Builder(holder.itemView.context)
            .setTitle("Noter la séance")
            .setMessage("Voulez-vous noter cette séance ?")
            .setPositiveButton("Oui") { dialog, _ ->
                // Afficher la SeekBar
                holder.seekBarRating.visibility = View.VISIBLE
                // Masquer le bouton
                holder.buttonRateEvent.visibility = View.GONE
                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        ratingDialog.show()
    }


}
