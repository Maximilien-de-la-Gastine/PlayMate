package com.playmate.ui.planning

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
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
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(
    private val eventList: List<EventList>,
    private val ratingChangeListener: PlanningFragment,
    private val currentUserName: String,
    private val database: DataBase
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val PAST_EVENT = 1
        const val FUTURE_EVENT = 2
    }

    interface RatingChangeListener {
        fun onRatingChanged(rating: Int)
    }

    inner class PastEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    inner class FutureEventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        val buttonFillCalendar: Button = itemView.findViewById(R.id.buttonFillCalendar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PAST_EVENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_past_event, parent, false)
                PastEventViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_future_event, parent, false)
                FutureEventViewHolder(view)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val event = eventList[position]

        when (holder.itemViewType) {
            PAST_EVENT -> {
                val pastHolder = holder as PastEventViewHolder
                pastHolder.eventTitle.text = event.date
                pastHolder.textEventSport.text = "Votre sport : ${event.sport}"
                pastHolder.textEventDate.text = "La séance a eu lieu le : ${event.date}"
                pastHolder.textEventTime.text = "Votre horaire était à : ${event.time}"
                pastHolder.textEventDuration.text = "Elle a duré : ${event.duration} h"
                pastHolder.textEventMaxPeople.text = "Il y avait maximum : ${event.maxPeople} personne(s)"
                pastHolder.textEventRequiredEquipement.text = "L'équipement nécessaire était : ${event.requiredEquipment}"
                pastHolder.textEventRequiredLevel.text = "Le niveau requis était: ${event.requiredLevel}"
                pastHolder.textEventParticipating.text = "Vous étiez : ${event.participating} participant"
                pastHolder.textEventAddress.text = "La séance était à l'adresse suivante : ${event.address}"

                pastHolder.eventTitle.setOnClickListener {
                    toggleEventDetails(pastHolder.eventDetails)
                }

                if (event.isRated) {
                    pastHolder.buttonRateEvent.visibility = View.GONE
                } else {
                    pastHolder.buttonRateEvent.visibility = View.VISIBLE
                    pastHolder.buttonRateEvent.setOnClickListener {
                        if (event.creatorUsername == currentUserName) {
                            Toast.makeText(
                                pastHolder.itemView.context,
                                "Vous ne pouvez pas noter votre propre événement",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showRatingDialog(pastHolder, event)
                        }
                    }
                }

                pastHolder.seekBarRating.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        val ratingValue = progress
                        pastHolder.textRating.text = "$ratingValue sur 5"
                        pastHolder.buttonSubmitRating.visibility = View.VISIBLE
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                pastHolder.buttonSubmitRating.setOnClickListener {
                    val rating = pastHolder.seekBarRating.progress

                    if (!database.hasUserRatedEvent(currentUserName, event.id)) {
                        ratingChangeListener.onRatingChanged(rating)

                        database.addEventRating(currentUserName, event.id)

                        val message = "Note envoyée : $rating"
                        Toast.makeText(pastHolder.itemView.context, message, Toast.LENGTH_SHORT).show()

                        event.isRated = true
                        pastHolder.buttonRateEvent.visibility = View.GONE
                        pastHolder.seekBarRating.visibility = View.GONE
                        pastHolder.buttonSubmitRating.visibility = View.GONE
                    } else {
                        Toast.makeText(pastHolder.itemView.context, "Vous avez déjà noté cet événement", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            FUTURE_EVENT -> {
                val futureHolder = holder as FutureEventViewHolder
                futureHolder.eventTitle.text = event.date
                futureHolder.textEventSport.text = "Votre sport : ${event.sport}"
                futureHolder.textEventDate.text = "La séance a eu lieu le : ${event.date}"
                futureHolder.textEventTime.text = "Votre horaire était à : ${event.time}"
                futureHolder.textEventDuration.text = "Elle a duré : ${event.duration} h"
                futureHolder.textEventMaxPeople.text = "Il y avait maximum : ${event.maxPeople} personne(s)"
                futureHolder.textEventRequiredEquipement.text = "L'équipement nécessaire était : ${event.requiredEquipment}"
                futureHolder.textEventRequiredLevel.text = "Le niveau requis était: ${event.requiredLevel}"
                futureHolder.textEventParticipating.text = "Vous étiez : ${event.participating} participant"
                futureHolder.textEventAddress.text = "La séance était à l'adresse suivante : ${event.address}"

                futureHolder.eventTitle.setOnClickListener {
                    toggleEventDetails(futureHolder.eventDetails)
                }

                futureHolder.buttonFillCalendar.setOnClickListener {
                    val event = eventList[position] // Replace this with getting the specific event for this position
                    addToCalendar(event, futureHolder.itemView.context)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun getItemViewType(position: Int): Int {
        // Déterminez ici si l'événement est passé ou futur
        // Comparez la date de l'événement avec la date actuelle et retournez le viewType approprié
        return if (eventList[position].isPastEvent) {
            PAST_EVENT
        } else {
            FUTURE_EVENT
        }
    }

    private fun toggleEventDetails(layout: View) {
        if (layout.visibility == View.VISIBLE) {
            layout.visibility = View.GONE
        } else {
            layout.visibility = View.VISIBLE
        }
    }

    private fun showRatingDialog(holder: RecyclerView.ViewHolder, event: EventList) {
        val ratingDialog = AlertDialog.Builder(holder.itemView.context)
            .setTitle("Noter la séance")
            .setMessage("Voulez-vous noter cette séance ?")
            .setPositiveButton("Oui") { dialog, _ ->
                if (holder is PastEventViewHolder) {
                    holder.seekBarRating.visibility = View.VISIBLE
                    holder.buttonRateEvent.visibility = View.GONE
                }
                dialog.dismiss()
            }
            .setNegativeButton("Non") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        ratingDialog.show()
    }

    private fun addToCalendar(event: EventList, context: Context) {
        val beginTime: Long = parseDateTime(event.date, event.time)

        val durationInHours = event.duration.toInt() * 60 * 60 * 1000
        val endTime: Long = beginTime + durationInHours

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
            .putExtra(CalendarContract.Events.TITLE, "Votre séance de ${event.sport}")

        context.startActivity(intent)
    }

    private fun parseDateTime(date: String, time: String): Long {
        val dateTimeString = "$date $time"
        val pattern = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateTime = pattern.parse(dateTimeString)
        return dateTime?.time ?: 0
    }
}
