package com.example.financetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: Date = Date(),
    val description: String? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE
} 