package com.example.financetracker.data

import kotlinx.coroutines.flow.Flow
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    suspend fun getTransactionsByDateRangeSync(startDate: Date, endDate: Date): List<Transaction> {
        return transactionDao.getTransactionsByDateRangeSync(startDate, endDate)
    }

    fun getTotalExpenses(startDate: Date, endDate: Date): Flow<Double> {
        return transactionDao.getTotalExpenses(startDate, endDate)
    }

    fun getTotalIncome(startDate: Date, endDate: Date): Flow<Double> {
        return transactionDao.getTotalIncome(startDate, endDate)
    }

    fun getExpensesByCategory(startDate: Date, endDate: Date): Flow<List<CategoryTotal>> {
        return transactionDao.getExpensesByCategory(startDate, endDate)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getAllTransactionsSync(): List<Transaction> {
        return transactionDao.getAllTransactionsSync()
    }
} 