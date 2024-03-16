package com.example.bondoman.repository

import androidx.lifecycle.LiveData
import com.example.bondoman.room.*

class TransactionRepository (private val database: TransactionDatabase) {
    // List of transaction as live data
    val listTransactions: LiveData<List<TransactionEntity>> = database.transactionDAO.getAllTransactions()

    // Add new transaction
    suspend fun insertTransaction(transaction: TransactionEntity) {
        database.transactionDAO.insertTransaction(transaction)
    }

    // Update existing transaction
    suspend fun updateTransaction(transaction: TransactionEntity) {
        database.transactionDAO.updateTransaction(transaction)
    }

    // Delete existing transaction
    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.transactionDAO.deleteTransaction(transaction)
    }

    // Delete all the transaction
    suspend fun deleteAllTransactions() {
        database.transactionDAO.deleteAllTransactions()
    }
}