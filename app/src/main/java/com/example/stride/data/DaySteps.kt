package com.example.stride.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per calendar day. [date] is ISO format, e.g. 2026-11-12. */
@Entity(tableName = "day_steps")
data class DaySteps(
    @PrimaryKey val date: String,
    val steps: Int,
)
