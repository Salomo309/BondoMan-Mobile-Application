package com.example.bondoman.ui.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.R
import com.example.bondoman.models.Transaction

class TransactionAdapter(var transactions: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    inner class TransactionViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val date: TextView = itemView.findViewById(R.id.date)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val category: TextView = itemView.findViewById(R.id.category)
        val location: TextView = itemView.findViewById(R.id.location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction = transactions[position]
        holder.title.text = currentTransaction.title
        holder.date.text = currentTransaction.date.toString()
        holder.amount.text = currentTransaction.amount.toString()
        holder.category.text = currentTransaction.category
        holder.location.text = currentTransaction.location
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
}
