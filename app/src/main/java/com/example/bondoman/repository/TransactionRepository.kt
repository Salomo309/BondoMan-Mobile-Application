package com.example.bondoman.repository

import androidx.lifecycle.LiveData
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity

class TransactionRepository(private val database: TransactionDatabase, private val nim: String) {
    val listTransactions: LiveData<List<TransactionEntity>> = database.transactionDAO.getAllTransactions(nim)

    // Add new transaction
    suspend fun insertTransaction(transaction: TransactionEntity) {
        transaction.nim = nim // Set nim for the transaction
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

    // Delete all the transactions for the given nim
    suspend fun deleteAllTransactions() {
        database.transactionDAO.deleteAllTransactions(nim)
    }
}