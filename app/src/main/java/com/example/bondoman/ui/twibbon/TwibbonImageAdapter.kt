package com.example.bondoman.ui.twibbon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.R

class TwibbonImageAdapter (private val twibbonImageIDs : List<Int>,
                           private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<TwibbonImageAdapter.TwibbonImageViewHolder>(){

    inner class TwibbonImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val twibbonButton : ImageButton = itemView.findViewById(R.id.item_twibbon)

        init {
            twibbonButton.setOnClickListener {
                onItemClick(twibbonImageIDs[bindingAdapterPosition])
            }
        }

        fun bind(twibbonImageID: Int) {
            twibbonButton.setImageResource(twibbonImageID)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwibbonImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_twibbon, parent, false)
        return TwibbonImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TwibbonImageViewHolder, position: Int) {
        val twibbonImageID = twibbonImageIDs[position]
        holder.bind(twibbonImageID)
    }

    override fun getItemCount(): Int {
        return twibbonImageIDs.size
    }
}