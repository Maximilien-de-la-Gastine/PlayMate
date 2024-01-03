package com.playmate.ui.history

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.playmate.DataBase
import com.playmate.R

class EventAdapter(
    private val eventList: List<EventList>,
    private val ratingChangeListener: RatingChangeListener,
    private val currentUserName: String,
    private val database: DataBase
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        holder.eventTitle.text = event.date
        holder.textEventSport.text = "Votre sport : ${event.sport}"
        holder.textEventDate.text = "La séance a eu lieu le : ${event.date}"
        holder.textEventTime.text = "Votre horaire était à : ${event.time}"
        holder.textEventDuration.text = "Elle a duré : ${event.duration} h"
        holder.textEventMaxPeople.text = "Il y avait maximum : ${event.maxPeople} personne(s)"
        holder.textEventRequiredEquipement.text = "L'équipement nécessaire était : ${event.requiredEquipment}"
        holder.textEventRequiredLevel.text = "Le niveau requis était: ${event.requiredLevel}"
        holder.textEventParticipating.text = "Vous étiez : ${event.participating} participant"
        holder.textEventAddress.text = "La séance était à l'adresse suivante : ${event.address}"

        holder.eventTitle.setOnClickListener {
            toggleEventDetails(holder.eventDetails)
        }

        if (event.isRated) {
            holder.buttonRateEvent.visibility = View.GONE
        } else {
            holder.buttonRateEvent.visibility = View.VISIBLE
            holder.buttonRateEvent.setOnClickListener {
                if (event.creatorUsername == currentUserName) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Vous ne pouvez pas noter votre propre événement",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showRatingDialog(holder, event)
                }
            }
        }

        holder.seekBarRating.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val ratingValue = progress
                holder.textRating.text = "$ratingValue sur 5"
                holder.buttonSubmitRating.visibility = View.VISIBLE
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        holder.buttonSubmitRating.setOnClickListener {
            val rating = holder.seekBarRating.progress

            if (!database.hasUserRatedEvent(currentUserName, event.id)) {
                ratingChangeListener.onRatingChanged(rating)

                database.addEventRating(currentUserName, event.id)

                val message = "Note envoyée : $rating"
                Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()

                event.isRated = true
                holder.buttonRateEvent.visibility = View.GONE
                holder.seekBarRating.visibility = View.GONE
                holder.buttonSubmitRating.visibility = View.GONE
            } else {
                Toast.makeText(holder.itemView.context, "Vous avez déjà noté cet événement", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    private fun toggleEventDetails(layout: View) {
        if (layout.visibility == View.VISIBLE) {
            layout.visibility = View.GONE
        } else {
            layout.visibility = View.VISIBLE
        }
    }

    private fun showRatingDialog(holder: EventViewHolder, event: EventList) {
        val ratingDialog = AlertDialog.Builder(holder.itemView.context)
            .setTitle("Noter la séance")
            .setMessage("Voulez-vous noter cette séance ?")
            .setPositiveButton("Oui") { dialog, _ ->
                holder.seekBarRating.visibility = View.VISIBLE
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
