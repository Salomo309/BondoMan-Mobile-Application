package com.example.bondoman.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDAO {
    // Get data
    @Query("SELECT * FROM transactions WHERE nim= :nim")
    fun getAllTransactions(nim: String): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id and nim= :nim")
    fun getTransaction(id: String, nim: String): TransactionEntity?

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
