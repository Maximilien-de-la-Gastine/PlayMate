package com.playmate.ui.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.playmate.R

class EventAdapter(private val eventList: List<EventList>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

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

        holder.textEventSport.text = "Sport: ${event.sport}"
        holder.textEventDate.text = "Date: ${event.date}"
        holder.textEventTime.text = "Time: ${event.time}"
        holder.textEventDuration.text = "Duration: ${event.duration}"
        holder.textEventMaxPeople.text = "MaxPeople: ${event.maxPeople}"
        holder.textEventRequiredEquipement.text = "RequiredEquipment: ${event.requiredEquipment}"
        holder.textEventRequiredLevel.text = "RequiredLevel: ${event.requiredLevel}"
        holder.textEventParticipating.text = "Participating: ${event.participating}"

        // Set click listener on header to show/hide details
        holder.eventTitle.setOnClickListener {
            toggleEventDetails(holder.eventDetails)
        }

        // Bind other event data to corresponding views here
        // ...
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
}