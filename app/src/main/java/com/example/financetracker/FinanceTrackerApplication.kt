package com.example.financetracker

import android.app.Application
import com.example.financetracker.data.FinanceDatabase
import com.example.financetracker.data.TransactionRepository

class FinanceTrackerApplication : Application() {
    val database by lazy { FinanceDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
} 