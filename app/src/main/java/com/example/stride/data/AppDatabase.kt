package com.example.stride.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DaySteps::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
}
