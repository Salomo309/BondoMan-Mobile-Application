package com.example.bondoman.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDAO {
    // Get data
    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransaction(id: String): TransactionEntity?

    // Insert data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Update data
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    // Delete data
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions(): Int

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
