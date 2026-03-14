package com.example.collagealert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoticeAdapter(
    private var notices: List<RealtimeAlert>,
    private val onDeleteClick: (RealtimeAlert) -> Unit
) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notica, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.bind(notices[position])
    }

    override fun getItemCount() = notices.size

    fun updateData(newNotices: List<RealtimeAlert>) {
        notices = newNotices
        notifyDataSetChanged()
    }

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        private val priorityTextView: TextView = itemView.findViewById(R.id.priorityTextView)
        private val deleteButton: MaterialButton = itemView.findViewById(R.id.deleteButton)
        private val typeEmojiTextView: TextView = itemView.findViewById(R.id.typeEmojiTextView)
        private val priorityBar: View = itemView.findViewById(R.id.priorityBar)

        fun bind(notice: RealtimeAlert) {
            titleTextView.text = notice.title
            messageTextView.text = notice.message
            typeTextView.text = notice.type
            priorityTextView.text = notice.priority

            // Set Emoji based on type
            typeEmojiTextView.text = when (notice.type) {
                "EXAM" -> "📝"
                "SEMINAR" -> "🎯"
                "HOLIDAY" -> "🏖️"
                "NOTICE" -> "📢"
                "URGENT" -> "🚨"
                else -> "📌"
            }

            // Format time
            val date = Date(notice.timestamp)
            val format = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            timeTextView.text = format.format(date)

            // Priority colors for Bar and Background
            val (barColor, bgColor) = when (notice.priority) {
                "HIGH" -> Pair(R.color.priority_high, R.color.priority_high_bg)
                "MEDIUM" -> Pair(R.color.priority_medium, R.color.priority_medium_bg)
                "LOW" -> Pair(R.color.priority_low, R.color.priority_low_bg)
                else -> Pair(R.color.primary, R.color.priority_normal_bg)
            }

            priorityBar.setBackgroundColor(ContextCompat.getColor(itemView.context, barColor))
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, bgColor))
            priorityTextView.setTextColor(ContextCompat.getColor(itemView.context, barColor))

            deleteButton.setOnClickListener {
                onDeleteClick(notice)
            }
        }
    }
}
