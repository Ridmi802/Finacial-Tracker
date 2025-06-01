package com.example.financetracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.data.Transaction
import com.example.financetracker.data.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        private val categoryTextView: TextView = itemView.findViewById(R.id.tvCategory)
        private val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDate)

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val currencyFormat = java.text.NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        fun bind(transaction: Transaction) {
            titleTextView.text = transaction.title
            categoryTextView.text = transaction.category
            amountTextView.text = currencyFormat.format(transaction.amount)
            dateTextView.text = dateFormat.format(transaction.date)

            // Set text color based on transaction type
            val color = if (transaction.type == TransactionType.INCOME) {
                itemView.context.getColor(android.R.color.holo_green_dark)
            } else {
                itemView.context.getColor(android.R.color.holo_red_dark)
            }
            amountTextView.setTextColor(color)
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 