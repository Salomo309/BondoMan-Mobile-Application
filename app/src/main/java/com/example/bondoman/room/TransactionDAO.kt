package com.example.bondoman.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.bondoman.models.TransactionSummary

@Dao
interface TransactionDAO {
    // Get data
    @Query("SELECT * FROM transactions WHERE nim= :nim")
    fun getAllTransactions(nim: String): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id and nim= :nim")
    fun getTransaction(id: String, nim: String): TransactionEntity?

    // Get transaction summary
    @Query("SELECT category, SUM(amount) as totalAmount FROM transactions WHERE nim= :nim GROUP BY category")
    fun getTransactionSummary(nim: String): LiveData<List<TransactionSummary>>

    // Insert data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Update data
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    // Delete data
    @Query("DELETE FROM transactions WHERE nim= :nim")
    suspend fun deleteAllTransactions(nim: String): Int

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
