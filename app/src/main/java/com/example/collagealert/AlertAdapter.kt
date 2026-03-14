package com.example.collagealert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class AlertAdapter(
    private var alerts: List<AlertData>,
    private val onItemClick: (AlertData) -> Unit,
    private val onItemLongClick: (AlertData) -> Unit
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount() = alerts.size

    fun updateData(newAlerts: List<AlertData>) {
        alerts = newAlerts
        notifyDataSetChanged()
    }

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val priorityBar: View = itemView.findViewById(R.id.priorityBar)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val typeEmojiTextView: TextView = itemView.findViewById(R.id.typeEmojiTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val readStatusTextView: TextView = itemView.findViewById(R.id.readStatusTextView)

        init {
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(alerts[pos])
            }
            itemView.setOnLongClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemLongClick(alerts[pos])
                    true
                } else false
            }
        }

        fun bind(alert: AlertData) {
            titleTextView.text = alert.title
            messageTextView.text = alert.message
            timeTextView.text = alert.formattedTime
            typeEmojiTextView.text = alert.typeEmoji

            // Set Saturated Aesthetic Colors based on Priority
            val (barColor, bgColor) = when (alert.priority) {
                Priority.HIGH -> Pair(R.color.priority_high, R.color.priority_high_bg)
                Priority.MEDIUM -> Pair(R.color.priority_medium, R.color.priority_medium_bg)
                Priority.LOW -> Pair(R.color.priority_low, R.color.priority_low_bg)
                else -> Pair(R.color.primary, R.color.priority_normal_bg)
            }

            priorityBar.setBackgroundColor(ContextCompat.getColor(itemView.context, barColor))
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, bgColor))

            if (alert.isRead) {
                readStatusTextView.text = "READ"
                readStatusTextView.setBackgroundResource(R.drawable.status_badge_read)
                cardView.alpha = 0.6f // Visual hierarchy for read items
            } else {
                readStatusTextView.text = "NEW"
                readStatusTextView.setBackgroundResource(R.drawable.status_badge_new)
                cardView.alpha = 1.0f
            }
        }
    }
}