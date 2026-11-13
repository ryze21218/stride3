package com.example.stride.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class StepDao {

    @Query("SELECT * FROM day_steps WHERE date >= :from ORDER BY date")
    abstract fun observeSince(from: String): Flow<List<DaySteps>>

    @Query("SELECT steps FROM day_steps WHERE date = :date")
    abstract suspend fun getSteps(date: String): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIfAbsent(day: DaySteps)

    @Query("UPDATE day_steps SET steps = steps + :delta WHERE date = :date")
    abstract suspend fun addToDay(date: String, delta: Int)

    @Transaction
    open suspend fun add(date: String, delta: Int) {
        insertIfAbsent(DaySteps(date, 0))
        addToDay(date, delta)
    }
}
