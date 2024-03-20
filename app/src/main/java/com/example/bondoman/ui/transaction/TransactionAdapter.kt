package com.example.bondoman.ui.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.example.bondoman.R
import com.example.bondoman.models.Transaction

class TransactionAdapter(var transactions: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    inner class TransactionViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val date: TextView = itemView.findViewById(R.id.date)
        val month: TextView = itemView.findViewById(R.id.month)
        val time: TextView = itemView.findViewById(R.id.time)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val category: TextView = itemView.findViewById(R.id.category)
        // val location: TextView = itemView.findViewById(R.id.location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction = transactions[position]
        holder.title.text = currentTransaction.title

        // Extracting day, month, and time
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = dateFormat.format(currentTransaction.date)
        val month = monthFormat.format(currentTransaction.date).toLowerCase(Locale.getDefault())
        val time = timeFormat.format(currentTransaction.date)

        holder.date.text = date
        holder.month.text = month
        holder.time.text = time

        // Converting amount to Indonesian currency (Rupiah)
        val amountFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedAmount = amountFormat.format(currentTransaction.amount)
        holder.amount.text = formattedAmount

        holder.category.text = currentTransaction.category
        // holder.location.text = currentTransaction.location
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
}
