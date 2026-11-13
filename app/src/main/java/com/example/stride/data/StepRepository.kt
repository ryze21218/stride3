package com.example.stride.data

import com.example.stride.GoalNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

class StepRepository(
    private val dao: StepDao,
    private val prefs: Prefs,
    private val notifier: GoalNotifier,
) {
    private val mutex = Mutex()

    fun observeSince(date: LocalDate): Flow<List<DaySteps>> = dao.observeSince(date.toString())

    /**
     * Turns a raw step-counter reading into "steps added to today".
     *
     * TYPE_STEP_COUNTER is a running total since the phone last booted, so:
     *  - normal case: delta = raw - lastRaw
     *  - reboot (raw < lastRaw): the counter restarted at 0, so delta = raw
     *  - very first reading: no baseline yet, so delta = 0
     *
     * Steps are credited to the day the reading arrives. The background worker runs
     * about every 15 minutes, so the error around midnight is small.
     */
    suspend fun onSensorReading(raw: Long) = mutex.withLock {
        val last = prefs.lastRaw
        val delta = when {
            last < 0 -> 0L
            raw < last -> raw
            else -> raw - last
        }
        prefs.lastRaw = raw
        if (delta <= 0) return@withLock

        val today = LocalDate.now().toString()
        val before = dao.getSteps(today) ?: 0
        dao.add(today, delta.toInt())
        val after = before + delta.toInt()

        val goal = prefs.goal
        if (before < goal && after >= goal && prefs.notifiedDate != today) {
            prefs.notifiedDate = today
            notifier.goalReached(goal)
        }
    }
}
